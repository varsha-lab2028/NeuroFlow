#!/usr/bin/env bash
# NeuroFlow — Start backend (Java :8080) + frontend (React :5173)
# Requires: Java 17+, Maven 3.9+, Node 18+
ROOT="$(cd "$(dirname "$0")" && pwd)"

echo ""
echo "╔══════════════════════════════════════╗"
echo "║        NeuroFlow App Launcher         ║"
echo "╚══════════════════════════════════════╝"

echo "[1/2] Starting Java Spring Boot backend on :8080..."
cd "$ROOT/java_backend"
mvn -q spring-boot:run &
JAVA_PID=$!

echo "      Waiting for backend..."
for i in $(seq 1 60); do
  if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "      ✓ Backend ready!"
    break
  fi
  sleep 1
done

echo "[2/2] Starting React frontend on :5173..."
cd "$ROOT/neuroflow-frontend"
[ -d node_modules ] || npm install
npm run dev &
VITE_PID=$!

echo ""
echo "══════════════════════════════════════════"
echo "  🌐 App:      http://localhost:5173"
echo "  🔧 API:      http://localhost:8080/api/health"
echo "  🐍 ML:       python hardwre_recog.py   (run separately, needs Arduino)"
echo ""
echo "  Demo PINs — Parent: 1234   Educator: 5678"
echo "  Press Ctrl+C to stop both servers"
echo "══════════════════════════════════════════"

trap "echo ''; echo 'Shutting down...'; kill $JAVA_PID $VITE_PID 2>/dev/null; exit 0" INT TERM
wait
