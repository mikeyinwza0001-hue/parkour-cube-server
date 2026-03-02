const express = require('express');
const app = express();
const http = require('http').createServer(app);
const io = require('socket.io')(http);
const path = require('path');

const PORT = 6969;

// Current state
let currentCp = 0;
let maxCp = 60;

// Serve the overlay HTML file
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'overlay.html'));
});

const fs = require('fs');

// Path to the log file Skript will generate
const LOG_FILE = path.join(__dirname, '../../../../plugins/Skript/logs/cp_overlay_data.log');

// Setup file watcher
if (!fs.existsSync(LOG_FILE)) {
    // Create empty file if it doesn't exist to prevent crash
    const dir = path.dirname(LOG_FILE);
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    fs.writeFileSync(LOG_FILE, '');
}

console.log(`[Watcher] Listening for changes in: ${LOG_FILE}`);

let lastKnownContent = "";

setInterval(() => {
    try {
        const content = fs.readFileSync(LOG_FILE, 'utf8').trim();
        if (content && content !== lastKnownContent) {

            // Extracted from Skript: "15/60" is logged.
            // But sometimes the log has timestamps like: [01:27:07 INFO]: 15/60
            const lines = content.split('\n');
            const lastLine = lines[lines.length - 1];

            // Extract numbers matching <number>/<number> exactly at the end of the line
            const match = lastLine.match(/(\d+)\/(\d+)$/);
            if (match) {
                currentCp = parseInt(match[1]);
                maxCp = parseInt(match[2]);

                console.log(`[Update] Checkpoint changed: ${currentCp} / ${maxCp}`);
                io.emit('cp_update', { current: currentCp, max: maxCp });
                lastKnownContent = content;
            }
        }
    } catch (e) {
        // Ignore read errors (file might be locked by Minecraft temporarily)
    }
}, 500); // Check every half second

// On OBS connect, send current state
io.on('connection', (socket) => {
    console.log('OBS Overlay Connected!');
    socket.emit('cp_update', { current: currentCp, max: maxCp });
});

http.listen(PORT, () => {
    console.log('===================================================');
    console.log(`[CP Overlay] Server is running on http://localhost:${PORT}`);
    console.log(`Add this URL as a Browser Source in OBS!`);
    console.log('===================================================');
});
