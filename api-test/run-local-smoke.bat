@echo off
REM Run api-test against local pre-registration-service without touching prereg.properties.
REM Usage: run-local-smoke.bat [smokeAndRegression^|smoke^|help] [testCaseIds]
REM Output: console + .local\logs\run-local-<testLevel>-<timestamp>.log
REM Prereqs: Docker deps up; host pre-registration-service on :9090; WireMock IAM on :8082.
REM Secrets: optional .env.local; otherwise local-dev-setup defaults are used.
REM Skip health preflight: set SKIP_PREFLIGHT=1
setlocal EnableExtensions EnableDelayedExpansion

set "API_TEST_DIR=%~dp0"
cd /d "%API_TEST_DIR%"

set "TEST_LEVEL=%~1"
set "TEST_CASES=%~2"
if "%TEST_LEVEL%"=="" set "TEST_LEVEL=smokeAndRegression"

call :usage
if /I "%TEST_LEVEL%"=="help" exit /b 0
if /I "%TEST_LEVEL%"=="-h" exit /b 0
if /I "%TEST_LEVEL%"=="--help" exit /b 0
if "%TEST_LEVEL%"=="/?" exit /b 0
set "LEVEL_OK="
if /I "%TEST_LEVEL%"=="smokeAndRegression" set "TEST_LEVEL=smokeAndRegression" & set "LEVEL_OK=1"
if /I "%TEST_LEVEL%"=="smoke" set "TEST_LEVEL=smoke" & set "LEVEL_OK=1"
if not defined LEVEL_OK (
  echo ERROR: unknown run type "%TEST_LEVEL%". Use one of the run types above.
  exit /b 1
)
if defined TEST_CASES set "testCasesToExecute=%TEST_CASES%"
echo Selected: %TEST_LEVEL%
if defined TEST_CASES (echo Test cases: %TEST_CASES%) else (echo Test cases: all)
echo.

if exist "%API_TEST_DIR%.env.local" (
  for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%API_TEST_DIR%.env.local") do (
    if not "%%A"=="" if not defined %%A set "%%A=%%B"
  )
)

if not defined postgres-password set "postgres-password=mosip123"
if not defined keycloak_Password set "keycloak_Password=admin"
if not defined mosip_testrig_client_secret set "mosip_testrig_client_secret=local-dev-testrig-secret"
if not defined mosip_admin_client_secret set "mosip_admin_client_secret=local-dev-secret"
if not defined mosip_partner_client_secret set "mosip_partner_client_secret=local-dev-secret"
if not defined mosip_pms_client_secret set "mosip_pms_client_secret=local-dev-secret"
if not defined mosip_resident_client_secret set "mosip_resident_client_secret=local-dev-secret"
if not defined mosip_idrepo_client_secret set "mosip_idrepo_client_secret=local-dev-secret"
if not defined mosip_reg_client_secret set "mosip_reg_client_secret=local-dev-secret"
if not defined mosip_hotlist_client_secret set "mosip_hotlist_client_secret=local-dev-secret"
if not defined mosip_regproc_client_secret set "mosip_regproc_client_secret=local-dev-secret"
if not defined mpartner_default_mobile_secret set "mpartner_default_mobile_secret=local-dev-secret"
if not defined AuthClientSecret set "AuthClientSecret=local-dev-secret"
if not defined mosip_crvs1_client_secret set "mosip_crvs1_client_secret=local-dev-secret"

if /I not "%SKIP_PREFLIGHT%"=="1" (
  echo Checking local stack ^(pre-registration-service + WireMock^)...
  set "PREFLIGHT_FAIL="
  curl.exe -sf --max-time 5 http://localhost:8082/__admin/health >nul 2>&1
  if errorlevel 1 (
    echo ERROR: WireMock local IAM is not reachable: http://localhost:8082/__admin/health
    echo        Start Docker deps: pre-registration\local-dev-setup  ^(run-prereg-local.bat up^)
    set "PREFLIGHT_FAIL=1"
  ) else (
    echo OK  WireMock local IAM  http://localhost:8082
  )
  curl.exe -sf --max-time 5 http://localhost:9090/preregistration/v1/actuator/health >nul 2>&1
  if errorlevel 1 (
    echo ERROR: pre-registration-service is not reachable: http://localhost:9090/preregistration/v1/actuator/health
    echo        Start the host app: pre-registration\local-dev-setup\run-prereg-local.bat start
    set "PREFLIGHT_FAIL=1"
  ) else (
    echo OK  pre-registration-service  http://localhost:9090/preregistration/v1
  )
  if defined PREFLIGHT_FAIL (
    echo Set SKIP_PREFLIGHT=1 to bypass ^(tests will still fail if the service is down^).
    exit /b 1
  )
)

set "authCertsPath=%API_TEST_DIR%target\local-authcerts"
set "REPORT_DIR=%API_TEST_DIR%target\local-reports"

set "LOG_DIR=%API_TEST_DIR%.local\logs"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set "STAMP=%%I"
set "LOG_FILE=%LOG_DIR%\run-local-%TEST_LEVEL%-%STAMP%.log"

