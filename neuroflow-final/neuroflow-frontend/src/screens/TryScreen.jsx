import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'
import { useAuth } from '../context/AuthContext'
import { saveSession, getLetterLatest, reportPracticeResult } from '../api/practice'

const TARGET_LETTER = 'b'   // driven by student.currentLetter in a future iteration
const TOTAL_STEPS   = 5

export default function TryScreen({ onSettings }) {
  const navigate    = useNavigate()
  const { student } = useAuth()
  const studentId   = student?.studentId ?? 1

  // 'idle' | 'writing' | 'result' | 'paused'
  const [phase,      setPhase]      = useState('idle')
  const [step,       setStep]       = useState(1)
  const [elapsed,    setElapsed]    = useState(0)
  
  const [result,     setResult]     = useState(null)

  const timerRef    = useRef(null)
  const pollRef     = useRef(null)
  const startRef    = useRef(null)
  const baselineRef = useRef(null)   // latest DB id when polling starts — ignore older results

  const progress = Math.round((step / TOTAL_STEPS) * 100)

  function startTimer() {
    startRef.current = Date.now()
    timerRef.current = setInterval(() => {
      setElapsed(Math.floor((Date.now() - startRef.current) / 1000))
    }, 1000)
  }

  function stopTimer() { clearInterval(timerRef.current) }

  async function startPolling() {
    // Snapshot the newest existing session (incl. seed data) so only NEW results count
    try {
      const existing = await getLetterLatest(studentId)
      baselineRef.current = existing.empty ? null : existing.id
    } catch { baselineRef.current = null }

    pollRef.current = setInterval(async () => {
      try {
        const data = await getLetterLatest(studentId)
        if (data.empty || data.id === baselineRef.current) return
        baselineRef.current = data.id
        
        stopTimer()
        stopPolling()
        setResult(data)
        setPhase('result')
      } catch { /* keep polling */ }
    }, 1500)
  }

  function stopPolling() { clearInterval(pollRef.current) }

  function handleStart() {
    setPhase('writing')
    setResult(null)
    startTimer()
    startPolling()
  }

  function handlePause() {
    setPhase('paused')
    stopTimer()
    stopPolling()
  }

  function handleResume() {
    setPhase('writing')
    startTimer()
    startPolling()
  }

  // ── Demo simulation (no hardware / Flask) ────────────────────────────────
  async function handleSimulateError() {
    stopTimer()
    stopPolling()
    if (studentId) {
      await reportPracticeResult({
        studentId,
        targetLetter:   TARGET_LETTER,
        detectedLetter: 'd',
        isCorrect:      false,
        confidence:     0.82,
        durationSeconds: elapsed,
      }).catch(() => {})
      // legacy save for analytics
      await saveSession({
        studentId,
        targetLetter:    TARGET_LETTER,
        detectedLetter:  'd',
        isCorrect:       false,
        confidence:      0.82,
        attempts:        step,
        durationSeconds: elapsed,
      }).catch(() => {})
    }
    navigate('/guide')
  }

  async function handleSimulateCorrect() {
    stopTimer()
    stopPolling()
    if (studentId) {
      await reportPracticeResult({
        studentId,
        targetLetter:   TARGET_LETTER,
        detectedLetter: TARGET_LETTER,
        isCorrect:      true,
        confidence:     0.94,
        durationSeconds: elapsed,
      }).catch(() => {})
    }
    advanceStep()
  }

  function advanceStep() {
    const next = step + 1
    if (next > TOTAL_STEPS) {
      navigate('/win')
    } else {
      setStep(next)
      setPhase('idle')
      setResult(null)
      setElapsed(0)
    }
  }

  // Handle result from ML polling
  useEffect(() => {
    if (phase !== 'result' || !result) return
    if (result.correct) {
      setTimeout(() => advanceStep(), 1200)
    } else if (result.detectedLetter === 'NONE') {
      // stay on screen, let them retry
      setTimeout(() => setPhase('idle'), 1500)
    } else {
      setTimeout(() => navigate('/guide'), 1200)
    }
  }, [result, phase])

  useEffect(() => () => { stopTimer(); stopPolling() }, [])

  return (
    <div className="screen-enter">
      <TopBar title="Your Turn" onBack={() => navigate('/watch')} backLabel="← Watch" onSettings={onSettings} />

      <div className="content">
        {/* Progress bar */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 20 }}>
          <div className="prog-track" style={{ flex: 1 }}>
            <div className="prog-fill" style={{ width: `${progress}%` }} />
          </div>
          <span style={{ color: 'var(--sub)', fontSize: 13, fontWeight: 600, whiteSpace: 'nowrap' }}>
            {step} of {TOTAL_STEPS}
          </span>
        </div>

        <div style={{ textAlign: 'center', marginBottom: 18 }}>
          <div style={{ fontSize: 20, fontWeight: 800, color: 'var(--tx)', marginBottom: 5 }}>
            Now you try writing "{TARGET_LETTER}"
          </div>
          <div style={{ color: 'var(--sub)' }}>Use your gripper on paper</div>
        </div>

        {/* Writing area */}
        <div className={`paper ${phase === 'writing' ? 'active' : ''}`}>
          {phase === 'idle' && (
            <div style={{ textAlign: 'center', color: 'var(--sub)' }}>
              <div style={{ fontSize: 34, marginBottom: 8 }}>📝</div>
              <div style={{ fontSize: 14 }}>Press Start, then write on paper</div>
            </div>
          )}
          {phase === 'writing' && (
            <div style={{ textAlign: 'center' }}>
              <div className="gdot gdot-ok pulse" style={{ margin: '0 auto 10px' }} />
              <div style={{ color: 'var(--ok)', fontWeight: 700, fontSize: 14 }}>Listening…</div>
              <div style={{ color: 'var(--sub)', fontSize: 12, marginTop: 4 }}>
                {elapsed}s — Gripper is tracking your writing
              </div>
            </div>
          )}
          {phase === 'paused' && (
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 30, marginBottom: 8 }}>⏸</div>
              <div style={{ color: 'var(--sub)', fontSize: 14 }}>Paused — {elapsed}s so far</div>
            </div>
          )}
          {phase === 'result' && result && (
            <div style={{ textAlign: 'center' }}>
              {result.correct ? (
                <>
                  <div style={{ fontSize: 36, marginBottom: 6 }}>✅</div>
                  <div style={{ color: 'var(--ok)', fontWeight: 800, fontSize: 16 }}>
                    Correct! The gripper saw "{result.detectedLetter}"
                  </div>
                </>
              ) : (
                <>
                  <div style={{ fontSize: 36, marginBottom: 6 }}>📳</div>
                  <div style={{ color: 'var(--warn)', fontWeight: 700, fontSize: 14 }}>
                    {result.feedbackMessage || 'Watch the stroke direction'}
                  </div>
                </>
              )}
            </div>
          )}
          <div className="paper-ref">{TARGET_LETTER}</div>
        </div>

        {/* Gripper status */}
        <div className="grip-row">
          <div className="gdot gdot-ok" />
          <span style={{ fontSize: 13, color: 'var(--tx)', fontWeight: 600 }}>Gripper connected</span>
          <span style={{ fontSize: 12, color: 'var(--sub)', marginLeft: 'auto' }}>Haptic: gentle</span>
        </div>

        {/* Action buttons */}
        {phase === 'idle' && (
          <>
            <button className="btn btn-ac" onClick={handleStart}>▶&nbsp; Start writing</button>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-ghost" onClick={handleSimulateError} style={{ fontSize: 12, flex: 1 }}>
                Demo: wrong →
              </button>
              <button className="btn btn-ghost" onClick={handleSimulateCorrect} style={{ fontSize: 12, flex: 1 }}>
                Demo: correct ✓
              </button>
            </div>
          </>
        )}
        {phase === 'writing' && (
          <>
            <button className="btn btn-muted" onClick={handlePause}>⏸&nbsp; Pause</button>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-ghost" onClick={handleSimulateError} style={{ fontSize: 12, flex: 1 }}>
                Demo: wrong →
              </button>
              <button className="btn btn-ghost" onClick={handleSimulateCorrect} style={{ fontSize: 12, flex: 1 }}>
                Demo: correct ✓
              </button>
            </div>
          </>
        )}
        {phase === 'paused' && (
          <>
            <button className="btn btn-ac" onClick={handleResume}>▶&nbsp; Continue</button>
            <button className="btn btn-ghost" onClick={() => navigate('/guide')}>See hint</button>
          </>
        )}
      </div>
    </div>
  )
}
