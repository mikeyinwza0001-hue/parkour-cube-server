@echo off
title Parkour Cube Server

:: ─── Java Configuration ──────────────────────────────────────────────────────
set JAVA_EXE=java
if exist "jre\bin\java.exe" (
    set JAVA_EXE="jre\bin\java.exe"
    echo [!] Using bundled JRE...
)

echo Starting Overlay Server...
start /b cmd /c "cd plugins\Skript\scripts\cp_overlay && start.bat"

echo Waiting for Overlay Server to start...
timeout /t 3 /nobreak >nul

echo Starting Minecraft Server...
%JAVA_EXE% -Xms2G -Xmx2G -jar paper.jar nogui

if %ERRORLEVEL% neq 0 (
    echo.
    echo [!] Server exited with error code %ERRORLEVEL%
    echo [!] Please make sure you have Java 17 or higher installed.
    echo [!] You can download it from: https://adoptium.net/
    pause
)
