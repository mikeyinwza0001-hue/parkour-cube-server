@echo off
echo Starting Overlay Server...
start /b cmd /c "cd plugins\Skript\scripts\cp_overlay && node server.js"

echo Starting Minecraft Server...
java -Xms2G -Xmx2G -jar paper-1.21.10-130.jar nogui
pause