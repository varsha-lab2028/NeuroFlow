import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'
import { useAuth } from '../context/AuthContext'
import { saveSession } from '../api/practice'

export default function TryScreen({ onSettings }) {
  const navigate    = useNavigate()
  const { student } = useAuth()

  // 'idle' | 'writing' | 'paused'
  const [phase, setPhase]     = useState('idle')
  const [step,  setStep]      = useState(1)         // 1–5 attempts
  const [elapsed, setElapsed] = useState(0)         // seconds
  const timerRef               = useRef(null)
  const startTimeRef           = useRef(null)

  const totalSteps = 5
  const progress   = Math.round((step / totalSteps) * 100)

  function startTimer() {
    startTimeRef.current = Date.now()
    timerRef.current = setInterval(() => {
      setElapsed(Math.floor((Date.now() - startTimeRef.current) / 1000))
    }, 1000)
  }

  function stopTimer() {
    clearInterval(timerRef.current)
  }

  function handleStart() {
    setPhase('writing')
    startTimer()
  }

  function handlePause() {
    setPhase('paused')
    stopTimer()
  }

  function handleResume() {
    setPhase('writing')
    startTimer()
  }

  // Simulate a stroke error — navigates to guide screen (demo)
  async function handleSimulateError() {
    stopTimer()
    // Save a failed session to the backend
    if (student?.studentId) {
      await saveSession({
        studentId:       student.studentId,
        targetLetter:    'b',
        detectedLetter:  'd',
        isCorrect:       false,
        confidence:      0.82,
        attempts:        step,
        durationSeconds: elapsed,
      }).catch(() => {})
    }
    navigate('/guide')
  }

  useEffect(() => () => stopTimer(), [])

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
            {step} of {totalSteps}
          </span>
        </div>

        <div style={{ textAlign: 'center', marginBottom: 18 }}>
          <div style={{ fontSize: 20, fontWeight: 800, color: 'var(--tx)', marginBottom: 5 }}>
            Now you try writing "b"
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
              <div style={{ color: 'var(--ok)', fontWeight: 700, fontSize: 14 }}>Listening...</div>
              <div style={{ color: 'var(--sub)', fontSize: 12, marginTop: 4 }}>
                Gripper is tracking your writing
              </div>
            </div>
          )}
          {phase === 'paused' && (
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 30, marginBottom: 8 }}>⏸</div>
              <div style={{ color: 'var(--sub)', fontSize: 14 }}>Paused — {elapsed}s so far</div>
            </div>
          )}
          <div className="paper-ref">b</div>
        </div>

        {/* Gripper status */}
        <div className="grip-row">
          <div className="gdot gdot-ok" />
          <span style={{ fontSize: 13, color: 'var(--tx)', fontWeight: 600 }}>Gripper connected</span>
          <span style={{ fontSize: 12, color: 'var(--sub)', marginLeft: 'auto' }}>Haptic: gentle</span>
        </div>

        {/* Action buttons */}
        {phase === 'idle' && (
          <button className="btn btn-ac" onClick={handleStart}>▶&nbsp; Start writing</button>
        )}
        {phase === 'writing' && (
          <>
            <button className="btn btn-muted" onClick={handlePause}>⏸&nbsp; Pause</button>
            <button className="btn btn-ghost" onClick={handleSimulateError} style={{ fontSize: 13 }}>
              Simulate stroke error → (demo)
            </button>
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
