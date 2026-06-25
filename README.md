# NeuroFlow 🧠✏️

> **A smart pencil gripper system for neurodivergent children** — real-time digit and letter recognition with haptic feedback to support early writing development.

Built by **Team Glitchmore Girls** at IIIT Delhi · 🏆 Special Mention, Education Category — WitchHunt Hackathon (selected from 6,000+ participants, 1,000+ teams, 373 prototypes, top 40 finalists)

---

## 📁 Project Materials

| Resource | Link |
|---|---|
| 📊 Prototype Presentation (PPT) | [Google Drive](https://drive.google.com/drive/u/0/folders/1cps0NRHROiH59iZCB10ABEY2Fyw0ci9h) |
| 🎥 Demo Video | [Google Drive](https://drive.google.com/drive/u/0/folders/1cps0NRHROiH59iZCB10ABEY2Fyw0ci9h) |

---

## 🧩 The Problem

Children aged 4–9 with dyslexia and other neurodivergent profiles frequently reverse visually similar letters and digits during early writing. Traditional correction relies on repetitive drill, which can be discouraging. Existing assistive tools are screen-based or passive — none deliver real-time physical feedback during the act of writing.

---

## 💡 The Solution

NeuroFlow is a **smart pencil gripper** that sits on a standard pencil and:

1. **Captures** 6-axis IMU motion data in real time as the child writes
2. **Classifies** the digit or letter being written using a custom-trained 1D CNN
3. **Delivers haptic feedback** immediately when a NONE (unrecognized / reversed) result is detected
4. **Logs practice sessions** so educators and parents can track progress over time

The system is non-punitive by design. Feedback is corrective, not evaluative — the app uses "practice activities" and "classroom focus" throughout, never "homework" or "assignments."

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Hardware Layer                    │
│     Arduino + MPU-6050 IMU + Coin Vibration Motor   │
└────────────────────┬────────────────────────────────┘
                     │ USB Serial (115200 baud)
                     │ ax, ay, az, gx, gy, gz
┌────────────────────▼────────────────────────────────┐
│              Python ML Layer (hardwre_recog.py)     │
│  • Gesture segmentation (motion threshold-based)    │
│  • 1D CNN digit classifier (0–9 + NONE)             │
│  • NONE detection → triggers haptic motor signal    │
│  • POSTs results to Java backend                    │
└────────────────────┬────────────────────────────────┘
                     │ REST (HTTP POST)
                     │ /api/digit-result · /api/none-event
┌────────────────────▼────────────────────────────────┐
│           Java Backend (Spring Boot :8080)          │
│  • REST API · SQLite (neuroflow.db)                 │
│  • Models: User, Student, PracticeSession,          │
│    ErrorPattern, PracticeActivity                   │
│  • Tables: digit_attempts, level_progress,          │
│    none_events                                      │
└────────────────────┬────────────────────────────────┘
                     │ polls every 1.5s
┌────────────────────▼────────────────────────────────┐
│        React Frontend (Vite + React :5173)          │
│  • Educator & Parent dashboards                     │
│  • Numeracy module (Watch → Try flow)               │
│  • NoneTrendChart — daily NONE counts via recharts  │
│    (downward trend = improvement)                   │
│  • ThinkingGames, GuideScreen, WinScreen            │
└─────────────────────────────────────────────────────┘
```

---

## 🤖 ML Pipeline

### Digit Model (`digit_model.h5`)
- **Architecture**: 1D CNN
- **Input**: 50 × 6 time-steps (ax, ay, az, gx, gy, gz from MPU-6050)
- **Classes**: 0–9 + NONE (11-class)
- **Data**: Custom-collected gesture data using the MPU-6050 sensor
- **Normalization**: `digit_norm_params.json` — stores per-feature mean and std (JSON, not pickle, for cross-platform compatibility)
- **NONE outputs**: trigger haptic motor feedback + log to `none_events` table

### Gesture Segmentation
Motion intensity is calculated from both accelerometer and gyroscope magnitude. Recording starts when intensity exceeds a threshold and ends after the motion has been still for 1.2 seconds. Sequences are smoothed with a moving average filter before being fed to the model.

### Key Design Notes
- Normalization parameters are stored as JSON (not pickle) — works across Python versions and operating systems.
- The `NONE` class handles unrecognized gestures and low-confidence outputs, decoupling correction from the classifier's confidence directly.

---

## 🔧 Hardware

| Component | Part |
|---|---|
| Microcontroller | Arduino (with USB serial) |
| IMU | MPU-6050 (6-axis accelerometer + gyroscope) |
| Haptic actuator | Coin vibration motor |
| Enclosure | 3D-printed pencil grip |

The Arduino sends raw `ax,ay,az,gx,gy,gz` values at 115200 baud over USB serial. The Python script reads and classifies these in real time.

---

## 📂 Repository Structure

```
NeuroFlow/
├── java_backend/               # Spring Boot REST API (Maven)
│   ├── src/main/java/com/neuroflow/
│   │   ├── api/                # Controllers (ML, Practice, Analytics, Auth)
│   │   ├── config/             # CORS, DatabaseManager
│   │   ├── dao/                # DAOs for all entities
│   │   └── model/              # Java models
│   └── pom.xml
├── neuroflow-frontend/         # React + Vite frontend
│   └── src/
│       ├── screens/            # HomeScreen, NumeracyScreen, NumberTryScreen,
│       │                       # NumberWatchScreen, EducatorDashboard,
│       │                       # ParentDashboard, TryScreen, WatchScreen,
│       │                       # GuideScreen, WinScreen, ThinkingGames
│       ├── components/         # NoneTrendChart, SettingsOverlay, TopBar, RoleBar
│       ├── api/                # axios calls (analytics, auth, practice, students)
│       └── context/            # AuthContext, ThemeContext
├── hardwre_recog.py            # Python: serial read → classify → POST to backend
├── digit_model.h5              # Trained 1D CNN digit model
├── digit_norm_params.json      # Normalization parameters (mean, std per feature)
├── neuroflow.db                # SQLite database
├── GRIPPER_USB_SETUP.md        # Hardware connection guide
├── start.bat                   # Windows one-click startup
├── start.sh                    # Mac/Linux startup script
└── old ML version/             # Archived: earlier prototype using OnHW-chars dataset
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+, Maven
- Python 3.9+ with `tensorflow`, `pyserial`, `requests`
- Node.js 18+
- Arduino IDE (for firmware upload)

### 1. Java Backend
```bash
cd java_backend
./mvnw spring-boot:run          # Mac/Linux
.\mvnw.cmd spring-boot:run      # Windows PowerShell
```
Wait for: `Started NeuroFlowApplication` — backend runs on **port 8080**.

### 2. React Frontend
```bash
cd neuroflow-frontend
npm install
npm run dev
```
Frontend runs on **port 5173**. Open `http://localhost:5173`.

### 3. Python + Hardware
```bash
pip install tensorflow pyserial requests

# Set your serial port (find it with: ls /dev/cu.* on Mac, Device Manager on Windows)
export NEUROFLOW_SERIAL_PORT=/dev/cu.usbserial-110   # Mac/Linux
# or edit SERIAL_PORT in hardwre_recog.py directly

python3 hardwre_recog.py
```

You'll see:
```
✅ Model loaded successfully
✅ Labels loaded: ['0', '1', '2', ..., '9', 'NONE']
✅ Connected to /dev/cu.usbserial-110
```

> **Startup order**: Java backend → React frontend → Python script

### One-click startup
```bash
# Windows
start.bat

# Mac/Linux
./start.sh
```

### Demo without hardware
Go to **Numeracy → pick a digit → Try screen** and click **"Simulate ML result → (demo)"**. This calls the exact same backend endpoints — the Java → SQLite → React data flow is fully real. Only gesture capture is mocked.

---

## 📊 Features

| Module | Status |
|---|---|
| Numeracy (digits 0–9) | ✅ Complete — real-time recognition + haptic feedback |
| Letters (b/d/p/q) | 🔄 ML model exists; UI in progress |
| Words | 📋 Planned |
| Educator Dashboard | ✅ Practice session logs, error pattern tracking |
| Parent Dashboard | ✅ Progress overview, NONE trend chart |
| Thinking Games | ✅ Cognitive exercises |

---

## 🏆 Recognition

**WitchHunt Hackathon — Special Mention, Education Category**
One of 8 Special Mentions awarded across the entire event, selected from 6,000+ participants, 1,000+ teams, and 373 prototypes. NeuroFlow was among the top 40 finalists.

---

## 👩‍💻 Team Glitchmore Girls

| Name | Role |
|---|---|
| **Varshamegana Atmakuri** | ML pipeline, Java backend, system integration |
| **Disha Kukkal** | React frontend, mobile app |
| **Yashswinie Arya** | React frontend, mobile app |
| **Ms. Monalisa Gupta** | Mentor |

IIIT Delhi

---

## 🔮 Roadmap

- [ ] Letter module UI (Level 2 — b/d/p/q)
- [ ] Words module (Level 3)
- [ ] Fine-tune models on larger hardware-collected datasets
- [ ] On-device TFLite inference (currently runs via Python script)
- [ ] Browser extension for reading mode across any webpage

---

## 📄 License

Built for educational and research purposes as part of the WitchHunt Hackathon.

---

*Glitchmore Girls — building at the intersection of neuroscience and technology.*
