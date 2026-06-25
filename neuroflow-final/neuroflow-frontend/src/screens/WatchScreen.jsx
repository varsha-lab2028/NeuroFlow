import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'
import { useTheme } from '../context/ThemeContext'

// Speaks a phonics sound using the Web Speech API
function speakLetter(sound = 'buh') {
  if (!window.speechSynthesis) return
  window.speechSynthesis.cancel()
  const u = new SpeechSynthesisUtterance(sound)
  u.rate = 0.72; u.pitch = 1.1; u.volume = 1
  const voices = window.speechSynthesis.getVoices()
  const nice   = voices.find(v => v.lang.startsWith('en') && /Female|Samantha|Karen|Google/.test(v.name))
  if (nice) u.voice = nice
  window.speechSynthesis.speak(u)
}

export default function WatchScreen({ onSettings }) {
  const navigate = useNavigate()
  const { motion } = useTheme()

  const s1Ref      = useRef(null)
  const s2Ref      = useRef(null)
  const startDotRef= useRef(null)

  const [phase, setPhase]   = useState('idle')  // 'idle' | 'playing' | 'done'
  const [status, setStatus] = useState('Press play to see how')
  const [dots,   setDots]   = useState([false, false, false])
  const timersRef            = useRef([])

  function clearTimers() {
    timersRef.current.forEach(clearTimeout)
    timersRef.current = []
  }

  function resetAnimation() {
    clearTimers()
    setPhase('idle')
    setStatus('Press play to see how')
    setDots([false, false, false])

    const reset = (el) => {
      if (!el) return
      el.style.transition = 'none'
      const len = el.getTotalLength()
      el.style.strokeDasharray = len
      el.style.strokeDashoffset = len
      el.style.stroke = 'var(--alt)'
    }
    reset(s1Ref.current)
    reset(s2Ref.current)
    if (startDotRef.current) startDotRef.current.style.display = 'none'
  }

  function playAnimation() {
    if (phase === 'playing') return
    setPhase('playing')

    const s1 = s1Ref.current
    const s2 = s2Ref.current

    // Init dash arrays
    ;[s1, s2].forEach(el => {
      if (!el) return
      el.style.transition = 'none'
      const len = el.getTotalLength()
      el.style.strokeDasharray = len
      el.style.strokeDashoffset = len
      el.style.stroke = 'var(--alt)'
    })

    // Phase 0: show start dot
    setStatus('● Start here — top of the letter')
    if (startDotRef.current) startDotRef.current.style.display = 'block'

    const t1 = setTimeout(() => {
      if (startDotRef.current) startDotRef.current.style.display = 'none'
      s1.style.stroke = 'var(--ac)'
      s1.getBoundingClientRect() // force reflow
      s1.style.transition = 'stroke-dashoffset 0.9s ease, stroke 0.2s'
      s1.style.strokeDashoffset = 0
      setDots([true, false, false])
      setStatus('↓ Drawing the stem all the way down...')
    }, 800)

    const t2 = setTimeout(() => {
      s2.style.stroke = 'var(--ac)'
      s2.getBoundingClientRect()
      s2.style.transition = 'stroke-dashoffset 1.1s ease, stroke 0.2s'
      s2.style.strokeDashoffset = 0
      setDots([true, true, false])
      setStatus('⤵ Adding the bump to the RIGHT...')
    }, 2000)

    const t3 = setTimeout(() => {
      setDots([true, true, true])
      s1.style.transition = 'stroke 0.3s'
      s2.style.transition = 'stroke 0.3s'
      s1.style.stroke = 'var(--tx)'
      s2.style.stroke = 'var(--tx)'
      setStatus("✓ Got it? Now it's your turn!")
      setPhase('done')
    }, 3400)

    timersRef.current = [t1, t2, t3]
  }

  // If motion is disabled, skip straight to done state
  useEffect(() => {
    if (!motion && phase === 'idle') {
      // Show the completed letter without animation
      ;[s1Ref.current, s2Ref.current].forEach(el => {
        if (!el) return
        el.style.transition = 'none'
        const len = el.getTotalLength()
        el.style.strokeDasharray = len
        el.style.strokeDashoffset = 0
        el.style.stroke = 'var(--tx)'
      })
      setDots([true, true, true])
      setStatus("Ready? Let's try!")
    }
  }, [motion])

  useEffect(() => () => clearTimers(), [])

  return (
    <div className="screen-enter">
      <TopBar title="Watch &amp; Learn" onBack={() => navigate('/home')} backLabel="← Home" onSettings={onSettings} />

      <div className="content">
        <div style={{ color: 'var(--sub)', marginBottom: 14, textAlign: 'center' }}>
          Watch how to write the letter
        </div>

        {/* Letter animation card */}
        <div className="card" style={{ textAlign: 'center', padding: '28px 20px', marginBottom: 14 }}>
          <div style={{ position: 'relative', display: 'inline-block' }}>
            {/* Pulsing start dot */}
            <div
              ref={startDotRef}
              style={{
                display: 'none',
                position: 'absolute', top: 8, left: 'calc(50% - 25px)',
                width: 16, height: 16, borderRadius: '50%',
                background: 'var(--ok)',
                boxShadow: '0 0 0 5px rgba(58,148,98,.22)',
                animation: 'pulse 1.2s infinite',
                zIndex: 2,
              }}
            />

            {/* SVG letter b */}
            <svg viewBox="0 0 110 168" width="130" height="152" style={{ display: 'block', margin: '0 auto', overflow: 'visible' }}>
              {/* Stroke 1: vertical stem */}
              <path
                ref={s1Ref}
                d="M 40,12 L 40,150"
                strokeWidth="13"
                strokeLinecap="round"
                fill="none"
                style={{ stroke: 'var(--alt)' }}
              />
              {/* Stroke 2: bowl bump to the right */}
              <path
                ref={s2Ref}
                d="M 40,84 C 40,58 108,58 108,112 C 108,157 64,163 40,150"
                strokeWidth="13"
                strokeLinecap="round"
                fill="none"
                style={{ stroke: 'var(--alt)' }}
              />
            </svg>
          </div>

          {/* Step dots */}
          <div className="step-dots">
            {dots.map((lit, i) => (
              <div key={i} className={`sdot ${lit ? 'lit' : ''}`} />
            ))}
          </div>
          <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 10, fontWeight: 600, minHeight: 20 }}>
            {status}
          </div>
        </div>

        {/* Stroke instructions */}
        <div className="card" style={{ marginBottom: 14 }}>
          <div style={{ fontWeight: 700, color: 'var(--tx)', marginBottom: 12, fontSize: 14 }}>How to write "b"</div>
          <div className="sstep"><div className="snum">1</div><span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 3 }}>Start at the top — place your pencil high up</span></div>
          <div className="sstep"><div className="snum">2</div><span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 3 }}>Draw a straight line all the way down</span></div>
          <div className="sstep"><div className="snum">3</div><span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 3 }}>Add a round bump to the <strong>RIGHT</strong> side</span></div>
        </div>

        {/* Buttons */}
        {phase !== 'done' ? (
          <button className="btn btn-ac" onClick={playAnimation}>▶&nbsp; Watch the animation</button>
        ) : (
          <>
            <button className="btn btn-ok" onClick={() => navigate('/try')}>✏️&nbsp; Now I'll try it!</button>
            <button className="btn btn-ghost" onClick={() => { resetAnimation(); playAnimation() }}>↩&nbsp; Watch again</button>
          </>
        )}

        <button
          onClick={() => speakLetter('buh')}
          style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 7, width: '100%', background: 'transparent', border: 'none', cursor: 'pointer', color: 'var(--ac)', fontWeight: 600, fontSize: 14, padding: 10 }}
        >
          🔊 Hear the letter sound
        </button>
      </div>
    </div>
  )
}
