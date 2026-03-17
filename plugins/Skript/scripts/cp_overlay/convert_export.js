/**
 * Convert checkpoint export log to checkpoints.json
 * 
 * Usage:
 *   1. Run /exportcp in-game first
 *   2. Then run: node convert_export.js
 *   3. This creates checkpoints.json for the API
 */

const fs = require('fs');
const path = require('path');

const LOG_FILE = path.join(__dirname, '../../logs/checkpoint_export.log');
const OUTPUT_FILE = path.join(__dirname, 'checkpoints.json');

if (!fs.existsSync(LOG_FILE)) {
    console.error('[Error] Export log not found:', LOG_FILE);
    console.error('Run /exportcp in-game first!');
    process.exit(1);
}

const raw = fs.readFileSync(LOG_FILE, 'utf8');
const lines = raw.split('\n').filter(l => l.trim());

const data = {
    final: 0,
    warps: {},
    beacons: {}
};

for (const line of lines) {
    // Skript log format: [HH:MM:SS] DATA
    // Strip timestamp if present
    const cleaned = line.replace(/^\[.*?\]\s*/, '').trim();
    const parts = cleaned.split('|');

    if (parts[0] === 'FINAL') {
        data.final = parseInt(parts[1]);
    } else if (parts[0] === 'WARP') {
        const id = parseInt(parts[1]);
        data.warps[id] = {
            x: parseFloat(parts[2]),
            y: parseFloat(parts[3]),
            z: parseFloat(parts[4]),
            yaw: parseFloat(parts[5]),
            pitch: parseFloat(parts[6]),
            world: parts[7] || 'world'
        };
    } else if (parts[0] === 'BEACON') {
        // parts[1] = location string like "world:2.5,57.5,39.5"
        // parts[2] = checkpoint number
        data.beacons[parts[1]] = parseInt(parts[2]);
    } else if (parts[0] === 'DONE') {
        console.log('[OK] Found DONE marker');
    }
}

const warpCount = Object.keys(data.warps).length;
const beaconCount = Object.keys(data.beacons).length;

console.log(`[OK] Final CP: ${data.final}`);
console.log(`[OK] Warps: ${warpCount}`);
console.log(`[OK] Beacons: ${beaconCount}`);

fs.writeFileSync(OUTPUT_FILE, JSON.stringify(data, null, 2), 'utf8');
console.log(`[OK] Saved to: ${OUTPUT_FILE}`);
console.log('\n[Next] Start the server - checkpoints will load from API automatically.');
