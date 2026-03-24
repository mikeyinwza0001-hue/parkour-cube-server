const http = require('http');

const cp = process.argv[2];
const max = process.argv[3];

if (!cp || !max) {
    console.error("Usage: node send.js <cp> <max>");
    process.exit(1);
}

const req = http.request({
    hostname: 'localhost',
    port: 6969,
    path: `/update?cp=${cp}&max=${max}`,
    method: 'GET'
}, (res) => {
    res.on('data', () => { });
    res.on('end', () => { });
});

req.on('error', (e) => { });
req.end();
