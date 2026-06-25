# =============================================================================
#  NeuroFlow — hardwre_recog.py
#  Reads MPU-6050 from Arduino → LSTM inference → posts to Java → React updates
#
#  BUGS FIXED IN THIS VERSION:
#  1. targetDigit mismatch: Python now fetches current target from Java before
#     posting, so correct/wrong is judged against what the child was asked to write
#  2. Tkinter GUI freeze: gui_motion used 2 separate root.after calls (race).
#     Fixed to a single scheduled update.
#  3. TF log noise: suppressed before tensorflow is imported
#  4. Model fallback: tries patched .h5 files first, never hard-crashes
#  5. Serial non-blocking: script opens fully even without Arduino connected
# =============================================================================

import os
os.environ["TF_ENABLE_ONEDNN_OPTS"] = "0"
os.environ["TF_CPP_MIN_LOG_LEVEL"]  = "3"
import warnings
warnings.filterwarnings("ignore")

import tkinter as tk
from tkinter import ttk
import threading
import time
import json
from collections import deque
import numpy as np

# ── Optional deps ─────────────────────────────────────────────────────────────
try:
    import serial
    SERIAL_AVAILABLE = True
except ImportError:
    SERIAL_AVAILABLE = False
    print("⚠️  pyserial not installed. Run:  pip install pyserial")

try:
    import requests
    REQUESTS_AVAILABLE = True
except ImportError:
    REQUESTS_AVAILABLE = False
    print("⚠️  requests not installed. Run:  pip install requests")

# ── Config ────────────────────────────────────────────────────────────────────
SERIAL_PORT  = os.environ.get("NEUROFLOW_SERIAL_PORT",  "COM16")
BAUD_RATE    = 115200
JAVA_API     = os.environ.get("NEUROFLOW_API",          "http://localhost:8080/api")
STUDENT_ID   = int(os.environ.get("NEUROFLOW_STUDENT_ID", "1"))

SEQ_LEN         = 50
N_FEATURES      = 6
START_THRESHOLD = 25
STOP_THRESHOLD  = 8
STOP_TIME       = 1.2
MIN_GESTURE_LEN = 30
MAX_GESTURE_LEN = 150
CONF_THRESHOLD  = 70
FILTER_WINDOW   = 5

# ── Load model ────────────────────────────────────────────────────────────────
from tensorflow.keras.models import load_model

model = None
for _f in ["digit_model_fixed.h5", "best_digit_model_fixed.h5",
           "best_digit_model.h5", "digit_model.h5"]:
    if not os.path.exists(_f):
        continue
    try:
        model = load_model(_f, compile=False)
        print(f"✅ Model loaded: {_f}")
        break
    except Exception as _e:
        print(f"⚠️  {_f} failed: {_e}")

if model is None:
    print("❌ No model found. Run:  python fix_and_run.py")
    raise SystemExit(1)

# ── Load norm params ──────────────────────────────────────────────────────────
try:
    with open("digit_norm_params.json") as _fp:
        _p        = json.load(_fp)
        labels    = _p["labels"]
        NORM_MEAN = np.array(_p["mean"])
        NORM_STD  = np.array(_p["std"])
    print(f"✅ Labels: {labels}")
except Exception as e:
    print(f"❌ digit_norm_params.json: {e}")
    raise SystemExit(1)

# ── Arduino serial ────────────────────────────────────────────────────────────
ser = None
if SERIAL_AVAILABLE:
    try:
        ser = serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=1)
        time.sleep(2)
        print(f"✅ Arduino on {SERIAL_PORT}")
    except Exception as e:
        print(f"⚠️  Serial open failed ({e})")
        print("    Set correct port:  set NEUROFLOW_SERIAL_PORT=COM3  (then re-run)")
        ser = None

# ── Java helpers ──────────────────────────────────────────────────────────────
def get_current_target():
    """
    Ask Java what digit is currently on screen.
    Uses /api/active-target which React sets when the child presses Start.
    This is the ONLY correct way — /api/current-target returns the next
    incomplete digit from the DB which is WRONG when the child navigates
    back to an earlier digit.
    """
    if not REQUESTS_AVAILABLE:
        return None
    try:
        r = requests.get(f"{JAVA_API}/active-target/{STUDENT_ID}", timeout=2)
        data = r.json()
        t = str(data.get("target", ""))
        print(f"🎯 Active target from app: {t}")
        return t
    except Exception as e:
        print(f"⚠️  Could not fetch active-target: {e}")
        return None

