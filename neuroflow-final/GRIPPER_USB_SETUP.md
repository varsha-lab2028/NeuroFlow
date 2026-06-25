# Connecting the Smart Gripper via USB

## What the gripper sends
The Arduino on the gripper sends 6 comma-separated values over USB serial at 115200 baud:
```
ax,ay,az,gx,gy,gz
```
These are accelerometer (ax/ay/az) and gyroscope (gx/gy/gz) readings from the MPU-6050.

---

## Step 1 — Find your serial port (Mac)

Plug the gripper into your MacBook via USB. Then run:
```bash
ls /dev/cu.*
```
You'll see something like:
```
/dev/cu.usbserial-110
/dev/cu.usbmodem1101
/dev/cu.SLAB_USBtoUART
```
The right one is usually `cu.usbserial-*` or `cu.usbmodem-*`.

---

## Step 2 — Set the port in the Python script

Open `hardwre_recog.py` and find line ~34:
```python
SERIAL_PORT = os.environ.get("NEUROFLOW_SERIAL_PORT", "COM16")
```
Change `COM16` to your Mac port, e.g.:
```python
SERIAL_PORT = os.environ.get("NEUROFLOW_SERIAL_PORT", "/dev/cu.usbserial-110")
```

Or set it without touching the file (recommended):
```bash
export NEUROFLOW_SERIAL_PORT=/dev/cu.usbserial-110
python3 hardwre_recog.py
```

---

## Step 3 — Install Python dependencies

```bash
pip3 install tensorflow requests pyserial
```

If TensorFlow is too heavy (takes long to install), install the lite version:
```bash
pip3 install tensorflow-macos tensorflow-metal   # M1/M2 Mac
```

---

## Step 4 — Run the full stack

**Terminal 1 — Java backend:**
```bash
cd "/Users/dishakukkal/Downloads/NeuroFlow 4/java_backend"
mvn spring-boot:run
```
Wait for: `Started NeuroFlowApplication`

**Terminal 2 — React frontend:**
```bash
cd "/Users/dishakukkal/Downloads/NeuroFlow 4/neuroflow-frontend"
npm run dev
```

**Terminal 3 — Python + gripper:**
```bash
cd "/Users/dishakukkal/Downloads/NeuroFlow 4"
export NEUROFLOW_SERIAL_PORT=/dev/cu.usbserial-110   # your port
python3 hardwre_recog.py
```

You'll see:
```
✅ Model loaded successfully
✅ Labels loaded: ['0', '1', '2', ..., '9', 'NONE']
✅ Connected to /dev/cu.usbserial-110
```

Then open `http://localhost:5173` → Numeracy → pick a digit → Try.

---

## How the data flows

```
Gripper (Arduino MPU-6050)
    ↓ USB serial (115200 baud)
hardwre_recog.py
    ↓ reads motion data, runs TensorFlow model
    ↓ POST /api/digit-result  or  /api/none-event
Java Spring Boot (:8080)
    ↓ saves to SQLite (neuroflow.db)
React frontend (:5173)
    ↑ polls /api/digit-latest every 1.5s
    ← updates UI automatically
```

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| `could not open port /dev/cu.xxx` | Wrong port — re-run `ls /dev/cu.*` with gripper plugged in |
| `permission denied /dev/cu.xxx` | Run `sudo chmod 666 /dev/cu.usbserial-110` |
| `No module named serial` | Run `pip3 install pyserial` |
| `No module named tensorflow` | Run `pip3 install tensorflow` (or tensorflow-macos on M1/M2) |
| Python script opens but no motion detected | Check Arduino is sending data: `screen /dev/cu.usbserial-110 115200` — you should see numbers scrolling |
| React doesn't update after recognition | Make sure Java backend is running on :8080 first |
| `java: command not found` in Maven | Install Java 17: `brew install openjdk@17` |

---

## Demo without hardware (use Simulate button)

If the gripper isn't available, go to Numeracy → pick digit → Try screen.
Click **"Simulate ML result → (demo)"**. 

This calls the exact same backend endpoints as the real hardware — the data flow through Java → SQLite → React is 100% real. Only the gesture capture is mocked.
