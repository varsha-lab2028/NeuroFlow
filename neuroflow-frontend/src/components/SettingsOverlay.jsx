import { useTheme, READING_MODES } from '../context/ThemeContext'

// SettingsOverlay — slides up from the bottom when the ⚙ cog is tapped
// Props:
//   isOpen   (bool)
//   onClose  (fn)

const THEMES = [
  { label: 'Soft Cream', cls: '',       bg: '#F6F1E8', ac: '#4E8FC5' },
  { label: 'Blue Mist',  cls: 't-blue', bg: '#EEF4FA', ac: '#5B8FB9' },
  { label: 'Warm Paper', cls: 't-warm', bg: '#F7EFE3', ac: '#C98F6B' },
  { label: 'Night',      cls: 't-dark', bg: '#2A2A2A', ac: '#7BA8F0' },
]

const RM_CARDS = [
  { key: 'default',  icon: '🌟', name: 'Default',           tag: 'Standard settings' },
  { key: 'dyslexia', icon: '📖', name: 'Dyslexia',          tag: 'Wider spacing · warm background · Lexend font' },
  { key: 'adhd',     icon: '🎯', name: 'ADHD Focus',        tag: 'Calm colours · no motion · minimal layout' },
  { key: 'sensory',  icon: '🌙', name: 'Sensory-Sensitive', tag: 'Dark · no sound · no motion · gentle haptic' },
  { key: 'vision',   icon: '🔎', name: 'Low Vision',        tag: 'Large text · high contrast · bold letters' },
]

export default function SettingsOverlay({ isOpen, onClose }) {
  const {
    themeClass, setThemeClass,
    fontSize, setFontSize,
    readingMode, applyReadingMode,
    motion, setMotion,
    sound, setSound,
    haptic, setHaptic,
  } = useTheme()

  if (!isOpen) return null

  const activeMode = READING_MODES[readingMode]

  return (
    <div className="overlay" onClick={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <div className="overlay-panel">

        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
          <span style={{ fontSize: 18, fontWeight: 700, color: 'var(--tx)' }}>Comfort Settings</span>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 24, color: 'var(--sub)', lineHeight: 1 }}>×</button>
        </div>
        <p style={{ fontSize: 13, color: 'var(--sub)', marginBottom: 20 }}>
          Adjust so the app feels just right for this child
        </p>

        {/* Reading mode */}
        <p style={{ fontSize: 12, fontWeight: 700, color: 'var(--sub)', letterSpacing: '.06em', marginBottom: 10 }}>READING MODE</p>
        <p style={{ fontSize: 12, color: 'var(--sub)', marginBottom: 12, lineHeight: 1.55 }}>
          Choose a preset — the app will automatically adjust colours, spacing, font, and motion
        </p>
        <div className="rm-scroll">
          {RM_CARDS.map(({ key, icon, name, tag }) => (
            <div
              key={key}
              className={`rm-card ${readingMode === key ? 'sel' : ''}`}
              onClick={() => applyReadingMode(key)}
            >
              <div className="rm-icon">{icon}</div>
              <span className="rm-name">{name}</span>
              <span className="rm-tag">{tag}</span>
            </div>
          ))}
        </div>

        {/* Active badge */}
        {readingMode !== 'default' && (
          <div className="rm-badge">
            <span>{activeMode.label} mode</span>
            <span style={{ opacity: 0.6 }}>active</span>
          </div>
        )}

        {/* Reading mode note */}
        {activeMode.note && (
          <div style={{ background: 'var(--acl)', borderRadius: 10, padding: '11px 13px', marginBottom: 18, fontSize: 12, color: 'var(--tx)', lineHeight: 1.55 }}>
            {activeMode.note}
          </div>
        )}

        {/* Colour theme */}
        <p style={{ fontSize: 12, fontWeight: 700, color: 'var(--sub)', letterSpacing: '.06em', marginBottom: 10 }}>COLOUR THEME</p>
        <div className="theme-grid">
          {THEMES.map(({ label, cls, bg, ac }) => (
            <button
              key={cls}
              className={`theme-opt ${themeClass === cls ? 'sel' : ''}`}
              style={{ background: bg, color: cls === 't-dark' ? '#E6E6E6' : '#1E1E1E', borderColor: themeClass === cls ? ac : 'transparent' }}
              onClick={() => setThemeClass(cls)}
            >
              <div className="theme-dot" style={{ background: ac }} />
              {label}
            </button>
          ))}
        </div>

        {/* Text size */}
        <p style={{ fontSize: 12, fontWeight: 700, color: 'var(--sub)', letterSpacing: '.06em', marginBottom: 10 }}>TEXT SIZE</p>
        <div className="size-row">
          {[['13px', 'A small', 12], ['16px', 'A medium', 15], ['20px', 'A large', 19]].map(([px, label, displaySize]) => (
            <button
              key={px}
              className={`size-btn ${fontSize === px ? 'on' : 'off'}`}
              style={{ fontSize: displaySize }}
              onClick={() => setFontSize(px)}
            >
              {label}
            </button>
          ))}
        </div>

        {/* Accessibility toggles */}
        <p style={{ fontSize: 12, fontWeight: 700, color: 'var(--sub)', letterSpacing: '.06em', marginBottom: 10 }}>ACCESSIBILITY</p>
        <div className="acessrow">
          <span style={{ color: 'var(--tx)', fontSize: 14 }}>Motion animations</span>
          <button className={`toggle ${motion ? 'on' : 'off'}`} onClick={() => setMotion(!motion)}>
            <div className="toggle-knob" />
          </button>
        </div>
        <div className="acessrow">
          <span style={{ color: 'var(--tx)', fontSize: 14 }}>Sound effects</span>
          <button className={`toggle ${sound ? 'on' : 'off'}`} onClick={() => setSound(!sound)}>
            <div className="toggle-knob" />
          </button>
        </div>
        <div className="acessrow">
          <span style={{ color: 'var(--tx)', fontSize: 14 }}>Haptic intensity</span>
          <div style={{ display: 'flex', gap: 6 }}>
            {['low', 'high'].map((level) => (
              <button
                key={level}
                onClick={() => setHaptic(level)}
                style={{
                  background: haptic === level ? 'var(--ac)' : 'var(--alt)',
                  border: 'none', borderRadius: 8, padding: '6px 12px',
                  fontSize: 12, fontWeight: 600,
                  color: haptic === level ? '#fff' : 'var(--sub)',
                  cursor: 'pointer',
                }}
              >
                {level.charAt(0).toUpperCase() + level.slice(1)}
              </button>
            ))}
          </div>
        </div>

      </div>
    </div>
  )
}