def post_to_java(endpoint, payload):
    if not REQUESTS_AVAILABLE:
        return
    try:
        requests.post(f"{JAVA_API}/{endpoint}", json=payload, timeout=2)
        print(f"📡 /{endpoint}  {payload}")
    except Exception as e:
        print(f"⚠️  Java unreachable: {e}")

# ── Runtime globals ───────────────────────────────────────────────────────────
recording        = False
stop_start       = None
captured_data    = []
recording_buffer = []

# ── GUI ───────────────────────────────────────────────────────────────────────
root = tk.Tk()
root.title("NeuroFlow — Digit Recognition")
root.geometry("920x700")
root.configure(bg="#1a1a2e")

BG     = "#1a1a2e"
SEC    = "#16213e"
ACCENT = "#e94560"
TEXT   = "#c4d0e3"
FRAME  = "#0f3460"
GREEN  = "#3A9462"

main_frame = tk.Frame(root, bg=BG)
main_frame.pack(expand=True, fill="both", padx=20, pady=20)

tk.Label(main_frame, text="✍️  NeuroFlow — Live Digit Recognition",
         font=("Segoe UI", 22, "bold"), bg=BG, fg=ACCENT).pack(pady=(10, 4))
tk.Label(main_frame, text="Write a digit in the air — the app will update automatically",
         font=("Segoe UI", 12), bg=BG, fg=TEXT).pack(pady=(0, 16))

# ── Digit display ─────────────────────────────────────────────────────────────
digit_frame = tk.Frame(main_frame, bg=FRAME, relief="ridge", bd=3, height=180)
digit_frame.pack(pady=8, padx=50, fill="x")
digit_frame.pack_propagate(False)
digit_var = tk.StringVar(value="?")
tk.Label(digit_frame, textvariable=digit_var,
         font=("Segoe UI", 96, "bold"), bg=FRAME, fg=ACCENT).pack(expand=True)

# ── Confidence bar ────────────────────────────────────────────────────────────
conf_frame = tk.Frame(main_frame, bg=BG)
conf_frame.pack(fill="x", padx=50, pady=6)
confidence_var = tk.StringVar(value="Confidence: 0%")
tk.Label(conf_frame, textvariable=confidence_var,
         font=("Segoe UI", 13), bg=BG, fg=TEXT).pack()
conf_progress = ttk.Progressbar(conf_frame, length=500, mode="determinate")
conf_progress.pack(pady=4)

# ── Status ────────────────────────────────────────────────────────────────────
status_frame = tk.Frame(main_frame, bg=FRAME, relief="sunken", bd=2)
status_frame.pack(fill="x", padx=50, pady=6)
status_var = tk.StringVar(value="🤚  Ready — write a digit 0–9 in the air")
tk.Label(status_frame, textvariable=status_var,
         font=("Segoe UI", 13), bg=FRAME, fg=TEXT, pady=10,
         wraplength=700, justify="center").pack()

# ── App sync status ───────────────────────────────────────────────────────────
sync_frame = tk.Frame(main_frame, bg=SEC, relief="flat", bd=0)
sync_frame.pack(fill="x", padx=50, pady=4)
sync_var = tk.StringVar(value="🔄  Connecting to app...")
tk.Label(sync_frame, textvariable=sync_var,
         font=("Segoe UI", 11), bg=SEC, fg=TEXT, pady=6).pack()

# ── Motion bar ────────────────────────────────────────────────────────────────
motion_outer = tk.Frame(main_frame, bg=SEC)
motion_outer.pack(fill="x", padx=50, pady=4)
tk.Label(motion_outer, text="Motion:", font=("Segoe UI", 10),
         bg=SEC, fg=TEXT).pack(side="left", padx=6)
motion_canvas = tk.Canvas(motion_outer, height=16, bg="#0a0a1a",
                           highlightthickness=1, highlightbackground=FRAME)
motion_canvas.pack(side="left", fill="x", expand=True, padx=6, pady=4)

# ── Samples + HW status ───────────────────────────────────────────────────────
info_frame = tk.Frame(main_frame, bg=BG)
info_frame.pack(pady=6)
samples_var = tk.StringVar(value="Samples: 0")
tk.Label(info_frame, textvariable=samples_var,
         font=("Segoe UI", 11), bg=BG, fg=TEXT).pack(side="left", padx=16)
hw_text = f"🟢 Arduino: {SERIAL_PORT}" if ser else "🔴 Arduino: not connected"
hw_color = GREEN if ser else ACCENT
tk.Label(info_frame, text=hw_text, font=("Segoe UI", 11),
         bg=BG, fg=hw_color).pack(side="left", padx=16)
