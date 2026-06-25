import { useState, useEffect, useRef } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import TopBar from '../components/TopBar'
import { useAuth } from '../context/AuthContext'
import { getDigitLatest, reportDigitResult, reportNoneEvent, getDigitSession, setActiveTarget } from '../api/practice'

const DIGITS = ['1','2','3','4','5','6','7','8','9']

export default function NumberTryScreen({ onSettings }) {
  const navigate    = useNavigate()
  const { num }     = useParams()
  const { student } = useAuth()
  const studentId   = student?.studentId ?? 1
  const targetDigit = num ?? '1'

  const [phase,        setPhase]        = useState('idle')
  const [elapsed,      setElapsed]      = useState(0)
  const [result,       setResult]       = useState(null)
  const [correctToday, setCorrectToday] = useState([])
  const [simCount,     setSimCount]     = useState(0)
  const [hwLive,       setHwLive]       = useState(false)  // gripper producing results?

  const timerRef    = useRef(null)
  const pollRef     = useRef(null)
  const startRef    = useRef(null)
  const baselineRef = useRef(null)

  useEffect(() => {
    getDigitSession(studentId).then(setCorrectToday).catch(() => {})
  }, [studentId])

  async function startListening() {
    setPhase('listening')
    setResult(null)
    setElapsed(0)
    startRef.current = Date.now()

    // Tell the backend (and therefore Python) exactly which digit is on screen.
    // This is the fix for: Python was reading /api/current-target which returns
    // the next incomplete digit from the DB — NOT what the child sees right now.
    setActiveTarget(studentId, targetDigit).catch(() => {})

    // Snapshot current latest so we only react to NEW results
    try {
      const existing = await getDigitLatest(studentId)
      baselineRef.current = existing.empty ? null : existing.id
    } catch { baselineRef.current = null }

    timerRef.current = setInterval(() => {
      setElapsed(Math.floor((Date.now() - startRef.current) / 1000))
    }, 1000)

    // Poll every 800 ms — fast enough to feel real-time
    pollRef.current = setInterval(async () => {
      try {
        const data = await getDigitLatest(studentId)
        if (data.empty || data.id === baselineRef.current) return
        baselineRef.current = data.id
        setHwLive(true)
        stopListening()
        setResult(data)
        setPhase('result')
        if (data.correct) {
          setCorrectToday(prev =>
            prev.includes(data.targetDigit) ? prev : [...prev, data.targetDigit])
        }
      } catch { /* keep polling */ }
    }, 800)
  }

  function stopListening() {
    clearInterval(timerRef.current)
    clearInterval(pollRef.current)
  }

  useEffect(() => () => stopListening(), [])

  // ── Simulate (only shown when hardware not producing results) ────────────
  async function handleSimulate() {
    stopListening()
    setSimCount(c => c + 1)
    const isCorrect  = simCount % 3 !== 1
    const recognized = isCorrect
      ? targetDigit
      : simCount % 3 === 1
        ? 'NONE'
        : String((parseInt(targetDigit) + 1) % 10)
    const confidence = isCorrect ? 0.88 + Math.random() * 0.1 : 0.55 + Math.random() * 0.1

    if (recognized === 'NONE') {
      await reportNoneEvent(studentId).catch(() => {})
    } else {
      await reportDigitResult({ studentId, targetDigit, recognizedDigit: recognized, confidence }).catch(() => {})
    }

    const simResult = {
      id: Date.now(), targetDigit, recognizedDigit: recognized,
      correct: recognized === targetDigit, confidence, empty: false,
    }
    setResult(simResult)
    setPhase('result')
    if (simResult.correct)
      setCorrectToday(prev => prev.includes(targetDigit) ? prev : [...prev, targetDigit])
  }

  function handleNext() {
    const next = DIGITS.find(d => !correctToday.includes(d))
    if (!next || correctToday.length >= 9) navigate('/win')
    else navigate(`/numwatch/${next}`)
  }

  function handleRetry() {
    setPhase('idle')
    setResult(null)
    setElapsed(0)
  }

  const doneCount = correctToday.length
  const progress  = Math.round((doneCount / 9) * 100)

  return (
    <div className="screen-enter">
      <TopBar
        title={`Write "${targetDigit}"`}
        onBack={() => navigate(`/numwatch/${targetDigit}`)}
        backLabel="← Watch"
        onSettings={onSettings}
      />

      <div className="content">

        {/* Progress */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 18 }}>
          <div className="prog-track" style={{ flex: 1 }}>
            <div className="prog-fill" style={{ width: `${progress}%` }} />
          </div>
          <span style={{ color: 'var(--sub)', fontSize: 13, fontWeight: 600, whiteSpace: 'nowrap' }}>
            {doneCount} / 9
          </span>
        </div>

        {/* Digit chips */}
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 18 }}>
          {DIGITS.map(d => (
            <div key={d} style={{
              width: 34, height: 34, borderRadius: 10,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontWeight: 900, fontSize: 16, fontFamily: 'Georgia,serif',
              background: correctToday.includes(d) ? 'var(--acl)' : 'var(--alt)',
              color:      correctToday.includes(d) ? 'var(--ac)'  : 'var(--sub)',
              border:     d === targetDigit ? '2px solid var(--ac)' : '2px solid transparent',
              transition: 'all .3s',
            }}>
              {correctToday.includes(d) ? '✓' : d}
            </div>
          ))}
        </div>

        {/* Writing area */}
        <div className={`paper ${phase === 'listening' ? 'active' : ''}`} style={{ marginBottom: 16 }}>

          {phase === 'idle' && (
            <div style={{ textAlign: 'center' }}>
              <div style={{
                fontSize: 64, fontFamily: 'Georgia,serif', fontWeight: 900,
                color: 'var(--tx)', lineHeight: 1, marginBottom: 10,
              }}>{targetDigit}</div>
              <div style={{ color: 'var(--sub)', fontSize: 14 }}>
                Press Start, then write in the air
              </div>
            </div>
          )}

          {phase === 'listening' && (
            <div style={{ textAlign: 'center' }}>
              <div className="gdot gdot-ok pulse" style={{ margin: '0 auto 12px' }} />
              <div style={{ color: 'var(--ok)', fontWeight: 700, fontSize: 16 }}>
                Listening…
              </div>
              <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 6 }}>
                {elapsed}s — write "{targetDigit}" in the air with your gripper
              </div>
              <div style={{ color: 'var(--sub)', fontSize: 11, marginTop: 4 }}>
                Hold still for 1 second when done
              </div>
            </div>
          )}

          {phase === 'result' && result && (
            <div style={{ textAlign: 'center' }}>
              {result.recognizedDigit === 'NONE' ? (
                <>
                  <div style={{ fontSize: 44, marginBottom: 8 }}>📳</div>
                  <div style={{ color: 'var(--warn)', fontWeight: 700, fontSize: 16 }}>
                    Gripper didn't catch that
                  </div>
                  <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 6 }}>
                    Write more slowly and hold still at the end
                  </div>
                </>
              ) : result.correct ? (
                <>
                  <div style={{ fontSize: 44, marginBottom: 8 }}>🎉</div>
                  <div style={{ color: 'var(--ok)', fontWeight: 800, fontSize: 20 }}>
                    "{result.recognizedDigit}" — correct!
                  </div>
                  <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 6 }}>
                    Confidence: {Math.round((result.confidence ?? 0) * 100)}%
                  </div>
                </>
              ) : (
                <>
                  <div style={{ fontSize: 44, marginBottom: 8 }}>🤔</div>
                  <div style={{ color: 'var(--warn)', fontWeight: 700, fontSize: 16 }}>
                    Saw "{result.recognizedDigit}" — try writing "{targetDigit}" again
                  </div>
                  <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 6 }}>
                    Confidence: {Math.round((result.confidence ?? 0) * 100)}%
                  </div>
                </>
              )}
            </div>
          )}

          <div className="paper-ref">{targetDigit}</div>
        </div>

        {/* Gripper status */}
        <div className="grip-row" style={{ marginBottom: 16 }}>
          <div className={`gdot ${phase === 'listening' ? 'gdot-ok pulse' : 'gdot-ok'}`} />
          <span style={{ fontSize: 13, color: 'var(--tx)', fontWeight: 600 }}>
            Smart Gripper
          </span>
          <span style={{ fontSize: 12, color: 'var(--sub)', marginLeft: 'auto' }}>
            {phase === 'listening' ? '🔴 Recording' : hwLive ? '🟢 Live' : '⚪ Standby'}
          </span>
        </div>

        {/* Buttons */}
        {phase === 'idle' && (
          <>
            <button className="btn btn-ac" onClick={startListening}>
              🎙️  Start — write "{targetDigit}" now
            </button>
            {/* Only show simulate if hardware hasn't produced a real result yet */}
            {!hwLive && (
              <button
                className="btn btn-ghost"
                onClick={handleSimulate}
                style={{ fontSize: 12, color: 'var(--sub)' }}
              >
                No gripper? Simulate →
              </button>
            )}
          </>
        )}

        {phase === 'listening' && (
          <button className="btn btn-muted" onClick={() => { stopListening(); setPhase('idle') }}>
            ⏹  Stop listening
          </button>
        )}

        {phase === 'result' && result?.correct && (
          <>
            <button className="btn btn-ok" onClick={handleNext}>
              {doneCount >= 9 ? '🏆 All done!' : '✅ Next digit →'}
            </button>
            <button className="btn btn-ghost" onClick={handleRetry}>Try again</button>
          </>
        )}

        {phase === 'result' && !result?.correct && (
          <>
            <button className="btn btn-ac" onClick={handleRetry}>🔄 Try again</button>
            <button className="btn btn-ghost" onClick={() => navigate(`/numwatch/${targetDigit}`)}>
              ↩ Watch it again
            </button>
          </>
        )}

      </div>
    </div>
  )
}
