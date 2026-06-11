# NeuroFlow — Full Stack App

A dyslexia support app for children, parents, and educators.

**Stack:** Flask (Python) frontend · Java 21 Spring Boot REST API · SQLite database

---

## Architecture

```
Browser
  │
  ▼
Flask (port 5001)          — UI (HTML/CSS/JS), session management, proxy
  │
  ▼ /api/* proxy calls
Java Spring Boot (port 8080) — REST API, business logic, SQLite
  │
  ▼
neuroflow.db                 — SQLite database (auto-created, auto-seeded)
```

---

## Project Structure

```
neuroflow/
├── start.sh                        # One-command launcher (both servers)
│
├── java_backend/
│   ├── pom.xml                     # Maven: Spring Boot + SQLite + JSON
│   ├── neuroflow.db                # SQLite DB (auto-seeded on first run)
│   └── src/main/java/com/neuroflow/
│       ├── NeuroFlowApplication.java
│       ├── config/
│       │   ├── DatabaseManager.java   # SQLite init, table creation, seeding
│       │   └── CorsConfig.java        # CORS for Flask frontend
│       ├── model/                     # User, Student, PracticeSession, etc.
│       ├── dao/                       # JDBC DAOs (UserDao, StudentDao, etc.)
│       └── api/                       # REST controllers
│           ├── AuthController.java    # POST /api/auth/login, GET /api/auth/children
│           ├── StudentController.java # CRUD students, home-data, stats
│           ├── PracticeController.java# POST classify + session, GET today stats
│           ├── AnalyticsController.java# Trends, activities, CSV export
│           └── HealthController.java  # GET /api/health
│
└── flask_frontend/
    ├── app.py                      # Flask app + proxy routes
    ├── requirements.txt            # flask, requests
    └── templates/
        └── app.html                # Full UI (matches HTML prototype exactly)
```

---

## Quick Start

### Prerequisites

- Java 21+ (`java --version`)
- Maven 3.9+ (`mvn --version`)
- Python 3.10+ (`python3 --version`)

### Run

```bash
cd neuroflow
./start.sh
```

Then open **http://localhost:5001** in your browser.

Or start servers separately:

```bash
# Terminal 1 — Java backend
cd java_backend
mvn spring-boot:run

# Terminal 2 — Flask frontend
cd flask_frontend
pip install -r requirements.txt
python app.py
```

---

## API Reference

### Auth
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/auth/children` | List child users for login picker |
| POST | `/api/auth/login` | `{ role, pin?, studentId? }` → user + student |

### Students
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/students` | All students |
| GET | `/api/students/{id}/home-data` | Student + today stats + recent sessions |
| GET | `/api/students/by-educator/{id}` | Students for educator dashboard |
| GET | `/api/students/by-parent/{id}` | Students for parent dashboard |
| PATCH | `/api/students/{id}/letter` | `{ letter }` — update current letter |
| PATCH | `/api/students/{id}/streak` | Increment streak |
| GET | `/api/students/stats/overview` | `{ activeStudents, practicedToday }` |

### Practice
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/practice/classify` | `{ targetLetter, studentId }` → ML result (simulated) |
| POST | `/api/practice/session` | Save completed session, update error patterns |
| GET | `/api/practice/today/{studentId}` | Today's stats summary |
| GET | `/api/practice/recent/{studentId}?limit=10` | Recent sessions |
| GET | `/api/practice/weekly-days/{studentId}` | Bar chart data (Sun–Sat) |

### Analytics
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/analytics/errors/weekly` | Error totals this week |
| GET | `/api/analytics/errors/all-time` | All-time error totals |
| GET | `/api/analytics/activities?educatorId=7` | Classroom activities |
| POST | `/api/analytics/activities` | Create activity |
| PATCH | `/api/analytics/activities/{id}/complete` | `{ completed: true }` |
| PATCH | `/api/analytics/activities/{id}/share` | `{ shared: true }` |
| GET | `/api/analytics/export/csv` | Download CSV report |
| GET | `/api/analytics/parent-summary/{studentId}` | Parent dashboard summary |

---

## Demo Credentials

| Role | PIN |
|------|-----|
| Parent | `1234` |
| Educator | `5678` |
| Child | No PIN — just tap name |

---

## Key Features Backed by the API

- **Login screen**: Child picker loaded live from `/api/auth/children`
- **Home screen**: Student streak, progress bar, current letter from `/api/students/{id}/home-data`
- **Try / Practice screen**: Letter classification via `/api/practice/classify` (simulated ML), session saved on completion
- **Win screen**: Accuracy, attempts, time from saved session data
- **Parent dashboard**: Today's duration, attempts, practised letters, error breakdown
- **Educator → Overview**: Live student list with progress bars, dynamic counts
- **Educator → This Week**: Activities with toggle (mark complete / share with parents)
- **Educator → Trends**: Error pattern bars from DB, weekly practice bar chart
- **Export CSV**: Downloads live report from Java backend

---

## Connecting to a Real ML Server

The ML classify endpoint in `PracticeController.java` uses simulation by default.
To connect a real model, set `ML_SERVER_URL` in `application.properties` and
update `PracticeController.simulateClassify()` to call it instead.

---

## Bugs Fixed

1. **`setSessionId` bug** — original `PracticeSession.java` had `this.sessionId = sessionId` inside the setter (instead of `this.sessionId = id`). Fixed.
2. **`setStudentId` bug** — original `Student.java` had `this.studentId = studentId` bug in setter. Fixed.
3. **CORS missing** — original app had no HTTP server; added `CorsConfig.java` to allow Flask→Java calls.
4. **`COALESCE` for null sums** — `sumDurationToday` and `countTodayAttempts` would return null on empty; wrapped in `COALESCE(..., 0)`.
5. **Weekly progress update** — original app never recalculated `weekly_progress` after sessions; now updates after each session save.
