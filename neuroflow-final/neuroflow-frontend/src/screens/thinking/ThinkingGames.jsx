// ─────────────────────────────────────────────────────────────────
//  All 5 Thinking Skills game screens
//  Each is its own exported component.
//  Import them individually in App.jsx
// ─────────────────────────────────────────────────────────────────

import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import TopBar from '../../components/TopBar'

// ── Shared back-button wrapper ────────────────────────────────────
function GameShell({ title, onSettings, children }) {
  const navigate = useNavigate()
  return (
    <div className="screen-enter">
      <TopBar title={title} onBack={() => navigate('/thinking')} backLabel="← Activities" onSettings={onSettings} />
      <div className="content">{children}</div>
    </div>
  )
}

function WhyCard({ text }) {
  return (
    <div className="card">
      <div style={{ fontWeight: 700, color: 'var(--tx)', marginBottom: 8, fontSize: 14 }}>Why this helps</div>
      <div style={{ color: 'var(--sub)', fontSize: 13, lineHeight: 1.6 }}>{text}</div>
    </div>
  )
}

// ─────────────────────────────────────────────────────────────────
// 1. LETTER DETECTIVE
// ─────────────────────────────────────────────────────────────────
export function DetectiveGame({ onSettings }) {
  const LETTERS = ['b','d','b','p','b','d']
  const [tapped, setTapped] = useState({})
  const [feedback, setFeedback] = useState({ text: "Tap the b's!", color: 'var(--sub)' })

  function tap(index, letter) {
    if (tapped[index]) return
    setTapped(prev => ({ ...prev, [index]: letter }))
    if (letter === 'b') {
      setFeedback({ text: "✓ Yes! That's a b!", color: 'var(--ok)' })
    } else {
      setFeedback({ text: `Look carefully — that's a "${letter}", not a "b" 👀`, color: 'var(--warn)' })
    }
  }

  function reset() {
    setTapped({})
    setFeedback({ text: "Tap the b's!", color: 'var(--sub)' })
  }

  return (
    <GameShell title="Letter Detective" onSettings={onSettings}>
      <div className="card" style={{ background: 'var(--acl)', borderColor: 'transparent', marginBottom: 18 }}>
        <div style={{ fontWeight: 700, color: 'var(--ac)', fontSize: 15, marginBottom: 4 }}>🔍 Your mission</div>
        <div style={{ color: 'var(--tx)', fontSize: 14, lineHeight: 1.6 }}>
          Find all the <strong>"b"</strong> letters hiding in the row — tap each one you spot!
        </div>
      </div>

      <div className="card" style={{ textAlign: 'center', padding: '28px 16px' }}>
        <div style={{ display: 'flex', justifyContent: 'center', gap: 14, flexWrap: 'wrap', marginBottom: 24 }}>
          {LETTERS.map((letter, i) => {
            const done = tapped[i]
            return (
              <div
                key={i}
                onClick={() => tap(i, letter)}
                style={{
                  width: 52, height: 52, borderRadius: 14,
                  background: done ? (done === 'b' ? 'rgba(58,148,98,.18)' : 'rgba(200,92,58,.1)') : 'var(--alt)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 30, fontWeight: 900, fontFamily: 'Georgia,serif',
                  cursor: 'pointer', transition: 'all .15s',
                  color: done ? (done === 'b' ? 'var(--ok)' : 'var(--sub)') : 'var(--tx)',
                  opacity: done && done !== 'b' ? 0.45 : 1,
                }}
              >
                {letter}
              </div>
            )
          })}
        </div>
        <div style={{ minHeight: 28, fontSize: 14, fontWeight: 700, color: feedback.color }}>{feedback.text}</div>
      </div>

      <div style={{ marginTop: 14 }}>
        <button className="btn btn-ok" onClick={reset}>▶ Try the next round!</button>
      </div>

      <WhyCard text="Many children mix up b, d, p and q — they look very similar! This game trains your eyes to spot the tiny differences. The more you practise, the easier reading becomes. 🌟" />
    </GameShell>
  )
}

