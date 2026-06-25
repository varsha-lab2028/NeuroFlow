// TopBar — sticky header used on every screen
// Props:
//   title       (string)   — centre text
//   onBack      (fn|null)  — if provided, shows ← button; pass null to show empty spacer
//   backLabel   (string)   — defaults to "← Back"
//   onSettings  (fn)       — opens the settings overlay; pass null to hide cog

export default function TopBar({ title, onBack = null, backLabel = '← Back', onSettings }) {
  return (
    <div className="top-bar">
      {onBack
        ? <button className="btn-back" onClick={onBack}>{backLabel}</button>
        : <div style={{ minWidth: 60 }} />
      }
      <span className="title">{title}</span>
      {onSettings
        ? <button className="btn-cog" onClick={onSettings}>⚙</button>
        : <div style={{ minWidth: 60 }} />
      }
    </div>
  )
}
