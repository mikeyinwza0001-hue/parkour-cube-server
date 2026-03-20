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
    npm install express socket.io tail
    
    if %ERRORLEVEL% neq 0 (
        echo.
        echo [!] ERROR: Failed to install dependencies.
        echo [!] Please make sure Node.js is installed or bundle node_modules.
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