// ─────────────────────────────────────────────────────────────────
// 2. SOUND EXPLORER
// ─────────────────────────────────────────────────────────────────
export function SoundGame({ onSettings }) {
  const OPTIONS = [
    { emoji: '🐶', label: 'Dog',    correct: false },
    { emoji: '🍌', label: 'Banana', correct: true  },
    { emoji: '🚗', label: 'Car',    correct: false },
    { emoji: '🌙', label: 'Moon',   correct: false },
  ]
  const [answered, setAnswered] = useState(false)
  const [feedback, setFeedback] = useState({ text: 'Tap the picture!', color: 'var(--sub)' })
  const [chosen,   setChosen]   = useState(null)

  function tap(idx) {
    if (answered) return
    const opt = OPTIONS[idx]
    setChosen(idx)
    setAnswered(true)
    if (opt.correct) {
      setFeedback({ text: '🎉 Yes! Banana starts with "B"!', color: 'var(--ok)' })
    } else {
      setFeedback({ text: 'Not quite — try listening again 🎧', color: 'var(--warn)' })
      setTimeout(() => { setAnswered(false); setChosen(null); setFeedback({ text: 'Tap the picture!', color: 'var(--sub)' }) }, 1400)
    }
  }

  return (
    <GameShell title="Sound Explorer" onSettings={onSettings}>
      <div className="card" style={{ background: 'rgba(58,148,98,.07)', borderColor: 'rgba(58,148,98,.2)', marginBottom: 18 }}>
        <div style={{ fontWeight: 700, color: 'var(--ok)', fontSize: 15, marginBottom: 4 }}>🔊 Listen carefully</div>
        <div style={{ color: 'var(--tx)', fontSize: 14, lineHeight: 1.6 }}>
          The app will say a letter sound. Tap the picture whose name <strong>starts with that sound!</strong>
        </div>
      </div>

      <div className="card" style={{ textAlign: 'center', padding: '24px 16px', marginBottom: 14 }}>
        <div style={{ background: 'var(--ok)', color: '#fff', borderRadius: '50%', width: 72, height: 72, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 36, fontWeight: 900, margin: '0 auto 16px', fontFamily: 'Georgia,serif' }}>B</div>
        <div style={{ color: 'var(--sub)', fontSize: 13, marginBottom: 20, fontWeight: 600 }}>Which picture starts with the "B" sound?</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          {OPTIONS.map((opt, i) => (
            <button
              key={i}
              onClick={() => tap(i)}
              style={{
                background: chosen === i ? (opt.correct ? 'rgba(58,148,98,.12)' : 'rgba(200,92,58,.08)') : 'var(--alt)',
                border: `2px solid ${chosen === i ? (opt.correct ? 'var(--ok)' : 'var(--er)') : 'var(--bd)'}`,
                borderRadius: 16, padding: '18px 8px', cursor: 'pointer',
                transition: 'all .15s', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8,
              }}
            >
              <span style={{ fontSize: 36 }}>{opt.emoji}</span>
              <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--tx)' }}>{opt.label}</span>
            </button>
          ))}
        </div>
        <div style={{ marginTop: 16, minHeight: 24, fontSize: 14, fontWeight: 700, color: feedback.color }}>{feedback.text}</div>
      </div>

      <WhyCard text="Connecting letter sounds to pictures builds phonics awareness — the foundation of reading. This directly supports the NeuroFlow literacy module. 📚" />
    </GameShell>
  )
}

