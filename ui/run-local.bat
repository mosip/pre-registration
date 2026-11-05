@echo off
REM Run the Angular SPA against local pre-registration-service without editing config.json.
REM Usage: run-local.bat [init^|start^|smoke^|stop^|help]
REM Default: start
REM Prereqs: Node 14.x preferred (Angular 7 / webpack 4). Node 17+ is accepted with
REM NODE_OPTIONS=--openssl-legacy-provider (ERR_OSSL_EVP_UNSUPPORTED otherwise).
REM Docker deps up; host pre-registration-service on :9090.
REM Skip health preflight: set SKIP_PREFLIGHT=1
REM Backend URL: PREREG_URL (default http://localhost:9090)
setlocal EnableExtensions EnableDelayedExpansion

set "UI_DIR=%~dp0"
cd /d "%UI_DIR%"

if not defined PORT set "PORT=4200"
if not defined PREREG_URL set "PREREG_URL=http://localhost:9090"
set "LOG_DIR=%UI_DIR%.local\logs"
set "PID_DIR=%UI_DIR%.local\pids"
set "PID_FILE=%PID_DIR%\prereg-ui.pid"
set "LOG_FILE=%LOG_DIR%\prereg-ui.log"
set "ERR_FILE=%LOG_DIR%\prereg-ui.err.log"
set "PROXY_FILE=%UI_DIR%proxy.conf.json"
set "UI_URL=http://127.0.0.1:%PORT%"
set "HEALTH_URL=%PREREG_URL%/preregistration/v1/actuator/health"

set "CMD=%~1"
if "%CMD%"=="" set "CMD=start"

if /I "%CMD%"=="help" goto :help
if /I "%CMD%"=="-h" goto :help
if /I "%CMD%"=="--help" goto :help
if /I "%CMD%"=="init" goto :init
if /I "%CMD%"=="start" goto :start
if /I "%CMD%"=="smoke" goto :smoke
if /I "%CMD%"=="stop" goto :stop

echo ERROR: unknown command "%CMD%". Use one of the commands above.
call :usage
exit /b 1

:help
call :usage
exit /b 0

:usage
echo.
echo run-local.bat [init^|start^|smoke^|stop^|help]
echo.
echo   init     npm install
echo   start    background ng serve + /proxyapi -^> %PREREG_URL%  (default)
echo   smoke    GET %UI_URL%
echo   stop     stop the process listening on :%PORT%
echo   help     show this list and exit
echo.
echo Environment:
echo   SKIP_PREFLIGHT=1     skip the WireMock / pre-registration health checks
echo   PREREG_URL           backend origin (default http://localhost:9090)
echo   PORT                 Angular port (default 4200; must match src\assets\config.json)
echo   NODE_OPTIONS         Node 17+ gets --openssl-legacy-provider automatically
echo.
echo Open %UI_URL%
echo config.json is already BASE_URL=%UI_URL%/proxyapi/ — this script does not edit it.
echo proxy.conf.json is gitignored (written at start).
echo npm install writes to .local\node_modules; node_modules is a junction to that folder.
echo start returns after compile; logs: .local\logs\prereg-ui.log
echo.
exit /b 0

:ensure_dirs
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%PID_DIR%" mkdir "%PID_DIR%"
exit /b 0

:ensure_node_modules
REM Physical install lives in .local\node_modules (gitignored with .local/).
REM Angular CLI still resolves ui\node_modules via a directory junction.
if not exist "%UI_DIR%.local" mkdir "%UI_DIR%.local"
if exist "%UI_DIR%node_modules" (
  fsutil reparsepoint query "%UI_DIR%node_modules" >nul 2>&1
  if not errorlevel 1 (
    echo node_modules -^> .local\node_modules
    exit /b 0
  )
  if exist "%UI_DIR%.local\node_modules\" (
    echo error: both ui\node_modules and ui\.local\node_modules exist as real folders.
    echo        Stop ng serve, keep one of them, then re-run.
    exit /b 1
  )
  echo ==^> moving node_modules -^> .local\node_modules
  move /Y "%UI_DIR%node_modules" "%UI_DIR%.local\node_modules" >nul
  if errorlevel 1 (
    echo error: could not move node_modules. Stop ng serve and retry.
    exit /b 1
  )
)
if not exist "%UI_DIR%.local\node_modules\" mkdir "%UI_DIR%.local\node_modules"
if not exist "%UI_DIR%node_modules" (
  mklink /J "%UI_DIR%node_modules" "%UI_DIR%.local\node_modules" >nul
  if errorlevel 1 (
    echo error: could not create junction ui\node_modules -^> ui\.local\node_modules
    exit /b 1
  )
)
echo node_modules -^> .local\node_modules
exit /b 0

:check_node
where node >nul 2>&1
if errorlevel 1 (
  echo error: node is required on PATH
  exit /b 1
)
where npm >nul 2>&1
if errorlevel 1 (
  echo error: npm is required on PATH
  exit /b 1
)
for /f "delims=" %%V in ('node -v 2^>nul') do set "NODE_VER=%%V"
echo node: %NODE_VER%
echo %NODE_VER% | findstr /R /C:"^v14\." >nul
if errorlevel 1 (
  echo warn: Node 14.x is preferred ^(Angular 7 / webpack 4^). Found: %NODE_VER%.
)
call :openssl_legacy
exit /b 0

:openssl_legacy
REM webpack 4 hashes with MD4, removed in OpenSSL 3 (Node 17+).
set "NODE_MAJOR="
for /f "tokens=1 delims=." %%A in ("%NODE_VER:v=%") do set "NODE_MAJOR=%%A"
if not defined NODE_MAJOR exit /b 0
set /a NODE_MAJOR_NUM=NODE_MAJOR 2>nul
if %NODE_MAJOR_NUM% LSS 17 exit /b 0
if defined NODE_OPTIONS (
  echo %NODE_OPTIONS% | findstr /C:"openssl-legacy-provider" >nul
  if not errorlevel 1 exit /b 0
  set "NODE_OPTIONS=%NODE_OPTIONS% --openssl-legacy-provider"
) else (
  set "NODE_OPTIONS=--openssl-legacy-provider"
)
echo Node %NODE_VER% uses OpenSSL 3; setting NODE_OPTIONS=--openssl-legacy-provider for webpack 4
exit /b 0

:write_proxy
(
  echo {
  echo   "/proxyapi": {
  echo     "target": "%PREREG_URL%",
  echo     "secure": false,
  echo     "changeOrigin": true,
  echo     "logLevel": "info",
  echo     "pathRewrite": {
  echo       "^/proxyapi": ""
  echo     }
  echo   }
  echo }
) > "%PROXY_FILE%"
exit /b 0

:preflight
if /I "%SKIP_PREFLIGHT%"=="1" (
  echo Skipping health preflight ^(SKIP_PREFLIGHT=1^)
  exit /b 0
)
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
curl.exe -sf --max-time 5 "%HEALTH_URL%" >nul 2>&1
if errorlevel 1 (
  echo ERROR: pre-registration-service is not reachable: %HEALTH_URL%
  echo        Start the host app: pre-registration\local-dev-setup\run-prereg-local.bat start
  set "PREFLIGHT_FAIL=1"
) else (
  echo OK  pre-registration-service  %PREREG_URL%/preregistration/v1
)
if defined PREFLIGHT_FAIL (
  echo Set SKIP_PREFLIGHT=1 to bypass ^(the UI will still fail API calls if the service is down^).
  exit /b 1
)
exit /b 0

:init
call :check_node
if errorlevel 1 exit /b 1
call :ensure_node_modules
if errorlevel 1 exit /b 1
echo ==^> npm install (ui/ -^> .local\node_modules)
call npm.cmd install
if errorlevel 1 exit /b 1
echo init complete
exit /b 0

:start
call :ensure_dirs
call :check_node
if errorlevel 1 exit /b 1
call :ensure_node_modules
if errorlevel 1 exit /b 1
if not exist "%UI_DIR%node_modules\@angular\cli\" (
  echo node_modules missing; running npm install...
  call :init
  if errorlevel 1 exit /b 1
)
call :write_proxy
call :preflight
if errorlevel 1 exit /b 1
if not exist "%UI_DIR%node_modules\@angular\cli\bin\ng" (
  echo error: Angular CLI not found. Run: %~nx0 init
  exit /b 1
)
call :pid_on_port
if defined PORT_PID (
  echo pre-registration-ui already listening on port %PORT% ^(pid !PORT_PID!^)
  call :open_browser
  exit /b 0
)
echo.
echo ==^> ng serve (HTTP, background)  port=%PORT%
echo     proxy %UI_URL%/proxyapi  -^>  %PREREG_URL%
echo     config.json BASE_URL is already %UI_URL%/proxyapi/
echo     logs %LOG_FILE%
echo.
if exist "%LOG_FILE%" del /q "%LOG_FILE%" >nul 2>&1
if exist "%ERR_FILE%" del /q "%ERR_FILE%" >nul 2>&1
set "LAUNCH_PS1=%UI_DIR%.local\launch-prereg-ui.ps1"
> "%LAUNCH_PS1%" echo $ErrorActionPreference = 'Stop'
>> "%LAUNCH_PS1%" echo $p = Start-Process -FilePath 'node' -ArgumentList @('node_modules\@angular\cli\bin\ng','serve','--proxy-config','proxy.conf.json','--port','%PORT%','--host','0.0.0.0','--disable-host-check') -WorkingDirectory '%UI_DIR%' -RedirectStandardOutput '%LOG_FILE%' -RedirectStandardError '%ERR_FILE%' -WindowStyle Hidden -PassThru
>> "%LAUNCH_PS1%" echo Set-Content -LiteralPath '%PID_FILE%' -Value $p.Id -Encoding ascii
powershell -NoProfile -ExecutionPolicy Bypass -File "%LAUNCH_PS1%"
if errorlevel 1 (
  echo error: failed to start ng serve
  exit /b 1
)
echo ==^> waiting for compile on port %PORT% ^(up to 120s^) ...
call :wait_ready 120
if errorlevel 1 exit /b 1
call :pid_on_port
echo ready  pid=!PORT_PID!  %UI_URL%
echo     pdfjs "Can't resolve zlib" is expected and does not block the UI.
echo     Stop: %~nx0 stop
call :open_browser
exit /b 0

