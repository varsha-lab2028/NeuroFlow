import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'

export default function GuideScreen({ onSettings }) {
  const navigate = useNavigate()

  return (
    <div className="screen-enter">
      <TopBar title="Let's adjust" onBack={() => navigate('/try')} onSettings={onSettings} />

      <div className="content">
        <div style={{ textAlign: 'center', marginBottom: 22 }}>
          <div style={{ fontSize: 13, color: 'var(--sub)', marginBottom: 6 }}>Your gripper noticed something</div>
          <div style={{ fontSize: 22, fontWeight: 800, color: 'var(--tx)' }}>Try starting here →</div>
        </div>

        {/* Correction pair */}
        <div className="card" style={{ textAlign: 'center' }}>
          <div className="corr-pair">
            <div style={{ textAlign: 'center', opacity: 0.5 }}>
              <div className="lsm" style={{ color: 'var(--er)', textDecoration: 'line-through', textDecorationColor: 'var(--er)' }}>d</div>
              <div style={{ fontSize: 12, color: 'var(--er)', fontWeight: 600, marginTop: 5 }}>Not this way</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div className="lsm" style={{ color: 'var(--ok)' }}>b</div>
              <div style={{ fontSize: 12, color: 'var(--ok)', fontWeight: 600, marginTop: 5 }}>Try this ✓</div>
            </div>
          </div>
          <div style={{ background: 'var(--acl)', borderRadius: 12, padding: '12px 16px' }}>
            <div style={{ fontSize: 14, color: 'var(--tx)', fontWeight: 600 }}>The bump goes to the RIGHT →</div>
          </div>
        </div>

        <div className="helpbox">
          💙 No worries — b and d are the trickiest letters! You're doing great just by practising.
        </div>

        <button className="btn btn-ok" onClick={() => navigate('/try')}>Let's try again!</button>
        <button className="btn btn-ghost" onClick={() => navigate('/watch')}>↩&nbsp; Watch the letter again</button>
      </div>
    </div>
  )
}