// ─────────────────────────────────────────────────────────────────
// 3. PATTERN TRAIN
// ─────────────────────────────────────────────────────────────────
export function PatternGame({ onSettings }) {
  const OPTIONS = [
    { label: 'Blue square', shape: 'square', color: '#5B8FB9', correct: true  },
    { label: 'Red circle',  shape: 'circle', color: '#E05C5C', correct: false },
    { label: 'Blue circle', shape: 'circle', color: '#5B8FB9', correct: false },
    { label: 'Red square',  shape: 'square', color: '#E05C5C', correct: false },
  ]
  const [chosen,   setChosen]   = useState(null)
  const [answered, setAnswered] = useState(false)
  const [feedback, setFeedback] = useState({ text: 'Choose the missing piece!', color: 'var(--sub)' })

  function tap(idx) {
    if (answered) return
    setChosen(idx)
    setAnswered(true)
    if (OPTIONS[idx].correct) {
      setFeedback({ text: "🎉 That's right — the pattern repeats!", color: 'var(--ok)' })
    } else {
      setFeedback({ text: 'Look at the pattern again — what keeps repeating? 🔍', color: 'var(--warn)' })
      setTimeout(() => { setChosen(null); setAnswered(false); setFeedback({ text: 'Choose the missing piece!', color: 'var(--sub)' }) }, 1400)
    }
  }

  return (
    <GameShell title="Pattern Train" onSettings={onSettings}>
      <div className="card" style={{ background: 'rgba(214,139,37,.08)', borderColor: 'rgba(214,139,37,.25)', marginBottom: 18 }}>
        <div style={{ fontWeight: 700, color: 'var(--warn)', fontSize: 15, marginBottom: 4 }}>🚂 Keep the train going!</div>
        <div style={{ color: 'var(--tx)', fontSize: 14, lineHeight: 1.6 }}>Look at the pattern — tap the shape or colour that comes next!</div>
      </div>

      <div className="card" style={{ textAlign: 'center', padding: '24px 16px', marginBottom: 14 }}>
        <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--sub)', marginBottom: 14, letterSpacing: '.04em' }}>WHAT COMES NEXT?</div>
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 10, marginBottom: 24, flexWrap: 'wrap' }}>
          {[true, false, true, false, true, null].map((isCircle, i) => (
            <div
              key={i}
              style={{
                width: 44, height: 44, flexShrink: 0,
                borderRadius: isCircle === null ? 6 : isCircle ? '50%' : 6,
                background: isCircle === null ? 'var(--alt)' : isCircle ? '#E05C5C' : '#5B8FB9',
                border: isCircle === null ? '2.5px dashed var(--bd)' : 'none',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 20, color: 'var(--sub)',
              }}
            >
              {isCircle === null ? '?' : ''}
            </div>
          ))}
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          {OPTIONS.map((opt, i) => (
            <button
              key={i}
              onClick={() => tap(i)}
              style={{
                background: chosen === i ? (opt.correct ? 'rgba(58,148,98,.12)' : 'rgba(200,92,58,.08)') : 'var(--alt)',
                border: `2px solid ${chosen === i ? (opt.correct ? 'var(--ok)' : 'var(--er)') : 'var(--bd)'}`,
                borderRadius: 14, padding: '16px 8px', cursor: 'pointer',
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10, transition: 'all .15s',
              }}
            >
              <div style={{ width: 40, height: 40, borderRadius: opt.shape === 'circle' ? '50%' : 6, background: opt.color }} />
              <span style={{ fontSize: 12, fontWeight: 700, color: 'var(--tx)' }}>{opt.label}</span>
            </button>
          ))}
        </div>
        <div style={{ marginTop: 16, minHeight: 24, fontSize: 14, fontWeight: 700, color: feedback.color }}>{feedback.text}</div>
      </div>

      <WhyCard text="Spotting patterns builds logical thinking and early maths skills. It also trains the brain to look for structure — a key skill for reading and writing too. 🧠" />
    </GameShell>
  )
}