:open_browser
echo.
echo Open %UI_URL%
start "" "%UI_URL%"
exit /b 0

:wait_ready
set /a WAIT_MAX=%~1
set /a WAIT_S=0
:wait_ready_loop
if !WAIT_S! GEQ %WAIT_MAX% (
  echo error: ng serve did not become ready in %WAIT_MAX%s. See %LOG_FILE% and %ERR_FILE%
  exit /b 1
)
findstr /C:"Compiled with warnings" /C:"Compiled successfully" "%LOG_FILE%" "%ERR_FILE%" >nul 2>&1
if not errorlevel 1 exit /b 0
findstr /C:"ERR_OSSL" /C:"error:0308010C" "%LOG_FILE%" "%ERR_FILE%" >nul 2>&1
if not errorlevel 1 (
  echo error: webpack OpenSSL failure. See %LOG_FILE% and %ERR_FILE%
  exit /b 1
)
call :pid_on_port
if defined PORT_PID (
  curl.exe -sS -o NUL -w "%%{http_code}" --connect-timeout 2 --max-time 3 "%UI_URL%" 2>nul | findstr /R /C:"^200$" >nul 2>&1
  if not errorlevel 1 exit /b 0
)
ping -n 3 127.0.0.1 >nul
set /a WAIT_S+=2
echo     ... compiling ^(!WAIT_S!s / %WAIT_MAX%s^)
goto :wait_ready_loop