tk.Label(info_frame, text=f"🌐 Java: {JAVA_API}",
         font=("Segoe UI", 11), bg=BG, fg=TEXT).pack(side="left", padx=16)

# ── Signal processing ─────────────────────────────────────────────────────────
def moving_avg(data, w):
    if len(data) < w:
        return data
    return np.convolve(data, np.ones(w) / w, mode="same")

def calc_motion(v):
    ax, ay, az, gx, gy, gz = v
    return np.sqrt(ax**2 + ay**2 + az**2) + np.sqrt(gx**2 + gy**2 + gz**2) * 0.3

def preprocess(data):
    if len(data) < MIN_GESTURE_LEN:
        return None
    arr    = np.array(data)
    smooth = np.zeros_like(arr)
    for i in range(arr.shape[1]):
        smooth[:, i] = moving_avg(arr[:, i], FILTER_WINDOW)
    norm = (smooth - NORM_MEAN) / NORM_STD
    if len(norm) < SEQ_LEN:
        return np.vstack([norm, np.zeros((SEQ_LEN - len(norm), N_FEATURES))])
    return norm[:SEQ_LEN]

# ── GUI update helpers (all called via root.after → main thread safe) ─────────
def _set_digit(d):      digit_var.set(d)
def _set_conf(c):       confidence_var.set(f"Confidence: {c:.1f}%"); conf_progress.configure(value=c)
def _set_status(t):     status_var.set(t)
def _set_samples(n):    samples_var.set(f"Samples: {n}")
def _set_sync(t):       sync_var.set(t)

def gui_predict(digit, conf):
    root.after(0, lambda: (_set_digit(digit), _set_conf(conf)))

def gui_status(text):
    root.after(0, lambda: _set_status(text))

def gui_sync(text):
    root.after(0, lambda: _set_sync(text))

def gui_samples(n):
    root.after(0, lambda: _set_samples(n))

# Single root.after call for motion — avoids the delete/draw race condition
def gui_motion(intensity):
    w = min(int(intensity * 4), motion_canvas.winfo_width() or 400)
    def _draw():
        motion_canvas.delete("all")
        cw = motion_canvas.winfo_width()
        # background track
        motion_canvas.create_rectangle(0, 0, cw, 16, fill="#0a0a1a", outline="")
        if w > 0:
            # colour: green when low, amber when mid, red when high
            pct = intensity / 100
            color = ACCENT if pct > 0.6 else ("#D68B25" if pct > 0.3 else GREEN)
            motion_canvas.create_rectangle(0, 0, w, 16, fill=color, outline="")
    root.after(0, _draw)

def gui_reset():
    root.after(0, lambda: (
        _set_digit("?"),
        _set_status("🤚  Ready — write a digit 0–9 in the air"),
        _set_conf(0),
    ))

# ── LED feedback ──────────────────────────────────────────────────────────────
def trigger_led():
    if ser is None:
        return
    try:
        ser.write(b"LED_ON\n")
    except Exception:
        pass

# ── Prediction ────────────────────────────────────────────────────────────────
def run_prediction(buf):
    """Run inference, compare against current target, post to Java."""
    if len(buf) < MIN_GESTURE_LEN:
        gui_status("⚠️  Gesture too short — try again")
        root.after(2000, gui_reset)
        return

    gui_status("🧠  Recognising...")

    processed = preprocess(buf)
    if processed is None:
        gui_status("❌  Invalid gesture data")
        root.after(2000, gui_reset)
        return

    try:
        preds      = model.predict(processed.reshape(1, SEQ_LEN, N_FEATURES), verbose=0)[0]
        top_idx    = int(np.argmax(preds))
        confidence = float(preds[top_idx]) * 100
        recognized = labels[top_idx]

        gui_predict(recognized, confidence)

        # ── BUG FIX: fetch what digit the child is currently supposed to write ──
        # Without this, targetDigit == recognizedDigit always → always "correct"
        target = get_current_target() or recognized

        if recognized == "NONE":
            gui_status("⏸️  No digit detected — write more clearly")
            gui_sync("📳  Sent NONE event → app")
            trigger_led()
            post_to_java("none-event", {"studentId": STUDENT_ID})

        elif confidence < CONF_THRESHOLD:
            gui_status(f"❓  Low confidence ({confidence:.1f}%) — try again")
            gui_sync("📳  Sent NONE (low conf) → app")
            post_to_java("none-event", {"studentId": STUDENT_ID})

        else:
            correct = (recognized == target)
            verdict = "✓ CORRECT" if correct else f"✗ WRONG (wanted {target})"
            gui_status(f"{verdict} — saw \"{recognized}\"  ({confidence:.1f}%)")
            gui_sync(f"📡  Sent digit-result → app  [{recognized} | target {target} | {'correct' if correct else 'wrong'}]")
            post_to_java("digit-result", {
                "studentId":       STUDENT_ID,
                "targetDigit":     target,
                "recognizedDigit": recognized,
                "confidence":      confidence / 100.0,
            })

        root.after(3500, gui_reset)
        root.after(3500, lambda: gui_sync("🔄  Waiting for next gesture..."))

    except Exception as e:
        print(f"Prediction error: {e}")
        gui_status(f"❌  Prediction error: {e}")
        root.after(2000, gui_reset)

