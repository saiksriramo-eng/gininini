@echo off
setlocal
set SCRIPT_DIR=%~dp0
set BACKEND_DIR=%SCRIPT_DIR%..\backend

cd /d "%BACKEND_DIR%"
if not exist "venv\Scripts\python.exe" (
    echo [INFO] Creating Python virtual environment...
    python -m venv venv
    venv\Scripts\python.exe -m pip install --upgrade pip
    venv\Scripts\python.exe -m pip install -r requirements.txt
)

if not exist ".env" (
    echo [WARNING] .env not found. Creating from .env.example...
    copy .env.example .env
)

echo [INFO] Starting Ginininini FastAPI Backend...
venv\Scripts\uvicorn.exe app.main:app --host 0.0.0.0 --port 8000 --reload
