# NeuroFlow 🧠✏️

> **A smart pencil gripper system for neurodivergent children** — real-time letter recognition and haptic feedback to support early writing development.

Built by **Team Glitchmore Girls** at IIIT Delhi · 🏆 Special Mention, Education Category — WitchHunt Hackathon (selected from 6,000+ participants, 1,000+ teams, 373 prototypes, top 40 finalists)

---

## 📁 Project Materials

| Resource | Link |
|---|---|
| 📊 Prototype Presentation (PPT) | [Google Drive](https://drive.google.com/drive/u/0/folders/1cps0NRHROiH59iZCB10ABEY2Fyw0ci9h) |
| 🎥 Demo Video | [Google Drive](https://drive.google.com/drive/u/0/folders/1cps0NRHROiH59iZCB10ABEY2Fyw0ci9h) |

---

## 🧩 The Problem

Children aged 4–9 with dyslexia and other neurodivergent profiles frequently reverse visually similar letters and digits — particularly **b, d, p, q** and numerals — during early writing. Traditional correction methods rely on repetitive drill, which can be discouraging. Existing assistive tools are either screen-based or passive, with no real-time physical feedback during the act of writing.

---

## 💡 The Solution

NeuroFlow is a **smart pencil gripper** that sits on a standard pencil and:

1. **Captures** handwriting motion data via an IMU sensor in real time
2. **Classifies** the letter or digit being written using on-device ML
3. **Delivers haptic feedback** immediately when a reversal is detected
4. **Logs practice sessions** so educators can track progress over time

The system is non-punitive by design. Feedback is corrective, not evaluative — the language throughout the app deliberately uses "practice activities" and "classroom focus," never "homework" or "assignments."

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Hardware Layer                    │
│   Arduino Nano 33 BLE + LSM9DS1 IMU + FSR Sensor   │
│              + Coin Vibration Motor                 │
└────────────────────┬────────────────────────────────┘
                     │ BLE / Serial
┌────────────────────▼────────────────────────────────┐
│              Python ML Layer (hardwre_recog.py)     │
│  • 1D CNN — letter classifier (b/d/p/q)             │
│  • 1D CNN — digit classifier (1–9 + NONE)           │
│  • Flask server (port 5000) for model serving       │
│  • NONE events trigger haptic feedback signal       │
└────────────────────┬────────────────────────────────┘
                     │ REST (HTTP POST)
┌────────────────────▼────────────────────────────────┐
│           Java Backend (Spring Boot)                │
│  • REST API · SQLite (neuroflow.db)                 │
│  • Models: User, Student, PracticeSession,          │
│    ErrorPattern, PracticeActivity                   │
│  • Tables: digit_attempts, level_progress,          │
│    none_events                                      │
└────────────────────┬────────────────────────────────┘
                     │ axios
┌────────────────────▼────────────────────────────────┐
│            React Frontend (Vite)                    │
│  • Dashboard · Progress charts (recharts)           │
│  • NoneTrendChart — daily NONE counts               │
│    (downward trend = improvement)                   │
└─────────────────────────────────────────────────────┘
```

---

## 🤖 ML Pipeline

### Letter Model (`neuroflow_model_v2.keras`)
- **Architecture**: 1D CNN
- **Input**: 100 × 6 (IMU axes: ax, ay, az, gx, gy, gz)
- **Classes**: b, d, p, q (4-class)
- **Dataset**: OnHW-chars (Fraunhofer IIS / STABILO)
- **Splits**: 0–3 train · split 4 held-out (writer-independent)
- **Accuracy**: 100% on held-out writer-independent test set
- **Scaler**: `scaler_params_v2.json` (JSON, not pickle — cross-platform safe)

### Digit Model (`digit_model.h5`)
- **Input**: 50 × 6
- **Classes**: digits 1–9 + NONE (10-class)
- **NONE outputs**: trigger haptic feedback + log to `none_events` table

### Key Design Decision
Reversed letters are not treated as a separate "reversal" class. A reversed *b* is simply a *d* — so reversal detection falls out naturally from letter identification. No correct/reversed class pairs needed.

---

## 🔧 Hardware

| Component | Part |
|---|---|
| Microcontroller | Arduino Nano 33 BLE |
| IMU | LSM9DS1 (onboard) |
| Pressure sensor | FSR (Force Sensitive Resistor) |
| Haptic actuator | Coin vibration motor |
| Enclosure | 3D-printed pencil grip (OpenSCAD, watertight mesh) |

---

## 📂 Repository Structure

```
NeuroFlow/
├── java_backend/           # Spring Boot REST API (Maven)
│   └── src/
├── neuroflow-ui/           # React + Vite frontend
│   └── src/
├── neuroflow-server/       # Flask ML server
│   ├── server.py
│   ├── neuroflow_model_v2.keras
│   ├── digit_model.h5
│   └── scaler_params_v2.json
├── hardwre_recog.py        # Python BLE → classify → POST to backend
├── arduino/                # Arduino Nano 33 BLE firmware
├── openscad/               # 3D-printable pencil grip design
├── notebooks/              # Training notebooks (Google Colab)
└── neuroflow.db            # SQLite database
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+, Maven
- Python 3.9+
- Node.js 18+
- Arduino IDE (for firmware upload)

### 1. Java Backend
```bash
cd java_backend
.\mvnw.cmd spring-boot:run        # Windows PowerShell
# or
./mvnw spring-boot:run            # Mac/Linux
```
Backend runs on **port 8080**.

### 2. React Frontend
```bash
cd neuroflow-ui
npm install
npm run dev
```
Frontend runs on **port 5173**.

### 3. Python ML Server
```bash
cd neuroflow-server
pip install -r requirements.txt
python server.py
```
Flask server runs on **port 5000**.

### 4. Hardware Recognition Script
```bash
python hardwre_recog.py
```
Start this **after** the Java backend and React frontend are running.

> **Startup order**: Java backend → React frontend → Python script

---

## 📊 Features

- **Level 1 — Digits**: Real-time digit recognition (1–9) with haptic correction
- **Level 2 — Letters**: Real-time b/d/p/q classification with haptic correction
- **Level 3 — Words**: Planned
- **Progress Dashboard**: Daily practice trends, error pattern tracking, session history
- **NoneTrendChart**: Visual downward trend as the child improves

---

## 🏆 Recognition

**WitchHunt Hackathon — Special Mention, Education Category**
One of 8 Special Mentions awarded across the entire event, selected from 6,000+ participants, 1,000+ teams, and 373 prototypes. NeuroFlow was among the top 40 finalists.

---

## 👩‍💻 Team Glitchmore Girls

| Name | Role |
|---|---|
| **Varsha** | ML pipeline, Java backend, system integration |
| **Disha Kukkal** | Mobile app development |
| **Yashswinie Arya** | Mobile app development |
| **Ms. Monalisa Gupta** | Mentor |

IIIT Delhi

---

## 🔮 Roadmap

- [ ] TFLite conversion for on-device inference (currently Flask server)
- [ ] Fine-tune models on real LSM9DS1 / MPU6050 hardware data
- [ ] Level 3 — Words implementation
- [ ] Browser extension for reading mode across any webpage
- [ ] Expand language support beyond English letters

---

## 📄 License

This project was built for educational and research purposes as part of the WitchHunt Hackathon.
