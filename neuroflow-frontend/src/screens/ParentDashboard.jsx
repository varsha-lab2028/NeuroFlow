import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'
import RoleBar from '../components/RoleBar'

// Demo data — no API needed
const DEMO = {
  todayMinutes: 12,
  todayAttempts: 5,
  practicedLetters: ['b', 'd'],
  weeklyErrors: { 'b/d reversal': 60, 'p/q reversal': 30 },
  tips: [
    'Ask "which way does the bump go?" when reading together',
    'Point out b and d in books — no pressure, just notice them',
    'Celebrate the practice, not just the result ✨',
  ],
}

export default function ParentDashboard({ onSettings }) {
  const navigate = useNavigate()

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
              <div style={{ fontWeight: 800, color: 'var(--tx)', fontSize: 18 }}>{DEMO.todayMinutes} min</div>
              <div style={{ color: 'var(--sub)', fontSize: 11, marginTop: 2 }}>Time spent</div>
            </div>
            <div style={{ background: 'var(--alt)', borderRadius: 10, padding: 11 }}>
              <div style={{ fontWeight: 800, color: 'var(--tx)', fontSize: 18 }}>{DEMO.todayAttempts} tries</div>
              <div style={{ color: 'var(--sub)', fontSize: 11, marginTop: 2 }}>Per letter</div>
            </div>
            <div style={{ background: 'var(--alt)', borderRadius: 10, padding: 11, gridColumn: 'span 2' }}>
              <div style={{ fontWeight: 800, color: 'var(--tx)', fontSize: 16 }}>
                Letters {DEMO.practicedLetters.join(' & ')}
              </div>
              <div style={{ color: 'var(--sub)', fontSize: 11, marginTop: 2 }}>Practised today</div>
            </div>
          </div>
        </div>

        {/* Common mix-ups */}
        <div className="card">
          <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 16, marginBottom: 6 }}>🔄 Common mix-ups</div>
          <div style={{ color: 'var(--sub)', fontSize: 13, marginBottom: 14 }}>Totally normal — these letters look very similar!</div>
          {Object.entries(DEMO.weeklyErrors).map(([label, pct]) => (
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

        {/* Tips */}
        <div className="card">
          <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 16, marginBottom: 12 }}>💡 What you can do today</div>
          {DEMO.tips.map((tip, i) => (
            <div className="sstep" key={i}>
              <div className="snum">{i + 1}</div>
              <span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 2, lineHeight: 1.5 }}>{tip}</span>
            </div>
          ))}
        </div>

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
