import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

// Demo hardcoded users — no backend needed
const DEMO_CHILD = {
  user:    { userId: 1, name: 'Aarav Mehta', role: 'child' },
  student: {
    studentId: 1, name: 'Aarav Mehta', initials: 'AM',
    currentLetter: 'b', streakDays: 4,
  },
}
const DEMO_PARENT = {
  user: { userId: 2, name: 'Parent', role: 'parent' },
}
const DEMO_EDUCATOR = {
  user: { userId: 7, name: 'Ms. Sharma', role: 'educator' },
}

export default function LoginScreen() {
  const navigate       = useNavigate()
  const { login }      = useAuth()

  function enter(demo, path) {
    login(demo.user, demo.student ?? null)
    navigate(path)
  }

  return (
    <div className="screen-enter">
      <div className="top-bar">
        <div style={{ minWidth: 60 }} />
        <span className="title">NeuroFlow</span>
        <div style={{ minWidth: 60 }} />
      </div>

      <div className="content" style={{ paddingTop: 32 }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ fontSize: 40, marginBottom: 12 }}>🧠</div>
          <div style={{ fontSize: 22, fontWeight: 800, color: 'var(--tx)', marginBottom: 6 }}>
            Welcome to NeuroFlow
          </div>
          <div style={{ color: 'var(--sub)', fontSize: 14, lineHeight: 1.6 }}>
            Smart handwriting support for every learner.<br />
            Choose a view to explore the demo.
          </div>
        </div>

        {/* Child */}
        <button className="module-tile" onClick={() => enter(DEMO_CHILD, '/home')}>
          <div className="m-icon" style={{ background: 'rgba(78,143,197,.13)', fontSize: 28 }}>🧒</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>Child View</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Aarav Mehta · Age 7</div>
            <div style={{ color: 'var(--ac)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>
              Practising letter "b" · 🔥 4 day streak
            </div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        {/* Parent */}
        <button className="module-tile" onClick={() => enter(DEMO_PARENT, '/parent')}>
          <div className="m-icon" style={{ background: 'rgba(58,148,98,.12)', fontSize: 28 }}>👨‍👩‍👧</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>Parent View</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Aarav's parent</div>
            <div style={{ color: 'var(--ok)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>
              Progress · mix-ups · daily tips
            </div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        {/* Educator */}
        <button className="module-tile" onClick={() => enter(DEMO_EDUCATOR, '/educator')}>
          <div className="m-icon" style={{ background: 'rgba(155,126,212,.12)', fontSize: 28 }}>🎓</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>Educator View</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Ms. Sharma · Class 2A</div>
            <div style={{ color: '#9B7ED4', fontSize: 12, marginTop: 4, fontWeight: 600 }}>
              5 students · trends · activities
            </div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        <div className="card" style={{ background: 'var(--acl)', borderColor: 'transparent', marginTop: 8 }}>
          <div style={{ fontSize: 13, color: 'var(--tx)', lineHeight: 1.6 }}>
            🤖 <strong>Smart Gripper</strong> sensor data is simulated in this demo. In production, it connects via Bluetooth to track real handwriting pressure and stroke direction.
          </div>
        </div>
      </div>
    </div>
  )
}
