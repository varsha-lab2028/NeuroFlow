"""
fix_and_run.py
==============
Run this ONCE from your NeuroFlow folder:

    python fix_and_run.py

What it does:
  1. Installs h5py if missing
  2. Patches digit_model.h5 → digit_model_fixed.h5
     (removes LSTM keys 'time_major' and 'implementation' that TF 2.13+ rejects)
  3. Verifies the patched model loads and runs inference
  4. Tells you exactly what to run next

Weights are NEVER touched — only the JSON metadata inside the .h5 file.
"""

import os, sys, shutil, json

os.environ["TF_ENABLE_ONEDNN_OPTS"] = "0"
os.environ["TF_CPP_MIN_LOG_LEVEL"]  = "3"
import warnings; warnings.filterwarnings("ignore")

# ── 1. Install h5py if missing ────────────────────────────────────────────────
try:
    import h5py
except ImportError:
    print("Installing h5py...")
    os.system(f'"{sys.executable}" -m pip install h5py -q')
    import h5py

# ── 2. Patch ──────────────────────────────────────────────────────────────────
REMOVE_FROM_LSTM = {"time_major", "implementation"}

def patch(src, dst):
    shutil.copy2(src, dst)
    with h5py.File(dst, "r+") as f:
        raw = f.attrs["model_config"]
        if isinstance(raw, bytes): raw = raw.decode()
        cfg   = json.loads(raw)
        fixed = []
        for layer in cfg["config"]["layers"]:
            cn, lc = layer["class_name"], layer["config"]
            if cn == "LSTM":
                for k in list(lc):
                    if k in REMOVE_FROM_LSTM:
                        lc.pop(k); fixed.append(f"LSTM.{k}")
            if cn == "BatchNormalization":
                ax = lc.get("axis")
                if isinstance(ax, list) and len(ax) == 1:
                    lc["axis"] = ax[0]; fixed.append("BN.axis")
            if cn != "InputLayer":
                lc.pop("batch_input_shape", None)
        f.attrs["model_config"] = json.dumps(cfg)
    return fixed

print("\n── Patching model files ─────────────────────────────────────────────────")
done = []
for src in ("digit_model.h5", "best_digit_model.h5"):
    if not os.path.exists(src):
        print(f"  (skip: {src} not found)"); continue
    dst = src.replace(".h5", "_fixed.h5")
    changes = patch(src, dst)
    print(f"  ✅ {dst}  fixed: {', '.join(changes) if changes else 'already clean'}")
    done.append(dst)

if not done:
    print("\n❌ No .h5 files found here.")
    print("   Make sure you run this from the NeuroFlow folder that contains digit_model.h5")
    sys.exit(1)

# ── 3. Verify ─────────────────────────────────────────────────────────────────
print("\n── Verifying ────────────────────────────────────────────────────────────")
try:
    from tensorflow.keras.models import load_model
    import numpy as np
    m      = load_model(done[0], compile=False)
    p      = json.load(open("digit_norm_params.json"))
    labels = p["labels"]
    pred   = m.predict(np.random.randn(1, 50, 6).astype("float32"), verbose=0)[0]
    top    = labels[int(pred.argmax())]
    print(f"  ✅ Model runs OK  —  test output: '{top}' ({pred.max()*100:.1f}%)")
    print(f"  ✅ Classes: {labels}")
except Exception as e:
    print(f"  ❌ Model still failing: {e}")
    print("\n  Try installing an older TF:")
    print("    pip install tensorflow==2.15")
    sys.exit(1)

print("""
────────────────────────────────────────────────────────────────
✅  All done.  Now start the app:

  Terminal 1 (Java backend):
    cd java_backend
    mvn spring-boot:run

  Terminal 2 (React frontend):
    cd neuroflow-frontend
    npm install
    npm run dev

  Terminal 3 (Python ML — needs Arduino):
    python hardwre_recog.py

  Open browser: http://localhost:5173
  (Use the Simulate button if Arduino is not connected)
────────────────────────────────────────────────────────────────
""")
