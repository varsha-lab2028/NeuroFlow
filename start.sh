#!/usr/bin/env bash
# NeuroFlow — Start both servers
# Requires: Java 21+, Maven 3.9+, Python 3.10+

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo ""
echo "╔══════════════════════════════════════╗"
echo "║         NeuroFlow App Launcher        ║"
echo "╚══════════════════════════════════════╝"
echo ""

# ── Java backend ─────────────────────────────────────────────────────────────
echo "[1/2] Starting Java Spring Boot backend on :8080..."
cd "$ROOT/java_backend"

mvn -q spring-boot:run \
    -Dspring-boot.run.arguments="--app.db.path=neuroflow.db" &
JAVA_PID=$!
echo "      Java PID: $JAVA_PID"

echo "      Waiting for backend..."
for i in $(seq 1 45); do
  if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "      ✓ Backend ready!"
    break
  fi
  sleep 1
done

# ── Flask frontend ────────────────────────────────────────────────────────────
echo ""
echo "[2/2] Starting Flask frontend on :5001..."
cd "$ROOT/flask_frontend"

pip3 install -r requirements.txt -q 2>/dev/null || pip install -r requirements.txt -q

JAVA_BACKEND_URL=http://localhost:8080 python3 app.py &
FLASK_PID=$!
echo "      Flask PID: $FLASK_PID"

echo ""
echo "══════════════════════════════════════════"
echo "  🌐 App running at: http://localhost:5001"
echo "  🔧 API running at: http://localhost:8080"
echo ""
echo "  Demo PINs:"
echo "    Parent:   1234"
echo "    Educator: 5678"
echo ""
echo "  Press Ctrl+C to stop all servers"
echo "══════════════════════════════════════════"

trap "echo ''; echo 'Shutting down...'; kill $JAVA_PID $FLASK_PID 2>/dev/null; exit 0" INT TERM
wait