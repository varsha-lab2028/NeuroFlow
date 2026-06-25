import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'
import { useAuth } from '../context/AuthContext'
import { getTodayStats } from '../api/practice'

export default function WinScreen({ onSettings }) {
  const navigate    = useNavigate()
  const { student } = useAuth()
  const [stats, setStats] = useState(null)

  useEffect(() => {
    // Calls GET /api/practice/today/1 → real session data from DB
    if (student?.studentId) {
      getTodayStats(student.studentId).then(setStats).catch(() => {})
    }
  }, [student])

  const minutes  = stats ? Math.round((stats.durationSeconds ?? 0) / 60) : 12
  const attempts = stats?.attempts ?? 5
  const streak   = student?.streakDays ?? 4
  const letters  = stats?.practicedLetters?.length ?? 3

  return (
    <div className="screen-enter">
      <TopBar title="Great work!" onSettings={onSettings} />
      <div className="content" style={{ textAlign: 'center' }}>
        <div className="win-icon">🌟</div>
        <div style={{ fontSize: 28, fontWeight: 900, color: 'var(--tx)', marginBottom: 8 }}>Done for today!</div>
        <div style={{ color: 'var(--sub)', fontSize: 16, marginBottom: 26 }}>
          You practised the letter "b" — keep it up!
        </div>

        <div className="stat-grid">
          <div className="stat-card">
            <div style={{ fontSize: 24, marginBottom: 4 }}>⏱</div>
            <div className="sv">{minutes} min</div>
            <div className="sl">Time practised</div>
          </div>
          <div className="stat-card">
            <div style={{ fontSize: 24, marginBottom: 4 }}>✏️</div>
            <div className="sv">{attempts} tries</div>
            <div className="sl">Attempts made</div>
          </div>
          <div className="stat-card">
            <div style={{ fontSize: 24, marginBottom: 4 }}>🎯</div>
            <div className="sv">{letters} letters</div>
            <div className="sl">This week</div>
          </div>
          <div className="stat-card">
            <div style={{ fontSize: 24, marginBottom: 4 }}>🔥</div>
            <div className="sv">{streak} days</div>
            <div className="sl">Streak</div>
          </div>
        </div>

        <button className="btn btn-ac" onClick={() => navigate('/watch')}>
          One more? (letter "d") →
        </button>
        <button className="btn btn-ghost" onClick={() => navigate('/home')}>
          Back to home
        </button>
        <div style={{ color: 'var(--sub)', fontSize: 12, marginTop: 14 }}>
          Your parent can see your progress automatically
        </div>
      </div>
    </div>
  )
}
