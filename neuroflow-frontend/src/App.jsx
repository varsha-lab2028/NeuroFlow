import { useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'

import { AuthProvider, useAuth } from './context/AuthContext'
import { ThemeProvider, useTheme } from './context/ThemeContext'

import SettingsOverlay from './components/SettingsOverlay'

import LoginScreen    from './screens/LoginScreen'
import HomeScreen     from './screens/HomeScreen'
import WatchScreen    from './screens/WatchScreen'
import TryScreen      from './screens/TryScreen'
import GuideScreen    from './screens/GuideScreen'
import WinScreen      from './screens/WinScreen'
import ParentDashboard    from './screens/ParentDashboard'
import EducatorDashboard  from './screens/EducatorDashboard'

import ThinkingMenu   from './screens/thinking/ThinkingMenu'
import {
  DetectiveGame,
  SoundGame,
  PatternGame,
  RobotGame,
  MemoryGame,
  StoryGame,
} from './screens/thinking/ThinkingGames'

// ── Inner app: has access to both context values ──────────────────
function AppRoutes() {
  const { user }                     = useAuth()
  const { themeClass, styleVars }    = useTheme()
  const [settingsOpen, setSettings]  = useState(false)

  const openSettings  = () => setSettings(true)
  const closeSettings = () => setSettings(false)

  // Pass onSettings down to every screen so the ⚙ cog works everywhere
  const sp = { onSettings: openSettings }

  return (
    // #app div gets the theme class + CSS variable overrides for font/spacing
    <div id="app" className={themeClass} style={styleVars}>
      <SettingsOverlay isOpen={settingsOpen} onClose={closeSettings} />

      <Routes>
        {/* Public */}
        <Route path="/login" element={<LoginScreen />} />

        {/* Child flow */}
        <Route path="/home"    element={user ? <HomeScreen {...sp} />  : <Navigate to="/login" />} />
        <Route path="/watch"   element={user ? <WatchScreen {...sp} /> : <Navigate to="/login" />} />
        <Route path="/try"     element={user ? <TryScreen {...sp} />   : <Navigate to="/login" />} />
        <Route path="/guide"   element={user ? <GuideScreen {...sp} /> : <Navigate to="/login" />} />
        <Route path="/win"     element={user ? <WinScreen {...sp} />   : <Navigate to="/login" />} />

        {/* Thinking skills */}
        <Route path="/thinking"             element={user ? <ThinkingMenu {...sp} />  : <Navigate to="/login" />} />
        <Route path="/thinking/detective"   element={user ? <DetectiveGame {...sp} /> : <Navigate to="/login" />} />
        <Route path="/thinking/sound"       element={user ? <SoundGame {...sp} />     : <Navigate to="/login" />} />
        <Route path="/thinking/pattern"     element={user ? <PatternGame {...sp} />   : <Navigate to="/login" />} />
        <Route path="/thinking/robot"       element={user ? <RobotGame {...sp} />     : <Navigate to="/login" />} />
        <Route path="/thinking/memory"      element={user ? <MemoryGame {...sp} />    : <Navigate to="/login" />} />
        <Route path="/thinking/story"       element={user ? <StoryGame {...sp} />     : <Navigate to="/login" />} />

        {/* Parent / Educator */}
        <Route path="/parent"   element={user ? <ParentDashboard {...sp} />   : <Navigate to="/login" />} />
        <Route path="/educator" element={user ? <EducatorDashboard {...sp} /> : <Navigate to="/login" />} />

        {/* Fallback */}
        <Route path="*" element={<Navigate to={user ? '/home' : '/login'} />} />
      </Routes>
    </div>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ThemeProvider>
          <AppRoutes />
        </ThemeProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
