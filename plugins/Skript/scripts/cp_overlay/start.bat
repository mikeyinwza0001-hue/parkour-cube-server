@echo off
clm
echo =========================================
echo  Checkpoint Overlay Server
echo =========================================

if not exist node_modules\express (
    echo [!] First time setup: Installing dependencies...
    npm install express socket.io
)

echo.
echo [!] Starting Server...
node server.js
pause