:pid_on_port
set "PORT_PID="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING"') do set "PORT_PID=%%P"
exit /b 0

:stop
call :ensure_dirs
call :pid_on_port
if not defined PORT_PID (
  echo pre-registration-ui is not running
  if exist "%PID_FILE%" del /q "%PID_FILE%" >nul 2>&1
  exit /b 0
)
echo ==^> stopping pre-registration-ui ^(%PORT_PID%^) on port %PORT%
taskkill /PID %PORT_PID% /T /F >nul 2>&1
if exist "%PID_FILE%" del /q "%PID_FILE%" >nul 2>&1
echo stopped.
exit /b 0

:smoke
echo ==^> GET %UI_URL%
set "HTTP_CODE=000"
set "HTTP_CODE_FILE=%UI_DIR%.local\http-code.txt"
if not exist "%UI_DIR%.local" mkdir "%UI_DIR%.local"
curl.exe -sS -o NUL -w "%%{http_code}" --connect-timeout 2 --max-time 3 "%UI_URL%" > "%HTTP_CODE_FILE%" 2>nul
if exist "%HTTP_CODE_FILE%" (
  for /f "usebackq delims=" %%C in ("%HTTP_CODE_FILE%") do set "HTTP_CODE=%%C"
  del /q "%HTTP_CODE_FILE%" >nul 2>&1
)
if "%HTTP_CODE%"=="200" (
  echo healthy  %UI_URL%  HTTP %HTTP_CODE%
  echo smoke ok
  exit /b 0
)
echo error: UI not reachable %UI_URL%  HTTP %HTTP_CODE%
echo        Start it: %~nx0 start
exit /b 1
