import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'
import RoleBar from '../components/RoleBar'
import { useAuth } from '../context/AuthContext'
import { getHomeData } from '../api/students'

export default function HomeScreen({ onSettings }) {
  const navigate       = useNavigate()
  const { student }    = useAuth()
  const [data, setData] = useState(null)

  useEffect(() => {
    if (student?.studentId) {
      getHomeData(student.studentId).then(setData).catch(() => {})
    }
  }, [student])

  const name = student?.name?.split(' ')[0] || 'there'
  const recentSessions = data?.recentSessions ?? []

  // Build a set of letters practiced recently
  const practicedLetters = [...new Set(recentSessions.map(s => s.targetLetter))]
  const targetLetter = student?.currentLetter || 'b'

  return (
    <div className="screen-enter">
      <TopBar title="Learning Time" onSettings={onSettings} />
      <RoleBar active="child" />

      <div className="content" style={{ paddingTop: 20 }}>
        <div style={{ marginBottom: 20 }}>
          <div style={{ fontSize: 24, fontWeight: 800, color: 'var(--tx)' }}>Good morning! 👋</div>
          <div style={{ color: 'var(--sub)', marginTop: 4 }}>Ready to practise today?</div>
        </div>

        {/* Gripper status */}
        <div className="grip-row">
          <div className="gdot gdot-ok" />
          <div>
            <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 14 }}>Smart Gripper connected</div>
            <div style={{ color: 'var(--sub)', fontSize: 12 }}>Ready to start writing</div>
          </div>
        </div>

        <p className="slabel">CHOOSE A MODULE</p>

        {/* Literacy tile */}
        <button className="module-tile" onClick={() => navigate('/watch')}>
          <div className="m-icon" style={{ background: 'rgba(78,143,197,.13)' }}>✏️</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>Literacy</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Letters &amp; reading</div>
            <div style={{ color: 'var(--ac)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>
              b, d, p, q &nbsp;•&nbsp; phonics
            </div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        {/* Numeracy tile */}
        <button className="module-tile" onClick={() => navigate('/numeracy')}>
          <div className="m-icon" style={{ background: 'rgba(58,148,98,.12)' }}>🔢</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>Numeracy</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Numbers &amp; counting</div>
            <div style={{ color: 'var(--ok)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>
              0–9 &nbsp;•&nbsp; number shapes
            </div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        {/* Thinking skills tile */}
        <button className="module-tile" onClick={() => navigate('/thinking')}>
          <div className="m-icon" style={{ background: 'rgba(155,126,212,.12)' }}>🧩</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>Thinking Skills</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Patterns &amp; sequences</div>
            <div style={{ color: '#9B7ED4', fontSize: 12, marginTop: 4, fontWeight: 600 }}>
              sort &nbsp;•&nbsp; match &nbsp;•&nbsp; sequence
            </div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        {/* Yesterday's practice card */}
        <div className="card">
          <div style={{ fontWeight: 700, color: 'var(--tx)', marginBottom: 12 }}>Yesterday's practice</div>
          <div className="chips">
            {practicedLetters.length > 0
              ? practicedLetters.map((letter) => (
                  <div key={letter} className="chip chip-ok">{letter}</div>
                ))
              : ['b','d','p','q'].map((letter, i) => (
                  <div key={letter} className={`chip ${i < 3 ? 'chip-ok' : 'chip-no'}`}>{letter}</div>
                ))
            }
          </div>
          <div style={{ color: 'var(--sub)', fontSize: 12, marginTop: 9 }}>
            {practicedLetters.length > 0
              ? `${practicedLetters.length} letter${practicedLetters.length !== 1 ? 's' : ''} practised ✓`
              : '3 of 4 letters practised ✓'
            }
          </div>
        </div>
      </div>
    </div>
  )
}
