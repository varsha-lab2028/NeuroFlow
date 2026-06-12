import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'
import RoleBar from '../components/RoleBar'
import { useAuth } from '../context/AuthContext'
import { getParentSummary } from '../api/analytics'

export default function ParentDashboard({ onSettings }) {
  const navigate    = useNavigate()
  const { student } = useAuth()
  const [data, setData] = useState(null)

  useEffect(() => {
    if (student?.studentId) {
      getParentSummary(student.studentId).then(setData).catch(() => {})
    }
  }, [student])

  const errors       = data?.weeklyErrors ?? { 'b/d reversal': 60, 'p/q reversal': 30 }
  const minutes      = data?.todayDurationMinutes ?? 12
  const attempts     = data?.todayAttempts ?? 5
  const letters      = data?.practicedLetters ?? ['b', 'd']
  const sharedActs   = data?.sharedActivities ?? []

  return (
    <div className="screen-enter">
      <TopBar title="Parent Dashboard" onSettings={onSettings} />
      <RoleBar active="parent" />

      <div className="content" style={{ paddingTop: 18 }}>
        <div style={{ marginBottom: 16 }}>
          <div style={{ fontSize: 20, fontWeight: 800, color: 'var(--tx)' }}>Hi there 👋</div>
          <div style={{ color: 'var(--sub)' }}>Here's how practice went today</div>
        </div>

        {/* Today's summary */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
            <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 16 }}>📅 Today's practice</div>
            <span className="badge b-ok">Completed</span>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
            <div style={{ background: 'var(--alt)', borderRadius: 10, padding: 11 }}>
              <div style={{ fontWeight: 800, color: 'var(--tx)', fontSize: 18 }}>{minutes} min</div>
              <div style={{ color: 'var(--sub)', fontSize: 11, marginTop: 2 }}>Time spent</div>
            </div>
            <div style={{ background: 'var(--alt)', borderRadius: 10, padding: 11 }}>
              <div style={{ fontWeight: 800, color: 'var(--tx)', fontSize: 18 }}>{attempts} tries</div>
              <div style={{ color: 'var(--sub)', fontSize: 11, marginTop: 2 }}>Per letter</div>
            </div>
            <div style={{ background: 'var(--alt)', borderRadius: 10, padding: 11, gridColumn: 'span 2' }}>
              <div style={{ fontWeight: 800, color: 'var(--tx)', fontSize: 16 }}>
                Letters {[...letters].join(' & ')}
              </div>
              <div style={{ color: 'var(--sub)', fontSize: 11, marginTop: 2 }}>Practised today</div>
            </div>
          </div>
        </div>

        {/* Common mix-ups */}
        <div className="card">
          <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 16, marginBottom: 6 }}>🔄 Common mix-ups</div>
          <div style={{ color: 'var(--sub)', fontSize: 13, marginBottom: 14 }}>Totally normal — these letters look very similar!</div>
          {Object.entries(errors).map(([label, pct]) => (
            <div className="trow" key={label}>
              <div className="tlabel">
                <span style={{ fontWeight: 700, color: 'var(--tx)', fontFamily: 'Georgia,serif', fontSize: 19 }}>{label}</span>
                <span style={{ color: 'var(--sub)', fontSize: 12 }}>{pct}% of the time</span>
              </div>
              <div className="ttrack">
                <div className="tfill" style={{ width: `${pct}%`, background: pct > 50 ? 'var(--warn)' : 'var(--ac)' }} />
              </div>
            </div>
          ))}
        </div>

        {/* What you can do */}
        <div className="card">
          <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 16, marginBottom: 12 }}>💡 What you can do today</div>
          <div className="sstep"><div className="snum">1</div><span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 2, lineHeight: 1.5 }}>Ask "which way does the bump go?" when reading together</span></div>
          <div className="sstep"><div className="snum">2</div><span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 2, lineHeight: 1.5 }}>Point out b and d in books — no pressure, just notice them</span></div>
          <div className="sstep"><div className="snum">3</div><span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 2, lineHeight: 1.5 }}>Celebrate the practice, not just the result ✨</span></div>
        </div>

        {/* Shared activities from educator */}
        {sharedActs.length > 0 && (
          <div className="card">
            <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 16, marginBottom: 12 }}>📚 From the classroom this week</div>
            {sharedActs.map((act) => (
              <div className="sstep" key={act.activityId}>
                <div className="snum">•</div>
                <span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 2, lineHeight: 1.5 }}>{act.description}</span>
              </div>
            ))}
          </div>
        )}

        {/* Settings shortcut */}
        <button
          onClick={onSettings}
          style={{ display: 'flex', alignItems: 'center', gap: 12, width: '100%', background: 'var(--sf)', border: '1px solid var(--bd)', borderRadius: 18, padding: '16px 20px', cursor: 'pointer', textAlign: 'left', marginBottom: 0 }}
        >
          <span style={{ fontSize: 20 }}>⚙️</span>
          <div>
            <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 14 }}>Comfort settings</div>
            <div style={{ color: 'var(--sub)', fontSize: 12 }}>Theme, font size, sound &amp; motion</div>
          </div>
          <span style={{ marginLeft: 'auto', color: 'var(--sub)', fontSize: 18 }}>›</span>
        </button>
      </div>
    </div>
  )
}
