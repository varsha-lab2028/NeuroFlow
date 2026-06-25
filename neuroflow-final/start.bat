@echo off
echo Starting NeuroFlow...

echo.
echo [1/2] Starting Java Backend + ML on port 8080...
start "NeuroFlow Backend" cmd /k "cd java_backend && mvn spring-boot:run"

echo Waiting for backend to start...
timeout /t 15 /nobreak > nul

echo.
echo [2/2] Starting React Frontend on port 5173...
start "NeuroFlow Frontend" cmd /k "cd neuroflow-frontend && npm run dev"

echo.
echo NeuroFlow is starting up!
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo.
echo Both windows will open automatically.
pause