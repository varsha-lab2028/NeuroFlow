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

export function ThemeProvider({ children }) {
  const [themeClass, setThemeClass]   = useState('')       // '', 't-blue', 't-warm', 't-dark'
  const [fontSize,   setFontSize]     = useState('16px')
  const [readingMode, setReadingMode] = useState('default')
  const [motion, setMotion]           = useState(true)
  const [sound,  setSound]            = useState(true)
  const [haptic, setHaptic]           = useState('high')   // 'low' | 'high'

  function applyReadingMode(key) {
    const m = READING_MODES[key]
    if (!m) return
    setReadingMode(key)
    setThemeClass(m.themeClass)
    setFontSize(m.size)
    setMotion(m.motion)
    setSound(m.sound)
    setHaptic(m.haptic)
  }

  // CSS variables injected onto the root app div
  const styleVars = {
    '--ls': READING_MODES[readingMode]?.ls ?? '0em',
    '--lh': READING_MODES[readingMode]?.lh ?? '1.6',
    '--ff': READING_MODES[readingMode]?.ff ?? "'Segoe UI',system-ui,sans-serif",
    letterSpacing: 'var(--ls)',
    lineHeight:    'var(--lh)',
    fontFamily:    'var(--ff)',
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
