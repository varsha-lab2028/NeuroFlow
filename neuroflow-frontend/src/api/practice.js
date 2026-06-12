const BASE = '/api/practice'

export async function classify(targetLetter, studentId) {
  const res = await fetch(`${BASE}/classify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ targetLetter, studentId }),
  })
  return res.json()
}

export async function saveSession(data) {
  // data: { studentId, targetLetter, detectedLetter, isCorrect, confidence, attempts, durationSeconds }
  const res = await fetch(`${BASE}/session`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  return res.json()
}

export async function getTodayStats(studentId) {
  const res = await fetch(`${BASE}/today/${studentId}`)
  return res.json()
}

export async function getWeeklyDays(studentId) {
  const res = await fetch(`${BASE}/weekly-days/${studentId}`)
  return res.json()
}