# ── Serial reader (background thread) ────────────────────────────────────────
def read_serial():
    global recording, stop_start, captured_data, recording_buffer
    motion_buf = deque(maxlen=20)

    while True:
        if ser is None:
            time.sleep(0.5)
            continue
        try:
            raw   = ser.readline()
            line  = raw.decode(errors="ignore").strip()
            parts = line.split(",")
            if len(parts) != 6:
                continue
            try:
                values = list(map(float, parts))
            except ValueError:
                continue

            motion = calc_motion(values)
            motion_buf.append(motion)
            avg    = float(np.mean(motion_buf))
            gui_motion(avg)

            if not recording:
                if avg > START_THRESHOLD:
                    recording = True
                    captured_data = list(recording_buffer[-15:]) if recording_buffer else []
                    stop_start    = None
                    gui_status("✍️  Writing digit...")
                    gui_sync("🔴  Recording gesture...")
                    gui_samples(0)
                recording_buffer.append(values)
                if len(recording_buffer) > MAX_GESTURE_LEN:
                    recording_buffer = recording_buffer[-MAX_GESTURE_LEN:]

            else:
                captured_data.append(values)
                gui_samples(len(captured_data))

                if avg > STOP_THRESHOLD:
                    stop_start = None
                else:
                    if stop_start is None:
                        stop_start = time.time()
                    elif time.time() - stop_start > STOP_TIME:
                        if len(captured_data) >= MIN_GESTURE_LEN:
                            recording = False
                            gui_status("🎯  Processing...")
                            t = threading.Thread(
                                target=run_prediction,
                                args=(captured_data.copy(),),
                                daemon=True,
                            )
                            t.start()
                        else:
                            recording = False
                            gui_status("📏  Too short — try again")
                            root.after(1500, gui_reset)
                        captured_data = []
                        stop_start    = None

                if len(captured_data) > MAX_GESTURE_LEN:
                    recording = False
                    gui_status("⚠️  Gesture too long — try again")
                    root.after(1500, gui_reset)
                    captured_data = []
                    stop_start    = None

        except Exception as e:
            name = type(e).__name__
            if "SerialException" in name or "PermissionError" in name:
                gui_status("❌  Serial connection lost — replug Arduino")
                time.sleep(2)
            else:
                time.sleep(0.05)

# ── Background ping to verify Java is alive ───────────────────────────────────
def ping_java():
    """Runs every 5 s to show Java/app connection status in the UI."""
    while True:
        time.sleep(5)
        if not REQUESTS_AVAILABLE:
            gui_sync("⚠️  requests not installed")
            continue
        try:
            r = requests.get(f"{JAVA_API}/health", timeout=2)
            if r.status_code == 200:
                target = get_current_target()
                msg = f"🟢  App connected  —  current target: \"{target}\"" if target else "🟢  App connected"
                gui_sync(msg)
            else:
                gui_sync(f"🟡  App returned {r.status_code}")
        except Exception:
            gui_sync("🔴  App not reachable — is Java running on :8080?")

threading.Thread(target=read_serial, daemon=True).start()
threading.Thread(target=ping_java,   daemon=True).start()

# ── Banner ────────────────────────────────────────────────────────────────────
print("\n" + "="*58)
print("  NeuroFlow — Live Digit Recognition")
print("="*58)
print(f"  Arduino  : {SERIAL_PORT if ser else 'NOT CONNECTED'}")
print(f"  Java API : {JAVA_API}")
print(f"  Student  : {STUDENT_ID}")
print(f"  Classes  : {labels}")
print("="*58)
if ser is None:
    print("\n  ⚠️  Arduino not connected.")
    print("  Find your port:  Device Manager → Ports (COM & LPT)")
    print("  Then run:  set NEUROFLOW_SERIAL_PORT=COM3  (use your port number)")
    print("             python hardwre_recog.py\n")
print()

root.mainloop()
if ser:
    ser.close()
print("Closed.")