// ─────────────────────────────────────────────────────────────────
// 4. ROBOT DIRECTIONS
// ─────────────────────────────────────────────────────────────────
export function RobotGame({ onSettings }) {
  const GOAL = { r: 2, c: 3 }
  const [pos, setPos]         = useState({ r: 0, c: 0 })
  const [feedback, setFeedback] = useState({ text: 'Use the arrows to move!', color: 'var(--sub)' })

  function move(dir) {
    setPos(prev => {
      let { r, c } = prev
      if (dir === 'up')    r = Math.max(0, r - 1)
      if (dir === 'down')  r = Math.min(3, r + 1)
      if (dir === 'left')  c = Math.max(0, c - 1)
      if (dir === 'right') c = Math.min(3, c + 1)
      if (r === GOAL.r && c === GOAL.c) {
        setFeedback({ text: '🎉 You found the star! Amazing!', color: 'var(--ok)' })
      } else {
        setFeedback({ text: 'Keep going...', color: 'var(--sub)' })
      }
      return { r, c }
    })
  }

  function reset() {
    setPos({ r: 0, c: 0 })
    setFeedback({ text: 'Use the arrows to move!', color: 'var(--sub)' })
  }

  return (
    <GameShell title="Robot Directions" onSettings={onSettings}>
      <div className="card" style={{ background: 'rgba(155,126,212,.08)', borderColor: 'rgba(155,126,212,.25)', marginBottom: 18 }}>
        <div style={{ fontWeight: 700, color: '#9B7ED4', fontSize: 15, marginBottom: 4 }}>🤖 Guide your robot!</div>
        <div style={{ color: 'var(--tx)', fontSize: 14, lineHeight: 1.6 }}>Tap the arrow buttons to move the robot step by step to reach the ⭐</div>
      </div>

      <div className="card" style={{ padding: '18px 12px', marginBottom: 14 }}>
        {/* 4×4 grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: 6, marginBottom: 18 }}>
          {Array.from({ length: 16 }, (_, idx) => {
            const r = Math.floor(idx / 4)
            const c = idx % 4
            const isRobot = r === pos.r && c === pos.c
            const isGoal  = r === GOAL.r && c === GOAL.c
            return (
              <div
                key={idx}
                style={{
                  height: 62, borderRadius: 10,
                  background: isRobot ? 'rgba(155,126,212,.25)' : isGoal ? 'rgba(58,148,98,.15)' : 'var(--alt)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 26, transition: 'background .2s',
                }}
              >
                {isRobot ? '🤖' : isGoal ? '⭐' : ''}
              </div>
            )
          })}
        </div>

        <div style={{ textAlign: 'center', minHeight: 22, fontSize: 14, fontWeight: 700, color: feedback.color, marginBottom: 14 }}>
          {feedback.text}
        </div>

        {/* Arrow controls */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8, maxWidth: 180, margin: '0 auto' }}>
          <div />
          <button onClick={() => move('up')}    style={{ background: 'var(--alt)', border: 'none', borderRadius: 12, padding: 14, fontSize: 22, cursor: 'pointer' }}>⬆️</button>
          <div />
          <button onClick={() => move('left')}  style={{ background: 'var(--alt)', border: 'none', borderRadius: 12, padding: 14, fontSize: 22, cursor: 'pointer' }}>⬅️</button>
          <button onClick={() => move('down')}  style={{ background: 'var(--alt)', border: 'none', borderRadius: 12, padding: 14, fontSize: 22, cursor: 'pointer' }}>⬇️</button>
          <button onClick={() => move('right')} style={{ background: 'var(--alt)', border: 'none', borderRadius: 12, padding: 14, fontSize: 22, cursor: 'pointer' }}>➡️</button>
        </div>
        <div style={{ textAlign: 'center', marginTop: 12 }}>
          <button onClick={reset} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: 'var(--ac)', fontSize: 13, fontWeight: 600 }}>↩ Reset</button>
        </div>
      </div>

      <WhyCard text="Planning a sequence of moves trains executive function and directional thinking — the same skills needed for left-to-right reading and ordered writing. 🧩" />
    </GameShell>
  )
}

// ─────────────────────────────────────────────────────────────────
// 5. MEMORY GARDEN
// ─────────────────────────────────────────────────────────────────
const FLOWERS = ['🌷', '🌻', '🌹']
const CORRECT_SEQ = [0, 1, 2]

