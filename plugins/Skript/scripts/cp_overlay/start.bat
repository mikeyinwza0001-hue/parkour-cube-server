@echo off
cls
echo =======================================
echo  Checkpoint Overlay Server
echo =======================================

:: ─── Node Configuration ──────────────────────────────────────────────────────
set NODE_EXE=node
if exist "node.exe" (
    set NODE_EXE="node.exe"
    echo [!] Using bundled Node.js...
)

:: ─── Dependency Check ────────────────────────────────────────────────────────
if not exist node_modules\express (
    echo [!] node_modules missing. Attempting to install...
    where npm >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        npm install express socket.io
    ) else if exist "node.exe" (
        echo [!] npm not found, trying bundled node...
        "node.exe" -e "const{execSync}=require('child_process');execSync('node.exe \"'+process.execPath.replace(/node\.exe$/,'')+'node_modules/npm/bin/npm-cli.js\" install express socket.io',{stdio:'inherit',cwd:__dirname})"
    )
    if not exist node_modules\express (
        echo.
        echo [!] ERROR: Failed to install dependencies.
        echo [!] Please pre-install node_modules before distribution.
        pause
        exit /b 1
    )
)

echo.
echo [!] Starting Server...
%NODE_EXE% server.js

if %ERRORLEVEL% neq 0 (
    echo.
    echo [!] Overlay server failed to start.
    pause
)

