const BASE = '/api/practice'
const ML   = '/api'

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

// ── ML integration endpoints ──────────────────────────────────────────────────

/** POST /api/practice-result — Python posts letter classification result */
export async function reportPracticeResult(data) {
  try {
    const res = await fetch(`${ML}/practice-result`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return res.json()
  } catch { return { status: 'error' } }
}

/** POST /api/digit-result — Python posts digit recognition result */
export async function reportDigitResult(data) {
  try {
    const res = await fetch(`${ML}/digit-result`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return res.json()
  } catch { return { status: 'error' } }
}

/** POST /api/none-event — record a NONE / unrecognised digit buzz */
export async function reportNoneEvent(studentId) {
  try {
    const res = await fetch(`${ML}/none-event`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ studentId }),
    })
    return res.json()
  } catch { return { status: 'error' } }
}

/** GET /api/letter-latest/:studentId — poll for newest letter ML result */
export async function getLetterLatest(studentId) {
  try {
    const res = await fetch(`${ML}/letter-latest/${studentId}`)
    return res.json()
  } catch { return { empty: true } }
}

/** GET /api/digit-latest/:studentId — poll for newest digit ML result */
export async function getDigitLatest(studentId) {
  try {
    const res = await fetch(`${ML}/digit-latest/${studentId}`)
    return res.json()
  } catch { return { empty: true } }
}

/** GET /api/digit-session/:studentId — digits correctly written today */
export async function getDigitSession(studentId) {
  try {
    const res = await fetch(`${ML}/digit-session/${studentId}`)
    return res.json()
  } catch { return [] }
}

/** GET /api/none-trend/:studentId — NONE event trend for dashboard */
export async function getNoneTrend(studentId) {
  try {
    const res = await fetch(`${ML}/none-trend/${studentId}`)
    return res.json()
  } catch { return { direction: 'stable', data: [] } }
}

/** GET /api/levels/:studentId — level unlock/completion status */
export async function getLevels(studentId) {
  try {
    const res = await fetch(`${ML}/levels/${studentId}`)
    return res.json()
  } catch {
    return [
      { levelId: 1, completed: false, unlocked: true },
      { levelId: 2, completed: false, unlocked: false },
      { levelId: 3, completed: false, unlocked: false },
    ]
  }
}

/** GET /api/current-target/:studentId — what to practise next */
export async function getCurrentTarget(studentId) {
  try {
    const res = await fetch(`${ML}/current-target/${studentId}`)
    return res.json()
  } catch { return { target: '1', type: 'digit' } }
}

/**
 * POST /api/set-target
 * React calls this when the child presses Start on the Try screen.
 * This is what Python reads to know the ACTUAL digit on screen —
 * NOT current-target which returns the next DB-computed digit.
 */
export async function setActiveTarget(studentId, target) {
  try {
    const res = await fetch(`${ML}/set-target`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ studentId, target }),
    })
    return res.json()
  } catch { return { status: 'error' } }
}
