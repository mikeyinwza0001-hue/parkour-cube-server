@echo off
echo Starting Overlay Server...
start /b cmd /c "cd plugins\Skript\scripts\cp_overlay && node server.js"

echo Waiting for Overlay Server to start...
timeout /t 3 /nobreak >nul

echo Starting Minecraft Server...
java -Xms2G -Xmx2G -jar paper.jar nogui
pause