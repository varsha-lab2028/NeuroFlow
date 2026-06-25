import { useNavigate } from 'react-router-dom'
import TopBar from "../../components/TopBar";

export default function ThinkingMenu({ onSettings }) {
  const navigate = useNavigate()

  return (
    <div className="screen-enter">
      <TopBar title="Thinking Skills" onBack={() => navigate('/home')} backLabel="← Home" onSettings={onSettings} />
      <div className="content">
        <div style={{ color: 'var(--sub)', marginBottom: 18, textAlign: 'center' }}>
          Pick an activity — take your time!
        </div>

        <p className="slabel">LETTERS &amp; READING</p>

        <button className="module-tile" onClick={() => navigate('/thinking/detective')}>
          <div className="m-icon" style={{ background: 'rgba(78,143,197,.13)', fontSize: 26 }}>🔍</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 16, color: 'var(--tx)' }}>Letter Detective</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Find all the matching letters in a row</div>
            <div style={{ color: 'var(--ac)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>b/d/p/q &nbsp;•&nbsp; visual discrimination</div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        <button className="module-tile" onClick={() => navigate('/thinking/sound')}>
          <div className="m-icon" style={{ background: 'rgba(58,148,98,.12)', fontSize: 26 }}>🔊</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 16, color: 'var(--tx)' }}>Sound Explorer</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Hear a sound — find the picture it matches</div>
            <div style={{ color: 'var(--ok)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>phonics &nbsp;•&nbsp; auditory processing</div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        <p className="slabel" style={{ marginTop: 8 }}>PATTERNS &amp; LOGIC</p>

        <button className="module-tile" onClick={() => navigate('/thinking/pattern')}>
          <div className="m-icon" style={{ background: 'rgba(214,139,37,.12)', fontSize: 26 }}>🚂</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 16, color: 'var(--tx)' }}>Pattern Train</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>What comes next in the pattern?</div>
            <div style={{ color: 'var(--warn)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>sequencing &nbsp;•&nbsp; logical thinking</div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        <button className="module-tile" onClick={() => navigate('/thinking/robot')}>
          <div className="m-icon" style={{ background: 'rgba(155,126,212,.13)', fontSize: 26 }}>🤖</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 16, color: 'var(--tx)' }}>Robot Directions</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Guide the robot to the star with arrows</div>
            <div style={{ color: '#9B7ED4', fontSize: 12, marginTop: 4, fontWeight: 600 }}>planning &nbsp;•&nbsp; computational thinking</div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        <p className="slabel" style={{ marginTop: 8 }}>MEMORY &amp; FOCUS</p>

        <button className="module-tile" onClick={() => navigate('/thinking/memory')}>
          <div className="m-icon" style={{ background: 'rgba(58,148,98,.12)', fontSize: 26 }}>🌱</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 16, color: 'var(--tx)' }}>Memory Garden</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Remember the flowers — then put them back</div>
            <div style={{ color: 'var(--ok)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>working memory &nbsp;•&nbsp; attention</div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        <button className="module-tile" onClick={() => navigate('/thinking/story')}>
          <div className="m-icon" style={{ background: 'rgba(200,92,58,.1)', fontSize: 26 }}>📖</div>
          <div>
            <div style={{ fontWeight: 800, fontSize: 16, color: 'var(--tx)' }}>Story Builder</div>
            <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>Put the pictures in the right order</div>
            <div style={{ color: 'var(--er)', fontSize: 12, marginTop: 4, fontWeight: 600 }}>sequencing &nbsp;•&nbsp; language &nbsp;•&nbsp; creativity</div>
          </div>
          <span className="m-arrow">›</span>
        </button>

        <div className="card" style={{ background: 'rgba(155,126,212,.07)', borderColor: 'rgba(155,126,212,.2)', marginTop: 4 }}>
          <div style={{ fontSize: 13, color: 'var(--tx)', lineHeight: 1.6 }}>
            💜 No timers, no pressure — every activity is just for exploring!
          </div>
        </div>
      </div>
    </div>
  )
}
