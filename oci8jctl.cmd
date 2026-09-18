@echo off
REM
REM oci8j-connector - Oracle 8i REST API
REM Copyright (c) 2024 - 2026 Hermes Rodríguez
REM SPDX-License-Identifier: MIT
REM
REM oci8jctl.cmd - Windows helper for oci8j-connector
REM
setlocal EnableExtensions EnableDelayedExpansion

set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"

REM Resolve version: VERSION env, else VERSION file, else pom.xml, else 0.0.0
if defined VERSION (
  set "IMG_VERSION=%VERSION%"
) else if exist "%PROJECT_ROOT%\VERSION" (
  set /p IMG_VERSION=<"%PROJECT_ROOT%\VERSION"
) else (
  set "IMG_VERSION=0.0.0"
  if exist "%PROJECT_ROOT%\pom.xml" (
    for /f "usebackq tokens=2 delims=<>" %%A in (`findstr /n /c:"<version>" "%PROJECT_ROOT%\pom.xml"`) do (
      if not defined _ver_done (
        set "IMG_VERSION=%%A"
        set "_ver_done=1"
      )
    )
  )
)

set "CMD=%~1"
if "%CMD%"=="" set "CMD=help"

if /i "%CMD%"=="help" goto :help
if /i "%CMD%"=="-h" goto :help
if /i "%CMD%"=="--help" goto :help
if /i "%CMD%"=="build" goto :build
if /i "%CMD%"=="build-arm64" goto :build_arm64
if /i "%CMD%"=="build-amd64" goto :build_amd64
if /i "%CMD%"=="build-multi" goto :build_multi
if /i "%CMD%"=="generate-build-info" goto :generate_build_info
if /i "%CMD%"=="up" goto :up
if /i "%CMD%"=="down" goto :down
if /i "%CMD%"=="logs" goto :logs
if /i "%CMD%"=="health" goto :health
if /i "%CMD%"=="query" goto :query

echo Unknown command: %CMD%
call :usage
exit /b 1

:help
call :usage
echo.
echo Current version: %IMG_VERSION%
echo Override with: set VERSION=x.y.z ^&^& oci8jctl.cmd build
exit /b 0

:usage
echo Usage: oci8jctl.cmd ^<command^>
echo.
echo Commands:
echo   help                  Show this help message
echo   build                 Build Docker image (host platform)
echo   build-arm64           Build Docker image for linux/arm64
echo   build-amd64           Build Docker image for linux/amd64
echo   build-multi           Build multi-arch OCI image archive (no push)
echo   generate-build-info   Generate build-info.properties file
echo   up                    Start the service
echo   down                  Stop the service
echo   logs                  View service logs
echo   health                Check health status
echo   query                 Execute test query (use QUERY env var)
echo.
echo Environment:
echo   VERSION               Override image version (default: VERSION file, else pom.xml)
echo   QUERY                 SQL query for "query" command
echo.
echo Examples:
echo   oci8jctl.cmd build
echo   set VERSION=1.2.3 ^&^& oci8jctl.cmd build
echo   set QUERY=SELECT 1 FROM DUAL ^&^& oci8jctl.cmd query
exit /b 0

:build
echo Building oci8j-connector image v%IMG_VERSION% (host platform)...
docker buildx build --load --no-cache -f "%PROJECT_ROOT%\docker\Dockerfile" -t "oci8j-connector:v%IMG_VERSION%" -t "oci8j-connector:latest" "%PROJECT_ROOT%"
if errorlevel 1 exit /b 1
echo Image built successfully: oci8j-connector:v%IMG_VERSION% and oci8j-connector:latest
exit /b 0

:build_arm64
echo Building oci8j-connector image v%IMG_VERSION% (linux/arm64)...
docker buildx build --load --no-cache --platform linux/arm64 -f "%PROJECT_ROOT%\docker\Dockerfile" -t "oci8j-connector:v%IMG_VERSION%-arm64" -t "oci8j-connector:latest-arm64" "%PROJECT_ROOT%"
if errorlevel 1 exit /b 1
echo Image built successfully: oci8j-connector:v%IMG_VERSION%-arm64 and oci8j-connector:latest-arm64
exit /b 0

:build_amd64
echo Building oci8j-connector image v%IMG_VERSION% (linux/amd64)...
docker buildx build --load --no-cache --platform linux/amd64 -f "%PROJECT_ROOT%\docker\Dockerfile" -t "oci8j-connector:v%IMG_VERSION%-amd64" -t "oci8j-connector:latest-amd64" "%PROJECT_ROOT%"
if errorlevel 1 exit /b 1
echo Image built successfully: oci8j-connector:v%IMG_VERSION%-amd64 and oci8j-connector:latest-amd64
exit /b 0

:build_multi
echo Building multi-arch OCI image v%IMG_VERSION% (linux/amd64,linux/arm64)...
if not exist "%PROJECT_ROOT%\dist" mkdir "%PROJECT_ROOT%\dist"
docker buildx build --no-cache --platform linux/amd64,linux/arm64 -f "%PROJECT_ROOT%\docker\Dockerfile" -t "oci8j-connector:v%IMG_VERSION%" -t "oci8j-connector:latest" --output "type=oci,dest=%PROJECT_ROOT%\dist\oci8j-connector-%IMG_VERSION%.oci.tar" "%PROJECT_ROOT%"
if errorlevel 1 exit /b 1
echo OCI image archive created: dist\oci8j-connector-%IMG_VERSION%.oci.tar
exit /b 0

:generate_build_info
echo Generating build information...
call "%PROJECT_ROOT%\scripts\generate-build-info.cmd"
exit /b %ERRORLEVEL%

:up
echo Starting oci8j-connector...
docker compose -f "%PROJECT_ROOT%\docker\docker-compose.yml" up -d
exit /b %ERRORLEVEL%

:down
echo Stopping oci8j-connector...
docker compose -f "%PROJECT_ROOT%\docker\docker-compose.yml" down
exit /b %ERRORLEVEL%

:logs
docker compose -f "%PROJECT_ROOT%\docker\docker-compose.yml" logs -f
exit /b %ERRORLEVEL%

:health
echo Checking health status...
curl -f http://localhost:8080/api/v1/oci8j-connector/healthz
if errorlevel 1 echo Health check failed
exit /b %ERRORLEVEL%

:query
if not defined QUERY (
  echo Error: You must provide a query
  echo    Usage: set QUERY=SELECT 1 FROM DUAL ^&^& oci8jctl.cmd query
  exit /b 1
)
echo Executing query: %QUERY%
curl -X POST http://localhost:8080/api/v1/oci8j-connector/query -H "Content-Type: application/json" -d "{\"query\": \"%QUERY%\"}"
exit /b %ERRORLEVEL%
