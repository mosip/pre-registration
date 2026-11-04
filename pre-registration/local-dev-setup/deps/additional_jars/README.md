# Additional jars (`-Dloader.path`)

Only jars that are **not** Maven dependencies belong here. The host JVM
(`run-prereg-local.*`) passes this folder to `-Dloader.path`. Docker
(`configure_start.sh`) wget’s the same artifacts into the image loader path.

`kernel-auth-adapter` is a Maven dependency of `pre-registration-service`.
`TransliterationImpl` and ICU4J ship in `kernel-core`. Do not put them here.
`kernel-ref-idobjectvalidator.jar` is thin: its runtime libs (`commons-collections4`,
`json-path`) are Maven dependencies of `pre-registration-service`.

| Jar | Required locally | Source |
|-----|------------------|--------|
| `kernel-ref-idobjectvalidator.jar` | **Yes** | [mosip-ref-impl `issue-1633`](https://github.com/mosip/mosip-ref-impl/tree/issue-1633/kernel/kernel-ref-idobjectvalidator) |
| `kernel-virusscanner-clamav.jar` | **Yes** | [mosip-ref-impl `issue-1633`](https://github.com/mosip/mosip-ref-impl/tree/issue-1633/kernel/kernel-virusscanner-clamav) |

`kernel-virusscanner-clamav.jar` is the ClamAV client, not a scanner. Local compose has no ClamAV daemon, so laptop config sets `mosip.preregistration.document.scan=false`. See `local-dev-setup/README.md`.

`*.jar` in this folder is gitignored. Cluster images wget from
`kernel_ref_idobjectvalidator_url` and `virusscanner_url_env` (artifactory-share
ConfigMap) — do not pin these SPIs in `pre-registration-service/pom.xml`.

Place the jars here, or from `local-dev-setup/`:

```bash
bash run-local-stack.sh prep
```

Windows cmd: `run-prereg-local.bat prep`