export function MemoryGame({ onSettings }) {
  // 'idle' | 'showing' | 'hidden' | 'done'
  const [phase,    setPhase]    = useState('idle')
  const [input,    setInput]    = useState([])
  const [tapped,   setTapped]   = useState({})
  const [visible,  setVisible]  = useState(true)
  const [status,   setStatus]   = useState('Look carefully...')
  const [feedback, setFeedback] = useState('')

  function start() {
    setInput([]); setTapped({}); setVisible(true)
    setStatus('Look carefully... 👀'); setFeedback('')
    setPhase('showing')
    setTimeout(() => {
      setVisible(false)
      setStatus('Now tap them in order!')
      setPhase('recall')
    }, 2200)
  }

  function tap(idx) {
    if (phase !== 'recall') return
    const next = input.length
    const newInput = [...input, idx]
    setInput(newInput)
    if (CORRECT_SEQ[next] === idx) {
      setTapped(prev => ({ ...prev, [idx]: 'ok' }))
      if (newInput.length === 3) {
        setFeedback('🌟 Perfect! You remembered them all!')
        setPhase('done')
      } else {
        setFeedback('✓ Keep going!')
      }
    } else {
      setTapped(prev => ({ ...prev, [idx]: 'err' }))
      setFeedback("Not quite — let's try again 🌱")
      setVisible(true)
      setStatus('Here they are again!')
      setPhase('done')
    }
  }

  function btnStyle(i) {
    const state = tapped[i]
    return {
      fontSize: 36,
      background: state === 'ok' ? 'rgba(58,148,98,.12)' : state === 'err' ? 'rgba(200,92,58,.08)' : 'var(--alt)',
      border: `2px solid ${state === 'ok' ? 'var(--ok)' : state === 'err' ? 'var(--er)' : 'var(--bd)'}`,
      borderRadius: 14, padding: '12px 16px', cursor: 'pointer', transition: 'all .15s',
      opacity: phase === 'recall' ? 1 : 0.4,
    }
  }

  return (
    <GameShell title="Memory Garden" onSettings={onSettings}>
      <div className="card" style={{ background: 'rgba(58,148,98,.07)', borderColor: 'rgba(58,148,98,.2)', marginBottom: 18 }}>
        <div style={{ fontWeight: 700, color: 'var(--ok)', fontSize: 15, marginBottom: 4 }}>🌱 Remember the flowers!</div>
        <div style={{ color: 'var(--tx)', fontSize: 14, lineHeight: 1.6 }}>
          Look carefully at the flowers — then they'll hide! Tap them in the <strong>same order</strong> you saw them.
        </div>
      </div>

      <div className="card" style={{ textAlign: 'center', padding: '24px 16px', marginBottom: 14 }}>
        {/* Show area */}
        <div style={{ display: 'flex', justifyContent: 'center', gap: 16, marginBottom: 20, minHeight: 64, alignItems: 'center' }}>
          {FLOWERS.map((f, i) => (
            <div key={i} style={{ fontSize: 48, transition: 'opacity .4s', opacity: visible ? 1 : 0 }}>{f}</div>
          ))}
        </div>
        <div style={{ fontSize: 13, color: 'var(--sub)', fontWeight: 600, marginBottom: 18 }}>{status}</div>

        {/* Tap buttons */}
        <div style={{ display: 'flex', justifyContent: 'center', gap: 12 }}>
          {FLOWERS.map((f, i) => (
            <button key={i} onClick={() => tap(i)} disabled={phase !== 'recall'} style={btnStyle(i)}>{f}</button>
          ))}
        </div>
        {feedback && <div style={{ marginTop: 14, fontSize: 14, fontWeight: 700, color: feedback.includes('🌟') ? 'var(--ok)' : 'var(--warn)' }}>{feedback}</div>}

        {(phase === 'idle' || phase === 'done') && (
          <button className="btn btn-ac" onClick={start} style={{ marginTop: 16, marginBottom: 0 }}>
            {phase === 'done' ? '▶ Try again!' : '▶ Show me the flowers!'}
          </button>
        )}
      </div>

      <WhyCard text="Working memory is how we hold information while we use it — essential for reading sentences, following instructions, and learning new things. 🌼" />
    </GameShell>
  )
}

