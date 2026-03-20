# ═══════════════════════════════════════════════════════
# Parkour Cube Server — Release Packaging Script
# ═══════════════════════════════════════════════════════
# Usage: .\build-release.ps1
# Creates a clean zip for distribution (no sensitive data)

param(
    [string]$OutputDir = ".\release"
)

$ErrorActionPreference = "Stop"

# Read version
$versionFile = Get-Content ".\server-version.json" | ConvertFrom-Json
$version = $versionFile.version
$zipName = "mabel-parkour-cube-server-v$version.zip"

Write-Host "=== Building Parkour Cube release v$version ===" -ForegroundColor Cyan

# Clean output dir
if (Test-Path $OutputDir) { Remove-Item $OutputDir -Recurse -Force }
New-Item -ItemType Directory -Path $OutputDir | Out-Null

$tempDir = "$OutputDir\parkour-cube-server"
New-Item -ItemType Directory -Path $tempDir | Out-Null

# ─── Files/Folders to EXCLUDE from release ────────────
$excludeFiles = @(
    "ops.json",
    "whitelist.json",
    "banned-players.json",
    "banned-ips.json",
    "usercache.json",
    ".console_history",
    "build-release.ps1",
    ".gitignore",
    ".gitattributes",
    "paper.jar",
    "mabel_folders.txt",
    "mabel_installed.txt",
    "mblt_files.txt",
    "mblt_root.txt",
    "parkour_assets.txt",
    "release_assets.txt",
    "remote_info.txt"
)

$excludeDirs = @(
    ".git",
    ".vscode",
    "logs",
    "crash-reports",
    "cache",
    "versions",
    "libraries",
    "release",
    "plugins\.paper-remapped",
    "plugins\bStats",
    "plugins\spark"
)

# ─── Ensure overlay dependencies are installed ────────
$overlayDir = ".\plugins\Skript\scripts\cp_overlay"
if (Test-Path "$overlayDir\package.json") {
    Write-Host "Installing CP overlay dependencies..." -ForegroundColor Yellow
    Push-Location $overlayDir
    $oldEAP = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    npm install --omit=dev 2>&1 | Out-Null
    $ErrorActionPreference = $oldEAP
    Pop-Location
}

# ─── Copy everything first ────────────────────────────
Write-Host "Copying server files..." -ForegroundColor Yellow

$allItems = Get-ChildItem -Path "." -Force
foreach ($item in $allItems) {
    $name = $item.Name
    $skip = $false

    # Skip excluded files
    if ($excludeFiles -contains $name) { $skip = $true }

    # Skip zip files
    if ($name -match '\.zip$') { $skip = $true }

    # Skip excluded directories
    foreach ($dir in $excludeDirs) {
        if ($name -eq $dir -or $name -eq $dir.Split("\")[-1]) { $skip = $true; break }
    }

    if (-not $skip) {
        if ($item.PSIsContainer) {
            Copy-Item -Path $item.FullName -Destination "$tempDir\$name" -Recurse -Force
        } else {
            Copy-Item -Path $item.FullName -Destination "$tempDir\$name" -Force
        }
    }
}

# ─── Create empty required files ──────────────────────
"[]" | Set-Content "$tempDir\ops.json"
"[]" | Set-Content "$tempDir\whitelist.json"
"[]" | Set-Content "$tempDir\banned-players.json"
"[]" | Set-Content "$tempDir\banned-ips.json"

# ─── Create zip ───────────────────────────────────────
Write-Host "Creating $zipName..." -ForegroundColor Yellow
$zipPath = "$OutputDir\$zipName"
# Compress-Archive -Path "$tempDir\*" -DestinationPath $zipPath -Force
tar -acf $zipPath -C $tempDir .

# Cleanup temp
Remove-Item $tempDir -Recurse -Force

$size = [math]::Round((Get-Item $zipPath).Length / 1MB, 1)
Write-Host ""
Write-Host "=== Release built! ===" -ForegroundColor Green
Write-Host "  File: $zipPath" -ForegroundColor White
Write-Host "  Size: ${size} MB" -ForegroundColor White
Write-Host "  Version: $version" -ForegroundColor White
Write-Host ""
Write-Host "Next: Upload $zipName to GitHub Releases" -ForegroundColor Cyan
