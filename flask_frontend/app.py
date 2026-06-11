"""
NeuroFlow Flask Frontend
  - Serves the UI (HTML pages powered by the prototype's design)
  - Proxies all /api/* calls to the Java Spring Boot backend on port 8080
  - Manages session (login state, current role, current student) in Flask session
"""

from flask import (Flask, render_template, request, jsonify,
                   session, redirect, url_for, send_from_directory)
import requests, os

# ─── Config ──────────────────────────────────────────────────────────────────
JAVA_BASE = os.environ.get("JAVA_BACKEND_URL", "http://localhost:8080")

app = Flask(__name__)
app.secret_key = os.environ.get("FLASK_SECRET", "neuroflow-dev-secret-change-in-prod")

# ─── Helpers ─────────────────────────────────────────────────────────────────

def java(method: str, path: str, **kwargs):
    """Forward a request to the Java backend. Returns (data, status_code)."""
    url = f"{JAVA_BASE}{path}"
    try:
        resp = getattr(requests, method)(url, timeout=5, **kwargs)
        try:
            return resp.json(), resp.status_code
        except Exception:
            return {"raw": resp.text}, resp.status_code
    except requests.exceptions.ConnectionError:
        return {"error": "Java backend not reachable. Is it running on port 8080?"}, 503

def java_get(path, **kw):   return java("get",    path, **kw)
def java_post(path, **kw):  return java("post",   path, **kw)
def java_patch(path, **kw): return java("patch",  path, **kw)
def java_del(path, **kw):   return java("delete", path, **kw)

def logged_in():
    return "user_id" in session

# ─── Main App Routes ──────────────────────────────────────────────────────────

@app.route("/")
def index():
    """Serve the main SPA shell. All screen logic lives in the HTML/JS."""
    data, _ = java_get("/api/auth/children")
    children = data if isinstance(data, list) else []
    return render_template("app.html",
                           children=children,
                           current_role=session.get("role", "child"),
                           current_user=session.get("user_name", ""),
                           student_id=session.get("student_id", 1),
                           backend_ok=("error" not in data if isinstance(data, dict) else True))

# ─── Auth Proxy ───────────────────────────────────────────────────────────────

@app.route("/api/auth/login", methods=["POST"])
def auth_login():
    payload = request.get_json()
    data, code = java_post("/api/auth/login", json=payload)
    if code == 200 and data.get("ok"):
        session["role"]       = data.get("role")
        session["user_id"]    = data.get("user", {}).get("userId")
        session["user_name"]  = data.get("user", {}).get("name")
        if data.get("student"):
            session["student_id"] = data["student"]["studentId"]
    return jsonify(data), code

@app.route("/api/auth/logout", methods=["POST"])
def auth_logout():
    session.clear()
    return jsonify({"ok": True})

@app.route("/api/auth/children")
def auth_children():
    data, code = java_get("/api/auth/children")
    return jsonify(data), code

# ─── Students Proxy ───────────────────────────────────────────────────────────

@app.route("/api/students", methods=["GET"])
def students_all():
    data, code = java_get("/api/students")
    return jsonify(data), code

@app.route("/api/students/<int:sid>", methods=["GET"])
def student_get(sid):
    data, code = java_get(f"/api/students/{sid}")
    return jsonify(data), code

@app.route("/api/students/<int:sid>/home-data")
def student_home(sid):
    data, code = java_get(f"/api/students/{sid}/home-data")
    return jsonify(data), code

@app.route("/api/students/by-educator/<int:eid>")
def students_by_educator(eid):
    data, code = java_get(f"/api/students/by-educator/{eid}")
    return jsonify(data), code

@app.route("/api/students/by-parent/<int:pid>")
def students_by_parent(pid):
    data, code = java_get(f"/api/students/by-parent/{pid}")
    return jsonify(data), code

@app.route("/api/students/stats/overview")
def students_stats():
    data, code = java_get("/api/students/stats/overview")
    return jsonify(data), code

