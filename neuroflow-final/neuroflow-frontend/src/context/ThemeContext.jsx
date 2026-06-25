import { createContext, useContext, useState } from 'react'

const ThemeContext = createContext(null)

// Reading mode presets — mirrors the MODES object in the prototype JS
export const READING_MODES = {
  default: {
    themeClass: '', size: '16px', ls: '0em', lh: '1.6',
    ff: "'Segoe UI',system-ui,sans-serif",
    motion: true, sound: true, haptic: 'high',
    label: 'Default', note: '',
  },
  dyslexia: {
    themeClass: 't-warm', size: '18px', ls: '0.05em', lh: '1.95',
    ff: "'Lexend','Segoe UI',sans-serif",
    motion: false, sound: true, haptic: 'high',
    label: 'Dyslexia',
    note: '💡 Lexend font reduces visual crowding. Warm background lowers contrast strain. Extra spacing helps word recognition.',
  },
  adhd: {
    themeClass: 't-blue', size: '16px', ls: '0.01em', lh: '1.75',
    ff: "'Segoe UI',system-ui,sans-serif",
    motion: false, sound: false, haptic: 'low',
    label: 'ADHD Focus',
    note: '💡 Calm blue tones reduce visual overstimulation. Animations and sounds are off.',
  },
  sensory: {
    themeClass: 't-dark', size: '16px', ls: '0.02em', lh: '1.8',
    ff: "'Segoe UI',system-ui,sans-serif",
    motion: false, sound: false, haptic: 'low',
    label: 'Sensory-Sensitive',
    note: '💡 Dark background reduces brightness. All sounds and animations are off.',
  },
  vision: {
    themeClass: 't-dark', size: '21px', ls: '0.02em', lh: '2.0',
    ff: "'Lexend','Segoe UI',sans-serif",
    motion: true, sound: true, haptic: 'high',
    label: 'Low Vision',
    note: '💡 Maximum text size and high-contrast dark theme. Bold Lexend font improves legibility.',
  },
}

// Persist settings to sessionStorage so they survive a dev-server reload
function loadPref(key, fallback) {
  try { const v = sessionStorage.getItem('nf_' + key); return v !== null ? JSON.parse(v) : fallback }
  catch { return fallback }
}
function savePref(key, val) {
  try { sessionStorage.setItem('nf_' + key, JSON.stringify(val)) } catch {}
}

export function ThemeProvider({ children }) {
  const [themeClass,   setThemeClassRaw]  = useState(() => loadPref('themeClass',   ''))
  const [fontSize,     setFontSizeRaw]    = useState(() => loadPref('fontSize',     '16px'))
  const [readingMode,  setReadingMode]    = useState(() => loadPref('readingMode',  'default'))
  const [motion,       setMotionRaw]      = useState(() => loadPref('motion',       true))
  const [sound,        setSoundRaw]       = useState(() => loadPref('sound',        true))
  const [haptic,       setHapticRaw]      = useState(() => loadPref('haptic',       'high'))

  const setThemeClass = v => { savePref('themeClass', v); setThemeClassRaw(v) }
  const setFontSize   = v => { savePref('fontSize',   v); setFontSizeRaw(v)   }
  const setMotion     = v => { savePref('motion',     v); setMotionRaw(v)     }
  const setSound      = v => { savePref('sound',      v); setSoundRaw(v)      }
  const setHaptic     = v => { savePref('haptic',     v); setHapticRaw(v)     }

  function applyReadingMode(key) {
    const m = READING_MODES[key]
    if (!m) return
    savePref('readingMode', key)
    setReadingMode(key)
    setThemeClass(m.themeClass)
    setFontSize(m.size)
    setMotion(m.motion)
    setSound(m.sound)
    setHaptic(m.haptic)
  }

  // CSS variables injected onto the root app div
  const mode = READING_MODES[readingMode]
  const styleVars = {
    // Set CSS vars so child CSS rules can reference them
    '--ls': mode?.ls ?? '0em',
    '--lh': mode?.lh ?? '1.6',
    '--ff': mode?.ff ?? "'Segoe UI',system-ui,sans-serif",
    '--fs': fontSize,
    // Also set as real properties so they cascade to all children
    // (CSS vars on their own don't cascade — the property must too)
    fontFamily:    mode?.ff ?? "'Segoe UI',system-ui,sans-serif",
    letterSpacing: mode?.ls ?? '0em',
    lineHeight:    mode?.lh ?? '1.6',
    fontSize,
  }

  return (
    <ThemeContext.Provider value={{
      themeClass, setThemeClass,
      fontSize, setFontSize,
      readingMode, applyReadingMode,
      motion, setMotion,
      sound, setSound,
      haptic, setHaptic,
      styleVars,
    }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme() {
  return useContext(ThemeContext)
}