set "JAR="
set "NEED_BUILD="
for %%F in ("%API_TEST_DIR%target\apitest-prereg-*-jar-with-dependencies.jar") do set "JAR=%%~fF"
set "PROPS_FILE=%API_TEST_DIR%src\main\resources\config\prereg-local.properties"
set "LOG4J_FILE=%API_TEST_DIR%src\main\resources\log4j.properties"
set "POM_FILE=%API_TEST_DIR%pom.xml"
if not defined JAR set "NEED_BUILD=1"
if defined JAR if exist "%PROPS_FILE%" (
  for /f %%I in ('powershell -NoProfile -Command "if ((Get-Item -LiteralPath '%PROPS_FILE%').LastWriteTime -gt (Get-Item -LiteralPath '%JAR%').LastWriteTime) { '1' }"') do set "NEED_BUILD=%%I"
)
if not defined NEED_BUILD if defined JAR if exist "%LOG4J_FILE%" (
  for /f %%I in ('powershell -NoProfile -Command "if ((Get-Item -LiteralPath '%LOG4J_FILE%').LastWriteTime -gt (Get-Item -LiteralPath '%JAR%').LastWriteTime) { '1' }"') do set "NEED_BUILD=%%I"
)
if not defined NEED_BUILD if defined JAR if exist "%POM_FILE%" (
  for /f %%I in ('powershell -NoProfile -Command "if ((Get-Item -LiteralPath '%POM_FILE%').LastWriteTime -gt (Get-Item -LiteralPath '%JAR%').LastWriteTime) { '1' }"') do set "NEED_BUILD=%%I"
)
if not defined NEED_BUILD if defined JAR (
  for /f %%I in ('powershell -NoProfile -Command "$jar=(Get-Item -LiteralPath '%JAR%').LastWriteTime; if (Get-ChildItem -LiteralPath '%API_TEST_DIR%src\main\resources' -Recurse -File | Where-Object { $_.LastWriteTime -gt $jar } | Select-Object -First 1) { '1' }"') do set "NEED_BUILD=%%I"
)
if not defined NEED_BUILD if defined JAR (
  for /f %%I in ('powershell -NoProfile -Command "$jar=(Get-Item -LiteralPath '%JAR%').LastWriteTime; if (Get-ChildItem -LiteralPath '%API_TEST_DIR%src\main\java' -Recurse -Filter *.java | Where-Object { $_.LastWriteTime -gt $jar } | Select-Object -First 1) { '1' }"') do set "NEED_BUILD=%%I"
)
if defined NEED_BUILD (
  echo Building api-test jar ^(pom/sources/resources newer than jar, or jar missing^)...
  call mvn clean install "-Dgpg.skip=true" "-Dmaven.gitcommitid.skip=true" "-Dmaven.javadoc.skip=true"
  if errorlevel 1 exit /b 1
  for %%F in ("%API_TEST_DIR%target\apitest-prereg-*-jar-with-dependencies.jar") do set "JAR=%%~fF"
)

if not defined JAR (
  echo ERROR: apitest-prereg jar-with-dependencies not found under target\
  exit /b 1
)

REM After mvn clean, so the report mount and auth cert dirs survive the rebuild.
if not exist "%authCertsPath%" mkdir "%authCertsPath%"
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"

set "EXTRACTED_PROPS=%API_TEST_DIR%target\MosipTestResource\MosipTemporaryTestResource\config\prereg-local.properties"
if exist "%EXTRACTED_PROPS%" (
  powershell -NoProfile -Command "(Get-Content -Raw '%EXTRACTED_PROPS%') -replace '(?m)^authCertsPath\s*=.*$','authCertsPath = target/local-authcerts' | Set-Content -NoNewline '%EXTRACTED_PROPS%'"
)

(
  echo ===== run-local-smoke start %DATE% %TIME% =====
  echo Using JAR: %JAR%
  echo Pre-registration service: http://localhost:9090/preregistration/v1
  echo WireMock IAM / stubs: http://localhost:8082
  echo Properties: prereg-local.properties
  echo testLevel: %TEST_LEVEL%
  echo authCertsPath: %authCertsPath%
  echo Log file: %LOG_FILE%
  echo ================================================
) > "%LOG_FILE%"

type "%LOG_FILE%"

set "LOG_FILE=%LOG_FILE%"
set "LOG_DIR=%LOG_DIR%"
set "JAR=%JAR%"
set "TEST_LEVEL=%TEST_LEVEL%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "& { $ErrorActionPreference = 'Continue'; $log = $env:LOG_FILE; $javaArgs = @(('-Dapi.test.log.dir=' + $env:LOG_DIR), '-Dprereg.propertiesFile=prereg-local.properties', '-Dprereg.skipPartnerSetup=true', '-Dmodules=prereg', '-Denv.user=api-internal.local', '-Denv.endpoint=http://localhost:8082', '-Denv.keycloak=http://localhost:8082', ('-Denv.testLevel=' + $env:TEST_LEVEL), '-jar', $env:JAR); $utf8 = New-Object System.Text.UTF8Encoding $false; $fs = [System.IO.File]::Open($log, 'Append', 'Write', 'ReadWrite'); $sw = New-Object System.IO.StreamWriter($fs, $utf8); $sw.AutoFlush = $true; try { & java @javaArgs 2>&1 | ForEach-Object { $line = $_.ToString(); Write-Host $line; $sw.WriteLine($line) }; $code = $LASTEXITCODE; if ($null -eq $code) { $code = 0 }; $sw.WriteLine('===== run-local-smoke end exit=' + $code + ' ====='); Write-Host ('Log written: ' + $log); exit $code } finally { $sw.Dispose() } }"

set "RC=!ERRORLEVEL!"
endlocal & exit /b %RC%

:usage
echo.
echo run-local-smoke.bat [runType] [testCaseIds]
echo.
echo Run types:
echo   smokeAndRegression   all smoke + regression cases (default)
echo   smoke                smoke cases only
echo   help ^| -h ^| --help   show this list and exit
echo.
echo testCaseIds (optional): comma-separated uniqueIdentifiers
echo.
echo Environment:
echo   SKIP_PREFLIGHT=1     skip the WireMock / pre-registration health checks
echo   .env.local           optional client secrets (see run-local.env.example)
echo.
exit /b 0