@app.route("/api/students/<int:sid>/letter", methods=["PATCH"])
def student_letter(sid):
    data, code = java_patch(f"/api/students/{sid}/letter", json=request.get_json())
    return jsonify(data), code

@app.route("/api/students/<int:sid>/streak", methods=["PATCH"])
def student_streak(sid):
    data, code = java_patch(f"/api/students/{sid}/streak")
    return jsonify(data), code

# ─── Practice Proxy ───────────────────────────────────────────────────────────

@app.route("/api/practice/classify", methods=["POST"])
def practice_classify():
    data, code = java_post("/api/practice/classify", json=request.get_json())
    return jsonify(data), code

@app.route("/api/practice/session", methods=["POST"])
def practice_session():
    data, code = java_post("/api/practice/session", json=request.get_json())
    return jsonify(data), code

@app.route("/api/practice/today/<int:sid>")
def practice_today(sid):
    data, code = java_get(f"/api/practice/today/{sid}")
    return jsonify(data), code

@app.route("/api/practice/recent/<int:sid>")
def practice_recent(sid):
    limit = request.args.get("limit", 10)
    data, code = java_get(f"/api/practice/recent/{sid}?limit={limit}")
    return jsonify(data), code

@app.route("/api/practice/weekly-days/<int:sid>")
def practice_weekly(sid):
    data, code = java_get(f"/api/practice/weekly-days/{sid}")
    return jsonify(data), code

# ─── Analytics Proxy ──────────────────────────────────────────────────────────

@app.route("/api/analytics/errors/weekly")
def errors_weekly():
    data, code = java_get("/api/analytics/errors/weekly")
    return jsonify(data), code

@app.route("/api/analytics/errors/all-time")
def errors_alltime():
    data, code = java_get("/api/analytics/errors/all-time")
    return jsonify(data), code

@app.route("/api/analytics/activities")
def activities_list():
    eid = request.args.get("educatorId", 7)
    data, code = java_get(f"/api/analytics/activities?educatorId={eid}")
    return jsonify(data), code

@app.route("/api/analytics/activities/week")
def activities_week():
    data, code = java_get("/api/analytics/activities/week")
    return jsonify(data), code

@app.route("/api/analytics/activities/shared")
def activities_shared():
    data, code = java_get("/api/analytics/activities/shared")
    return jsonify(data), code

@app.route("/api/analytics/activities", methods=["POST"])
def activities_create():
    data, code = java_post("/api/analytics/activities", json=request.get_json())
    return jsonify(data), code

@app.route("/api/analytics/activities/<int:aid>/complete", methods=["PATCH"])
def activities_complete(aid):
    data, code = java_patch(f"/api/analytics/activities/{aid}/complete",
                             json=request.get_json())
    return jsonify(data), code

@app.route("/api/analytics/activities/<int:aid>/share", methods=["PATCH"])
def activities_share(aid):
    data, code = java_patch(f"/api/analytics/activities/{aid}/share",
                             json=request.get_json())
    return jsonify(data), code

@app.route("/api/analytics/activities/<int:aid>", methods=["DELETE"])
def activities_delete(aid):
    data, code = java_del(f"/api/analytics/activities/{aid}")
    return jsonify(data), code

@app.route("/api/analytics/export/csv")
def export_csv():
    """Stream the CSV directly from Java."""
    resp = requests.get(f"{JAVA_BASE}/api/analytics/export/csv", timeout=10)
    from flask import Response
    return Response(
        resp.content,
        mimetype="text/csv",
        headers={"Content-Disposition": resp.headers.get(
            "Content-Disposition", "attachment; filename=NeuroFlow_Report.csv")}
    )

@app.route("/api/analytics/parent-summary/<int:sid>")
def parent_summary(sid):
    data, code = java_get(f"/api/analytics/parent-summary/{sid}")
    return jsonify(data), code

# ─── Health ───────────────────────────────────────────────────────────────────

@app.route("/api/health")
def health():
    data, code = java_get("/api/health")
    return jsonify({"flask": "ok", "java": data}), code

# ─── Run ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=True)
