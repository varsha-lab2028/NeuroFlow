import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import TopBar from '../components/TopBar'
import { useTheme } from '../context/ThemeContext'

// ── All number stroke data (ported 1:1 from prototype NUM_DATA) ──
const NUM_DATA = {
  0: {
    label: '0',
    steps: [
      'Start at the top — lift your pencil to the top-middle',
      'Curve all the way round in one big smooth loop',
      'Close the loop back where you started',
    ],
    strokes: [
      { d: 'M 60,18 C 95,18 105,50 105,90 C 105,130 95,162 60,162 C 25,162 15,130 15,90 C 15,50 25,18 60,18', dur: 2.0, msg: '⭕ Drawing the round loop...' },
    ],
    startTop: '45%', startLeft: '47%',
  },
  1: {
    label: '1',
    steps: ['Start at the top', 'Draw a straight line straight down'],
    strokes: [
      { d: 'M 60,18 L 60,162', dur: 0.9, msg: '↓ Drawing the line straight down...' },
    ],
    startTop: '8%', startLeft: '47%',
  },
  2: {
    label: '2',
    steps: [
      'Start near the top — centre of the number',
      'Curve right then swing down-left to the bottom',
      'Sweep across to the right along the bottom',
    ],
    strokes: [
      { d: 'M 35,45 C 35,18 90,18 90,50 C 90,72 70,88 45,112 C 30,126 22,138 22,148', dur: 1.1, msg: '↩ Curving down from the top...' },
      { d: 'M 22,148 L 95,148', dur: 0.6, msg: '→ Sweeping across the bottom...' },
    ],
    startTop: '20%', startLeft: '22%',
  },
  3: {
    label: '3',
    steps: [
      'Start at the top left',
      'Curve right, then back in to the middle',
      'Curve right again, then back in at the bottom',
    ],
    strokes: [
      { d: 'M 30,30 C 80,14 100,50 60,90', dur: 1.0, msg: '↩ Making the top bump...' },
      { d: 'M 60,90 C 100,125 80,162 30,148', dur: 1.0, msg: '↩ Making the bottom bump...' },
    ],
    startTop: '13%', startLeft: '20%',
  },
  4: {
    label: '4',
    steps: [
      'Start at the top right',
      'Draw diagonally down-left to the middle',
      'Draw a short line across to the right',
      'Lift and draw a line straight down from the top',
    ],
    strokes: [
      { d: 'M 80,20 L 20,100', dur: 0.7, msg: '↙ Drawing the diagonal arm...' },
      { d: 'M 20,100 L 100,100', dur: 0.5, msg: '→ Drawing the crossbar...' },
      { d: 'M 75,20 L 75,162', dur: 0.8, msg: '↓ Drawing the straight line down...' },
    ],
    startTop: '7%', startLeft: '58%',
  },
  5: {
    label: '5',
    steps: [
      'Start at the top right and draw left across the top',
      'Draw straight down the left side',
      'Curve right then down and back left to close the belly',
    ],
    strokes: [
      { d: 'M 90,20 L 25,20 L 25,72', dur: 0.7, msg: '← Drawing the top bar and left side...' },
      { d: 'M 25,72 C 25,72 22,90 48,94 C 78,98 105,108 105,130 C 105,152 86,164 60,164 C 34,164 18,150 18,130 C 18,115 28,105 40,100', dur: 1.3, msg: '↪ Curving right for the belly...' },
    ],
    startTop: '9%', startLeft: '70%',
  },
  6: {
    label: '6',
    steps: [
      'Start at the top right',
      'Curve left and sweep all the way down',
      'Loop around to the right to close the circle at the bottom',
    ],
    strokes: [
      { d: 'M 85,28 C 60,10 18,30 16,80 C 14,125 30,158 65,160 C 95,162 112,142 112,115 C 112,88 95,72 65,72 C 40,72 20,90 20,115', dur: 2.2, msg: '↩ Sweeping down and closing the circle...' },
    ],
    startTop: '11%', startLeft: '65%',
  },
  7: {
    label: '7',
    steps: [
      'Start at the top left',
      'Draw across to the right',
      'Then angle down-left to the bottom',
    ],
    strokes: [
      { d: 'M 20,28 L 100,28', dur: 0.5, msg: '→ Drawing the top bar...' },
      { d: 'M 100,28 L 38,162', dur: 0.9, msg: '↙ Angling down to the bottom...' },
    ],
    startTop: '11%', startLeft: '13%',
  },
  8: {
    label: '8',
    steps: [
      'Start in the middle',
      'Draw upward and loop round to make the top circle',
      'Continue curving round to make the bottom circle',
      'Come back to where you started',
    ],
    strokes: [
      { d: 'M 60,88 C 60,88 30,82 30,55 C 30,28 90,28 90,55 C 90,82 60,88 60,88 C 60,88 25,96 25,127 C 25,155 95,158 95,127 C 95,96 60,88 60,88', dur: 2.5, msg: '∞ Drawing both loops...' },
    ],
    startTop: '46%', startLeft: '46%',
  },
  9: {
    label: '9',
    steps: [
      'Start at the top of the circle',
      'Draw round to the left and all the way round to close the circle',
      'Continue the line straight down from the right side',
    ],
    strokes: [
      { d: 'M 68,20 C 95,20 108,40 108,62 C 108,88 90,105 65,105 C 40,105 22,88 22,62 C 22,36 40,20 65,20', dur: 1.8, msg: '○ Drawing the circle...' },
      { d: 'M 108,62 L 108,158', dur: 0.8, msg: '↓ Drawing the tail straight down...' },
    ],
    startTop: '7%', startLeft: '51%',
  },
}

