# Windows PowerShell Launcher for Ginininini Backend
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$BackendDir = Join-Path $ProjectRoot "backend"

Set-Location $BackendDir

Write-Host "==> Checking Python Environment in $BackendDir..." -ForegroundColor Cyan

$VenvDir = Join-Path $BackendDir "venv"
$PythonExe = Join-Path $VenvDir "Scripts\python.exe"
$UvicornExe = Join-Path $VenvDir "Scripts\uvicorn.exe"

if (-not (Test-Path $PythonExe)) {
    Write-Host "Creating virtual environment at $VenvDir..." -ForegroundColor Yellow
    python -m venv $VenvDir
    & $PythonExe -m pip install --upgrade pip
    & $PythonExe -m pip install -r (Join-Path $BackendDir "requirements.txt")
}

if (-not (Test-Path (Join-Path $BackendDir ".env"))) {
    Write-Host "Warning: No .env file found in $BackendDir." -ForegroundColor Yellow
    Write-Host "Copying .env.example to .env..." -ForegroundColor Yellow
    Copy-Item (Join-Path $BackendDir ".env.example") (Join-Path $BackendDir ".env")
    Write-Host "Please configure .env before starting production runs!" -ForegroundColor Magenta
}

Write-Host "==> Starting Ginininini FastAPI Backend on 0.0.0.0:8000..." -ForegroundColor Green
& $UvicornExe app.main:app --host 0.0.0.0 --port 8000 --reload
