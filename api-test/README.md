# Pre-Registration API Test Rig

Functional API tests (REST Assured + TestNG) for **external** Pre-Registration HTTP APIs under `/preregistration/v1` (applications, documents, login, sync, captcha, booking).

| Level | `env.testLevel` | What runs |
|-------|-----------------|-----------|
| Smoke | `smoke` | Positive cases only |
| Full | `smokeAndRegression` | Positive + negative |

---

## Prerequisites

- JDK **21**
- Maven **3.9+**
- Lombok
- MOSIP Maven `settings.xml` in `~/.m2` ([copy](https://github.com/mosip/mosip-functional-tests/blob/master/settings.xml))

Windows: Git Bash optional. Linux: also put `settings.xml` under Maven `/conf` if you install Maven system-wide.

Clone:

```sh
git clone https://github.com/mosip/pre-registration.git
cd pre-registration
```

Paths below are from that **git clone root**. Nested Java/Maven lives in `pre-registration/`; this rig lives in `api-test/`.

---

## 1. Run locally (laptop)

Local means: Docker deps + **host** `pre-registration-service` on `:9090`. Tests do **not** use `prereg.properties` (server/QA). They use `prereg-local.properties` and WireMock IAM on `:8082`.

| Piece | URL | Role |
|-------|-----|------|
| Pre-Registration | `http://localhost:9090/preregistration/v1` | Service under test (host JVM) |
| WireMock | `http://localhost:8082` | Local IAM, masterdata, audit |
| Booking | `http://localhost:9090` | native `/preregistration/v1/appointment/**` on the HTTP JVM |
| mock-smtp-sms | `http://localhost:8080` | Mailbox UI + `/sendsms`; SMTP `:8025`; WS `:8081` |
| notifier | `http://localhost:8083` | Forwards `/v1/notifier` into mock-smtp |
| PostgreSQL | `localhost:5455` | `mosip_prereg`, `mosip_audit`, `mosip_keymgr` |

There is **no Keycloak**. Tokens come from WireMock.

Full stack notes: [`pre-registration/local-dev-setup/README.md`](../pre-registration/local-dev-setup/README.md) (detail: [`LOCAL-DEV-SETUP.md`](../pre-registration/local-dev-setup/LOCAL-DEV-SETUP.md)).

### Step A — Start Docker deps and the host app

**Windows:**

```bat
cd pre-registration\local-dev-setup
run-prereg-local.bat up
run-prereg-local.bat start
```

**Linux / macOS / Git Bash:**

```sh
cd pre-registration/local-dev-setup
./run-local-stack.sh up
./run-prereg-local.sh start
```

Wait until the host app is up on `:9090`.

### Step B — Check the stack

```bat
curl.exe -s http://localhost:9090/preregistration/v1/actuator/health
curl.exe -s http://localhost:8082/__admin/health
```

### Step C — Run api-test

Secrets default to local-dev values (`mosip123`, `local-dev-testrig-secret`, …). Optional override: copy `api-test/run-local.env.example` to gitignored `api-test/.env.local`.

Do **not** edit `prereg.properties` for local runs.

**Windows:**

```bat
cd api-test
run-local-smoke.bat smoke
run-local-smoke.bat
```

**Linux / macOS / Git Bash:**

```sh
cd api-test
./run-local-smoke.sh smoke                 # positives only
./run-local-smoke.sh                       # smokeAndRegression (default)
```

The script:

- Preflights `:9090` and `:8082` (skip with `SKIP_PREFLIGHT=1`)
- Rebuilds the fat jar if `src/main/resources` is newer than the jar
- Writes console + `api-test/.local/logs/run-local-<level>-<timestamp>.log`
- Writes HTML reports to `api-test/testng-report/`

---

## 2. Run against a server / QA environment

Use this for a deployed MOSIP env (not localhost). Edit **server** properties; do **not** use `run-local-smoke.*` or `prereg-local.properties`.

### Step A — Fill server properties

Edit `api-test/src/main/resources/config/prereg.properties`:

- `keycloak-external-url`, `db-server`, JDBC URLs
- Client secrets and DB passwords for that environment

Do not commit real secrets.

### Step B — Build

```sh
cd api-test
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```

On Windows PowerShell, quote `-D` flags:

```bat
mvn clean install "-Dgpg.skip=true" "-Dmaven.gitcommitid.skip=true"
```

### Step C — Run the jar

```sh
cd api-test/target
java -Dmodules=prereg \
  -Denv.user=api-internal.<env> \
  -Denv.endpoint=https://api-internal.<env>.mosip.net \
  -Denv.testLevel=smokeAndRegression \
  -jar apitest-prereg-*-jar-with-dependencies.jar
```

Windows:

```bat
cd api-test\target
java -Dmodules=prereg -Denv.user=api-internal.<env> -Denv.endpoint=https://api-internal.<env>.mosip.net -Denv.testLevel=smokeAndRegression -jar apitest-prereg-1.4.1-SNAPSHOT-jar-with-dependencies.jar
```

| Flag | Server example | Meaning |
|------|----------------|---------|
| `env.user` | `api-internal.qa-java21` | Report / run-context prefix |
| `env.endpoint` | `https://api-internal.<env>.mosip.net` | MOSIP internal gateway |
| `env.testLevel` | `smoke` or `smokeAndRegression` | Suite size |

Omit `-Dprereg.propertiesFile` so the runner loads `prereg.properties`.

### Eclipse (server)

1. Import `api-test` as an existing Maven project.
2. **Run → Run Configurations → Java Application**, main class `io.mosip.testrig.apirig.prereg.testrunner.MosipTestRunner`.
3. VM arguments (server):

```
-Dmodules=prereg -Denv.user=api-internal.<env> -Denv.endpoint=https://api-internal.<env>.mosip.net -Denv.testLevel=smokeAndRegression
```

---

## Reports

| Output | Path |
|--------|------|
| HTML TestNG report | `api-test/testng-report/` |
| Script console log | `api-test/.local/logs/run-local-<level>-<timestamp>.log` |
| Log4j | `api-test/.local/logs/mosip-api-test.log` |

Report counts: **T** total, **P** passed, **F** failed, **S** skipped (missing dependency), **I** ignored (schema / feature not supported), **KI** known issues.

On `smoke`, most YAML cases are skipped (not failed). A large skip count with a few passes is expected. Use `smokeAndRegression` for negatives.

---

## Local troubleshooting

| Symptom | Fix |
|---------|-----|
| Preflight: `:9090` down | Start `run-prereg-local.bat start` |
| Preflight: `:8082` down | `run-prereg-local.bat up` (WireMock) |
| `Unknown lifecycle phase ".skip=true"` | PowerShell split `-D`; quote `"-Dgpg.skip=true"` or use `run-local-smoke.bat` |
| Runner hits QA hosts | You used green ▶ on `MosipTestRunner` or omitted `prereg-local.properties` |
| YAML change not picked up | `run-local-smoke` rebuilds when resources are newer than the jar |
| Auth / token failures | Recreate WireMock; testrig secret is `local-dev-testrig-secret` |
| Slack / `/home/mosip/testrig/report` errors | Cluster-only; ignore on a laptop |

---

## License

[Mozilla Public License 2.0](https://github.com/mosip/mosip-platform/blob/master/LICENSE)
