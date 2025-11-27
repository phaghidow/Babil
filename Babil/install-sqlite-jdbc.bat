@echo off
setlocal enabledelayedexpansion

REM Script to download SQLite JDBC driver
echo Downloading SQLite JDBC driver...

set LIB_DIR=lib
set JAR_NAME=sqlite-jdbc-3.51.0.0.jar
set JAR_PATH=%LIB_DIR%\%JAR_NAME%

if not exist %LIB_DIR% (
    mkdir %LIB_DIR%
    echo Created lib directory
)

REM Check if JAR already exists
if exist %JAR_PATH% (
    echo %JAR_NAME% already exists
    goto :eof
)

REM Download using PowerShell (more reliable on Windows)
powershell -Command ^
  "$ProgressPreference = 'SilentlyContinue'; ^
   Invoke-WebRequest -Uri 'https://github.com/xerial/sqlite-jdbc/releases/download/3.51.0.0/sqlite-jdbc-3.51.0.0.jar' ^
   -OutFile '%JAR_PATH%'; ^
   Write-Host 'Downloaded %JAR_NAME% successfully'"

if exist %JAR_PATH% (
    echo.
    echo [OK] SQLite JDBC driver installed successfully
    for /f "tokens=*" %%a in ('dir /s /b %JAR_PATH%') do (
        for %%b in ("%%a") do set size=%%~zb
        echo File size: !size! bytes
    )
) else (
    echo [ERROR] Failed to download SQLite JDBC driver
    exit /b 1
)