// ─────────────────────────────────────────────────────────────────
// 6. STORY BUILDER
// ─────────────────────────────────────────────────────────────────
const STORY_CARDS = [
  { key: 'seed',   correctOrder: 1, emoji: '🌱', label: 'Boy finds seed' },
  { key: 'flower', correctOrder: 3, emoji: '🌸', label: 'Flower grows!'  },
  { key: 'water',  correctOrder: 2, emoji: '💧', label: 'Boy waters it'  },
]

export function StoryGame({ onSettings }) {
  const [nextExpected, setNextExpected] = useState(1)
  const [placed, setPlaced]             = useState({})   // key → order number
  const [feedback, setFeedback]         = useState({ text: 'Tap 1st, then 2nd, then 3rd!', color: 'var(--sub)' })

  function tap(card) {
    if (placed[card.key]) return
    if (card.correctOrder === nextExpected) {
      const newPlaced = { ...placed, [card.key]: nextExpected }
      setPlaced(newPlaced)
      const newNext = nextExpected + 1
      setNextExpected(newNext)
      if (newNext > 3) {
        setFeedback({ text: '🎉 Great story! You got it right!', color: 'var(--ok)' })
      } else {
        setFeedback({ text: `✓ Now tap number ${newNext}!`, color: 'var(--ok)' })
      }
    } else {
      setFeedback({ text: 'Hmm — think about what happens first 🤔', color: 'var(--warn)' })
      setTimeout(() => setFeedback({ text: 'Tap 1st, then 2nd, then 3rd!', color: 'var(--sub)' }), 1200)
    }
  }

  function reset() {
    setNextExpected(1); setPlaced({})
    setFeedback({ text: 'Tap 1st, then 2nd, then 3rd!', color: 'var(--sub)' })
  }

  return (
    <GameShell title="Story Builder" onSettings={onSettings}>
      <div className="card" style={{ background: 'rgba(200,92,58,.07)', borderColor: 'rgba(200,92,58,.2)', marginBottom: 18 }}>
        <div style={{ fontWeight: 700, color: 'var(--er)', fontSize: 15, marginBottom: 4 }}>📖 Tell the story!</div>
        <div style={{ color: 'var(--tx)', fontSize: 14, lineHeight: 1.6 }}>These pictures are all mixed up. Tap them in the right order to tell the story from start to finish.</div>
      </div>

      <div className="card" style={{ padding: '20px 16px', marginBottom: 14 }}>
        <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--sub)', letterSpacing: '.05em', marginBottom: 14 }}>TAP IN ORDER: 1 → 2 → 3</div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 10, marginBottom: 18 }}>
          {STORY_CARDS.map((card) => {
            const num = placed[card.key]
            return (
              <button
                key={card.key}
                onClick={() => tap(card)}
                style={{
                  background: num ? 'rgba(58,148,98,.1)' : 'var(--alt)',
                  border: `2.5px solid ${num ? 'var(--ok)' : 'var(--bd)'}`,
                  borderRadius: 16, padding: '14px 8px', cursor: 'pointer',
                  display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8,
                  transition: 'all .15s', position: 'relative',
                }}
              >
                <span style={{ fontSize: 34 }}>{card.emoji}</span>
                <span style={{ fontSize: 11, fontWeight: 700, color: 'var(--sub)' }}>{card.label}</span>
                {num && (
                  <div style={{
                    position: 'absolute', top: 6, right: 8,
                    width: 20, height: 20, borderRadius: '50%',
                    background: 'var(--ok)', color: '#fff',
                    fontSize: 11, fontWeight: 800,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>
                    {num}
                  </div>
                )}
              </button>
            )
          })}
        </div>
        <div style={{ textAlign: 'center', minHeight: 24, fontSize: 14, fontWeight: 700, color: feedback.color }}>{feedback.text}</div>
        <button onClick={reset} style={{ display: 'block', width: '100%', background: 'transparent', border: 'none', cursor: 'pointer', color: 'var(--ac)', fontSize: 13, fontWeight: 600, marginTop: 8 }}>↩ Start over</button>
      </div>

      <WhyCard text="Understanding that events happen in order builds sequencing skills — essential for following written instructions, telling stories, and understanding cause and effect. 📚" />
    </GameShell>
  )
}
