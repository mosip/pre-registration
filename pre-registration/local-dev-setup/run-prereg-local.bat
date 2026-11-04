@echo off
setlocal EnableExtensions EnableDelayedExpansion
REM Windows cmd helper for consolidated Pre-Registration local stack.
REM Git Bash: use run-prereg-local.sh

set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
set "MAVEN_PARENT=%SCRIPT_DIR%\.."
set "SERVICE_DIR=%MAVEN_PARENT%\pre-registration-service"
set "LOADER_PATH=%SCRIPT_DIR%\deps\additional_jars"
if not defined PORT set "PORT=9090"
if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=default,local"
if not defined SPRING_CLOUD_CONFIG_URI set "SPRING_CLOUD_CONFIG_URI=http://localhost:51100/config"
if not defined loader_path_env set "loader_path_env=%LOADER_PATH%"
set "CONTEXT=/preregistration/v1"
set "BASE=http://127.0.0.1:%PORT%%CONTEXT%"
set "LOCAL_DIR=%SCRIPT_DIR%\.local"

set "CMD=%~1"
if "%CMD%"=="" goto :help
if /I "%CMD%"=="help" goto :help
if /I "%CMD%"=="-h" goto :help
if /I "%CMD%"=="up" goto :up
if /I "%CMD%"=="prep" goto :prep
if /I "%CMD%"=="run" goto :run
if /I "%CMD%"=="start" goto :start
if /I "%CMD%"=="stop" goto :stop
if /I "%CMD%"=="smoke" goto :smoke
if /I "%CMD%"=="wipe" goto :wipe
if /I "%CMD%"=="wipe-run" goto :wiperun
if /I "%CMD%"=="test" goto :test
echo unknown command '%CMD%'
goto :help

:help
echo %~nx0 [command]
echo   up          Start Docker deps (also runs prep)
echo   prep        Fetch loader jars (idobjectvalidator + virusscanner)
echo   run         Rebuild + start host JVM
echo   start       Start existing jar
echo   stop        Stop host JVM
echo   smoke       Health + swagger
echo   wipe        compose down -v then up
echo   wipe-run    wipe then rebuild + start
echo   test        mvn test
exit /b 0

:prep
set "GIT_BASH=%ProgramFiles%\Git\bin\bash.exe"
if exist "%GIT_BASH%" (
  "%GIT_BASH%" "%SCRIPT_DIR%\run-local-stack.sh" prep
  exit /b %ERRORLEVEL%
)
where bash >nul 2>&1
if not errorlevel 1 (
  bash "%SCRIPT_DIR%\run-local-stack.sh" prep
  exit /b %ERRORLEVEL%
)
echo ERROR: bash is required for prep ^(Git Bash / WSL^).
echo Run: bash run-local-stack.sh prep
echo See deps\additional_jars\README.md
exit /b 1

:up
call :prep
if errorlevel 1 exit /b 1
docker compose -f "%SCRIPT_DIR%\deps\docker-compose.yml" up -d --build
exit /b %ERRORLEVEL%

:stop
call "%SERVICE_DIR%\run-local.bat" stop
exit /b %ERRORLEVEL%

:start
if not exist "%loader_path_env%\kernel-ref-idobjectvalidator.jar" goto :missing_jars
if not exist "%loader_path_env%\kernel-virusscanner-clamav.jar" goto :missing_jars
set "SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%"
set "SPRING_CLOUD_CONFIG_URI=%SPRING_CLOUD_CONFIG_URI%"
set "loader_path_env=%loader_path_env%"
set "LOCAL_DIR=%LOCAL_DIR%"
echo Logs: %LOCAL_DIR%\logs
call "%SERVICE_DIR%\run-local.bat" start
exit /b %ERRORLEVEL%

:missing_jars
echo ERROR: missing loader.path jars under %loader_path_env%
echo kernel-ref-idobjectvalidator and kernel-virusscanner-clamav are not Maven dependencies.
echo Run: %~nx0 prep
echo See deps\additional_jars\README.md
exit /b 1

:run
call :up
if errorlevel 1 exit /b 1
pushd "%MAVEN_PARENT%"
set "SAVED=%SPRING_PROFILES_ACTIVE%"
set "SPRING_PROFILES_ACTIVE="
call mvn -pl pre-registration-service -am clean package -DskipTests "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
set "SPRING_PROFILES_ACTIVE=%SAVED%"
popd
if not "%RC%"=="0" exit /b %RC%
call :start
exit /b %ERRORLEVEL%

:smoke
call "%SERVICE_DIR%\run-local.bat" smoke
exit /b %ERRORLEVEL%

:wipe
call :stop
docker compose -f "%SCRIPT_DIR%\deps\docker-compose.yml" down -v
call :up
exit /b %ERRORLEVEL%

:wiperun
call :wipe
if errorlevel 1 exit /b 1
call :run
exit /b %ERRORLEVEL%

:test
pushd "%MAVEN_PARENT%"
set "SAVED=%SPRING_PROFILES_ACTIVE%"
set "SPRING_PROFILES_ACTIVE="
call mvn -pl pre-registration-service -am test "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
set "SPRING_PROFILES_ACTIVE=%SAVED%"
popd
exit /b %RC%
