import { useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'

import { AuthProvider }          from './context/AuthContext'
import { ThemeProvider, useTheme } from './context/ThemeContext'

import SettingsOverlay    from './components/SettingsOverlay'
import LoginScreen        from './screens/LoginScreen'
import HomeScreen         from './screens/HomeScreen'
import WatchScreen        from './screens/WatchScreen'
import TryScreen          from './screens/TryScreen'
import GuideScreen        from './screens/GuideScreen'
import WinScreen          from './screens/WinScreen'
import NumeracyScreen     from './screens/NumeracyScreen'
import NumberWatchScreen  from './screens/NumberWatchScreen'
import NumberTryScreen    from './screens/NumberTryScreen'
import ParentDashboard    from './screens/ParentDashboard'
import EducatorDashboard  from './screens/EducatorDashboard'
import ThinkingMenu       from './screens/thinking/ThinkingMenu'
import {
  DetectiveGame, SoundGame, PatternGame,
  RobotGame, MemoryGame, StoryGame,
} from './screens/thinking/ThinkingGames'

function AppRoutes() {
  const { themeClass, styleVars }   = useTheme()
  const [settingsOpen, setSettings] = useState(false)
  const sp = { onSettings: () => setSettings(true) }

  return (
    <div id="app" className={themeClass} style={styleVars}>
      <SettingsOverlay isOpen={settingsOpen} onClose={() => setSettings(false)} />
      <Routes>
        {/* Landing / demo picker */}
        <Route path="/login"    element={<LoginScreen />} />

        {/* Child flow */}
        <Route path="/home"     element={<HomeScreen {...sp} />} />
        <Route path="/watch"    element={<WatchScreen {...sp} />} />
        <Route path="/try"      element={<TryScreen {...sp} />} />
        <Route path="/guide"    element={<GuideScreen {...sp} />} />
        <Route path="/win"      element={<WinScreen {...sp} />} />

        {/* Numeracy flow */}
        <Route path="/numeracy"       element={<NumeracyScreen {...sp} />} />
        <Route path="/numwatch/:num"  element={<NumberWatchScreen {...sp} />} />
        <Route path="/numtry/:num"    element={<NumberTryScreen {...sp} />} />

        {/* Thinking skills */}
        <Route path="/thinking"            element={<ThinkingMenu {...sp} />} />
        <Route path="/thinking/detective"  element={<DetectiveGame {...sp} />} />
        <Route path="/thinking/sound"      element={<SoundGame {...sp} />} />
        <Route path="/thinking/pattern"    element={<PatternGame {...sp} />} />
        <Route path="/thinking/robot"      element={<RobotGame {...sp} />} />
        <Route path="/thinking/memory"     element={<MemoryGame {...sp} />} />
        <Route path="/thinking/story"      element={<StoryGame {...sp} />} />

        {/* Parent / Educator */}
        <Route path="/parent"    element={<ParentDashboard {...sp} />} />
        <Route path="/educator"  element={<EducatorDashboard {...sp} />} />

        {/* Default */}
        <Route path="*" element={<Navigate to="/login" />} />
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
