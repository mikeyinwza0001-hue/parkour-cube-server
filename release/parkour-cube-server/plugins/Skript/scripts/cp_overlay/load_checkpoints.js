/**
 * Fetch checkpoint data from API and generate Skript file
 * 
 * This runs BEFORE the MC server starts.
 * It fetches checkpoint data from the overlay API and generates
 * a .sk file that loads variables into Skript on server start.
 * 
 * Usage: node load_checkpoints.js
 * Env:   CP_API_KEY (must match server.js)
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const API_KEY = process.env.CP_API_KEY || 'CHANGE_ME_TO_A_SECRET_KEY';
const API_URL = `http://localhost:6969/api/checkpoints?key=${encodeURIComponent(API_KEY)}`;
const OUTPUT_SK = path.join(__dirname, '..', '_loaded_checkpoints.sk');

function fetchJSON(url) {
    return new Promise((resolve, reject) => {
        const req = http.get(url, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                if (res.statusCode !== 200) {
                    reject(new Error(`API returned ${res.statusCode}: ${body}`));
                    return;
                }
                try {
                    resolve(JSON.parse(body));
                } catch (e) {
                    reject(new Error(`Invalid JSON: ${e.message}`));
                }
            });
        });
        req.on('error', reject);
        req.setTimeout(5000, () => {
            req.destroy();
            reject(new Error('Request timed out'));
        });
    });
}

function generateSkript(data) {
    const lines = [];
    lines.push('#########################################');
    lines.push('# AUTO-GENERATED - DO NOT EDIT');
    lines.push('# Loaded from API on server start');
    lines.push('#########################################');
    lines.push('');
    lines.push('on load:');

    // Final checkpoint
    if (data.final != null) {
        lines.push(`    set {cp.final} to ${data.final}`);
    }

    // Warps
    if (data.warps) {
        lines.push('');
        lines.push('    # Checkpoint warps');
        const sortedKeys = Object.keys(data.warps).map(Number).sort((a, b) => a - b);
        for (const id of sortedKeys) {
            const w = data.warps[id];
            const worldName = w.world || 'world';
            lines.push(`    set {_loc} to location(${w.x}, ${w.y}, ${w.z}, world "${worldName}")`);
            lines.push(`    set yaw of {_loc} to ${w.yaw}`);
            lines.push(`    set pitch of {_loc} to ${w.pitch}`);
            lines.push(`    set {cp.warp::${id}} to {_loc}`);
        }
    }

    // Beacons
    if (data.beacons) {
        lines.push('');
        lines.push('    # Beacon mappings');
        for (const [locStr, cpNum] of Object.entries(data.beacons)) {
            lines.push(`    set {cp.beacon::${locStr}} to ${cpNum}`);
        }
    }

    lines.push('');
    lines.push('    send "&a[API] Loaded %size of {cp.warp::*}% warps, %size of {cp.beacon::*}% beacons from API" to console');
    lines.push('');

    return lines.join('\n');
}

async function main() {
    console.log('[Loader] Fetching checkpoint data from API...');

    try {
        const data = await fetchJSON(API_URL);

        const warpCount = Object.keys(data.warps || {}).length;
        const beaconCount = Object.keys(data.beacons || {}).length;
        console.log(`[Loader] Received: final=${data.final}, warps=${warpCount}, beacons=${beaconCount}`);

        const skContent = generateSkript(data);
        fs.writeFileSync(OUTPUT_SK, skContent, 'utf8');
        console.log(`[Loader] Generated: ${OUTPUT_SK}`);
        console.log('[Loader] Done! Skript will load checkpoint data on server start.');

    } catch (err) {
        console.error(`[Loader] ERROR: ${err.message}`);

        // If the .sk file already exists from a previous run, keep it as cache
        if (fs.existsSync(OUTPUT_SK)) {
            console.log('[Loader] Using cached _loaded_checkpoints.sk from previous run.');
        } else {
            console.error('[Loader] No cached file found. Checkpoints will NOT be loaded!');
            process.exit(1);
        }
    }
}

main();
