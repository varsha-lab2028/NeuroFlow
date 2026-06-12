import { useNavigate } from 'react-router-dom'
import TopBar from '../components/TopBar'

export default function NumeracyScreen({ onSettings }) {
  const navigate = useNavigate()

  return (
    <div className="screen-enter">
      <TopBar
        title="Numbers"
        onBack={() => navigate('/home')}
        backLabel="← Home"
        onSettings={onSettings}
      />

      <div className="content">
        <div style={{ color: 'var(--sub)', marginBottom: 14, textAlign: 'center' }}>
          Choose a number to practise writing
        </div>

        {/* Gripper status */}
        <div className="grip-row" style={{ marginBottom: 18 }}>
          <div className="gdot gdot-ok pulse" />
          <span style={{ fontSize: 13, color: 'var(--tx)', fontWeight: 600 }}>Smart Gripper ready</span>
          <span style={{ fontSize: 12, color: 'var(--sub)', marginLeft: 'auto' }}>0 – 9</span>
        </div>

        <p className="slabel">PICK A NUMBER</p>

        {/* 0–9 grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5,1fr)', gap: 10, marginBottom: 20 }}>
          {[0,1,2,3,4,5,6,7,8,9].map((n) => (
            <button
              key={n}
              onClick={() => navigate(`/numwatch/${n}`)}
              style={{
                background: 'var(--sf)',
                border: '1.5px solid var(--bd)',
                borderRadius: 14,
                padding: '14px 0',
                cursor: 'pointer',
                fontSize: 26,
                fontWeight: 900,
                color: 'var(--tx)',
                fontFamily: 'Georgia,serif',
                transition: 'transform .15s',
              }}
              onMouseOver={e => e.currentTarget.style.transform = 'translateY(-2px)'}
              onMouseOut={e  => e.currentTarget.style.transform = ''}
            >
              {n}
            </button>
          ))}
        </div>

        {/* ML info card */}
        <div className="card" style={{ background: 'var(--acl)', borderColor: 'transparent' }}>
          <div style={{ fontWeight: 700, color: 'var(--ac)', marginBottom: 6, fontSize: 14 }}>
            🤖 ML model is watching
          </div>
          <div style={{ color: 'var(--tx)', fontSize: 13, lineHeight: 1.55 }}>
            When you write, the smart gripper will recognise which digit you wrote — even if it looks a little different each time!
          </div>
        </div>
      </div>
    </div>
  )
}