const NUMBER_NAMES = ['zero','one','two','three','four','five','six','seven','eight','nine']

function speakNumber(n) {
  if (!window.speechSynthesis) return
  window.speechSynthesis.cancel()
  const u = new SpeechSynthesisUtterance(NUMBER_NAMES[n] ?? String(n))
  u.rate = 0.72; u.pitch = 1.1; u.volume = 1
  const nice = window.speechSynthesis.getVoices()
    .find(v => v.lang.startsWith('en') && /Female|Samantha|Karen|Google/.test(v.name))
  if (nice) u.voice = nice
  window.speechSynthesis.speak(u)
}

export default function NumberWatchScreen({ onSettings }) {
  const navigate      = useNavigate()
  const { num }       = useParams()           // comes from /numwatch/:num
  const currentNum    = parseInt(num ?? '1')
  const data          = NUM_DATA[currentNum] ?? NUM_DATA[1]
  const { motion }    = useTheme()

  // One ref per possible stroke (up to 3)
  const strokeRefs  = [useRef(null), useRef(null), useRef(null)]
  const startDotRef = useRef(null)
  const timersRef   = useRef([])

  const [phase,  setPhase]  = useState('idle')   // 'idle' | 'playing' | 'done'
  const [status, setStatus] = useState('Press play to see how')
  const [dots,   setDots]   = useState([])        // array of booleans, one per dot

  // Initialise dot array whenever number changes
  useEffect(() => {
    setDots(Array(data.strokes.length + 1).fill(false))
    setPhase('idle')
    setStatus('Press play to see how')
    resetStrokes()
  }, [currentNum])

  function clearTimers() {
    timersRef.current.forEach(clearTimeout)
    timersRef.current = []
  }

  function resetStrokes() {
    data.strokes.forEach((_, i) => {
      const el = strokeRefs[i].current
      if (!el) return
      el.style.transition = 'none'
      el.setAttribute('d', data.strokes[i].d)
      el.style.stroke = 'var(--alt)'
      el.style.strokeDasharray = ''
      el.style.strokeDashoffset = ''
    })
    if (startDotRef.current) startDotRef.current.style.display = 'none'
  }

  function play() {
    if (phase === 'playing') return
    setPhase('playing')
    setDots(Array(data.strokes.length + 1).fill(false))

    // Step 1: set all path 'd' attributes, reset to invisible
    data.strokes.forEach((s, i) => {
      const el = strokeRefs[i].current
      if (!el) return
      el.style.transition = 'none'
      el.setAttribute('d', s.d)
      el.style.stroke = 'var(--alt)'
    })

    // Show start dot
    setStatus('● Start here')
    if (startDotRef.current) {
      startDotRef.current.style.display = 'block'
      startDotRef.current.style.top  = data.startTop
      startDotRef.current.style.left = data.startLeft
    }

    // Step 2: after 100ms, compute getTotalLength and set dasharray
    const t0 = setTimeout(() => {
      data.strokes.forEach((_, i) => {
        const el = strokeRefs[i].current
        if (!el) return
        const len = el.getTotalLength()
        el.style.strokeDasharray  = len
        el.style.strokeDashoffset = len
      })
    }, 100)

    // Step 3: animate each stroke in sequence
    let cumTime = 800
    const newTimers = [t0]

    data.strokes.forEach((s, i) => {
      const t = setTimeout(() => {
        if (startDotRef.current && i === 0) startDotRef.current.style.display = 'none'
        const el = strokeRefs[i].current
        if (!el) return
        el.style.stroke = 'var(--ac)'
        el.getBoundingClientRect() // force reflow
        el.style.transition = `stroke-dashoffset ${s.dur}s ease, stroke 0.2s`
        el.style.strokeDashoffset = 0
        setDots(prev => prev.map((v, idx) => idx === i ? true : v))
        setStatus(s.msg)
      }, cumTime)
      newTimers.push(t)
      cumTime += Math.round(s.dur * 1000) + 600
    })

    // Step 4: finish
    const tFinal = setTimeout(() => {
      setDots(prev => prev.map(() => true))
      data.strokes.forEach((_, i) => {
        const el = strokeRefs[i].current
        if (!el) return
        el.style.transition = 'stroke 0.3s'
        el.style.stroke = 'var(--tx)'
      })
      setStatus("✓ Got it? Now it's your turn!")
      setPhase('done')
    }, cumTime)
    newTimers.push(tFinal)

    timersRef.current = newTimers
  }

  function replay() {
    clearTimers()
    setPhase('idle')
    setStatus('Press play to see how')
    setDots(Array(data.strokes.length + 1).fill(false))
    resetStrokes()
    setTimeout(play, 120)
  }

  // If motion disabled, show completed letter immediately
  useEffect(() => {
    if (!motion) {
      data.strokes.forEach((s, i) => {
        const el = strokeRefs[i].current
        if (!el) return
        el.style.transition = 'none'
        el.setAttribute('d', s.d)
        el.style.stroke = 'var(--tx)'
        const len = el.getTotalLength?.() ?? 0
        if (len) { el.style.strokeDasharray = len; el.style.strokeDashoffset = 0 }
      })
      setDots(Array(data.strokes.length + 1).fill(true))
      setStatus("Ready? Let's try!")
      setPhase('done')
    }
  }, [motion, currentNum])

  useEffect(() => () => clearTimers(), [])

  return (
    <div className="screen-enter">
      <TopBar
        title={`Watch & Learn — ${data.label}`}
        onBack={() => navigate('/numeracy')}
        backLabel="← Numbers"
        onSettings={onSettings}
      />

      <div className="content">
        <div style={{ color: 'var(--sub)', marginBottom: 14, textAlign: 'center' }}>
          Watch how to write the number <strong>{data.label}</strong>
        </div>

        {/* Animation card */}
        <div className="card" style={{ textAlign: 'center', padding: '28px 20px', marginBottom: 14 }}>
          <div style={{ position: 'relative', display: 'inline-block' }}>
            {/* Pulsing start dot */}
            <div
              ref={startDotRef}
              style={{
                display: 'none',
                position: 'absolute',
                width: 16, height: 16, borderRadius: '50%',
                background: 'var(--ok)',
                boxShadow: '0 0 0 5px rgba(58,148,98,.22)',
                animation: 'pulse 1.2s infinite',
                zIndex: 2,
              }}
            />
            {/* SVG with up to 3 stroke paths */}
            <svg viewBox="0 0 120 180" width="120" height="152"
              style={{ display: 'block', margin: '0 auto', overflow: 'visible' }}>
              {[0,1,2].map(i => (
                <path
                  key={i}
                  ref={strokeRefs[i]}
                  strokeWidth="13"
                  strokeLinecap="round"
                  fill="none"
                  style={{ stroke: 'var(--alt)' }}
                />
              ))}
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

        {/* Written steps card */}
        <div className="card" style={{ marginBottom: 14 }}>
          <div style={{ fontWeight: 700, color: 'var(--tx)', marginBottom: 12, fontSize: 14 }}>
            How to write "{data.label}"
          </div>
          {data.steps.map((step, i) => (
            <div className="sstep" key={i}>
              <div className="snum">{i + 1}</div>
              <span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 3 }}>{step}</span>
            </div>
          ))}
        </div>

        {/* Action buttons */}
        {phase !== 'done' ? (
          <button className="btn btn-ac" onClick={play}>▶&nbsp; Watch the animation</button>
        ) : (
          <>
            <button className="btn btn-ok" onClick={() => navigate(`/numtry/${currentNum}`)}>
              ✏️&nbsp; Now I'll try it!
            </button>
            <button className="btn btn-ghost" onClick={replay}>
              ↩&nbsp; Watch again
            </button>
          </>
        )}

        {/* Speak button */}
        <button
          onClick={() => speakNumber(currentNum)}
          style={{
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            gap: 7, width: '100%', background: 'transparent', border: 'none',
            cursor: 'pointer', color: 'var(--ac)', fontWeight: 600, fontSize: 14, padding: 10,
          }}
        >
          🔊 Hear the number
        </button>
      </div>
    </div>
  )
}
