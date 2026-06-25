import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'
import RoleBar from '../components/RoleBar'
import { useAuth } from '../context/AuthContext'
import { getHomeData } from '../api/students'

export default function HomeScreen({ onSettings }) {
  const navigate        = useNavigate()
  const { student }     = useAuth()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Calls GET /api/students/1/home-data via Vite proxy → Java backend
    if (student?.studentId) {
      getHomeData(student.studentId)
        .then(setData)
        .catch(() => {})
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [student])

  const name             = student?.name?.split(' ')[0] || 'there'
  const recentSessions   = data?.recentSessions ?? []
  const practicedLetters = [...new Set(recentSessions.map(s => s.targetLetter))]

  return (
    <div className="screen-enter">
      <TopBar title="Learning Time" onSettings={onSettings} />
      <RoleBar active="child" />

      <div className="content" style={{ paddingTop: 20 }}>
        <div style={{ marginBottom: 20 }}>
          <div style={{ fontSize: 24, fontWeight: 800, color: 'var(--tx)' }}>
            Good morning, {name}! 👋
          </div>
          <div style={{ color: 'var(--sub)', marginTop: 4 }}>Ready to practise today?</div>
        </div>

        <div className="grip-row">
          <div className="gdot gdot-ok" />
          <div>
            <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 14 }}>Smart Gripper connected</div>
            <div style={{ color: 'var(--sub)', fontSize: 12 }}>Ready to start writing</div>
          </div>
        </div>

        <p className="slabel">CHOOSE A MODULE</p>

        <button className="module-tile" onClick={() => navigate('/watch')}>
          <div className="m-icon" style={{ background: 'rgba(78,143,197,.13)' }}>✏️</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>Literacy</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Letters &amp; reading</div>
            <div style={{ color: 'var(--ac)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>b, d, p, q &nbsp;•&nbsp; phonics</div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        <button className="module-tile" onClick={() => navigate('/numeracy')}>
          <div className="m-icon" style={{ background: 'rgba(58,148,98,.12)' }}>🔢</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>Numeracy</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Numbers &amp; counting</div>
            <div style={{ color: 'var(--ok)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>0–9 &nbsp;•&nbsp; number shapes</div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        <button className="module-tile" onClick={() => navigate('/thinking')}>
          <div className="m-icon" style={{ background: 'rgba(155,126,212,.12)' }}>🧩</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>Thinking Skills</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Patterns &amp; sequences</div>
            <div style={{ color: '#9B7ED4', fontSize: 12, marginTop: 4, fontWeight: 600 }}>sort &nbsp;•&nbsp; match &nbsp;•&nbsp; sequence</div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        {/* Yesterday's practice — real data from backend */}
        <div className="card">
          <div style={{ fontWeight: 700, color: 'var(--tx)', marginBottom: 12 }}>Yesterday's practice</div>
          <div className="chips">
            {practicedLetters.length > 0
              ? practicedLetters.map(l => <div key={l} className="chip chip-ok">{l}</div>)
              : ['b','d','p','q'].map((l, i) => (
                  <div key={l} className={`chip ${i < 3 ? 'chip-ok' : 'chip-no'}`}>{l}</div>
                ))
            }
          </div>
          <div style={{ color: 'var(--sub)', fontSize: 12, marginTop: 9 }}>
            {loading
              ? 'Loading...'
              : practicedLetters.length > 0
                ? `${practicedLetters.length} letter${practicedLetters.length !== 1 ? 's' : ''} practised ✓`
                : '3 of 4 letters practised ✓'
            }
          </div>
        </div>
      </div>
    </div>
  )
}
