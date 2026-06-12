import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { login, getChildren } from '../api/auth'

export default function LoginScreen() {
  const navigate     = useNavigate()
  const { login: setAuth } = useAuth()

  const [role, setRole]         = useState('child')   // 'child' | 'parent' | 'educator'
  const [children, setChildren] = useState([])
  const [pin, setPin]           = useState('')
  const [error, setError]       = useState('')
  const [loading, setLoading]   = useState(false)

  useEffect(() => {
    getChildren().then(setChildren).catch(() => {})
  }, [])

  async function handleChildSelect(child) {
    setLoading(true)
    try {
      const res = await login('child', null, child.studentId)
      if (res.ok) {
        setAuth(res.user, res.student)
        navigate('/home')
      } else {
        setError(res.error || 'Something went wrong')
      }
    } finally {
      setLoading(false)
    }
  }

  async function handlePinLogin() {
    if (!pin) return
    setError('')
    setLoading(true)
    try {
      const res = await login(role, pin)
      if (res.ok) {
        setAuth(res.user)
        navigate(role === 'parent' ? '/parent' : '/educator')
      } else {
        setError('Incorrect PIN — try again')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="screen-enter">
      <div className="top-bar">
        <div style={{ minWidth: 60 }} />
        <span className="title">NeuroFlow</span>
        <div style={{ minWidth: 60 }} />
      </div>

      {/* Role tabs */}
      <div className="role-bar">
        {[['child','🧒 Child'], ['parent','👨‍👩‍👧 Parent'], ['educator','🎓 Educator']].map(([r, label]) => (
          <button
            key={r}
            className={`role-btn ${role === r ? 'on' : ''}`}
            onClick={() => { setRole(r); setError(''); setPin('') }}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="content" style={{ paddingTop: 24 }}>

        {/* ── CHILD: pick your name ── */}
        {role === 'child' && (
          <>
            <div style={{ marginBottom: 20 }}>
              <div style={{ fontSize: 22, fontWeight: 800, color: 'var(--tx)', marginBottom: 4 }}>Who's learning today?</div>
              <div style={{ color: 'var(--sub)', fontSize: 14 }}>Tap your name to begin</div>
            </div>

            {children.length === 0 && (
              <div style={{ color: 'var(--sub)', textAlign: 'center', padding: '32px 0' }}>
                Loading students…
              </div>
            )}

            {children.map((child) => (
              <button
                key={child.studentId}
                className="module-tile"
                onClick={() => handleChildSelect(child)}
                disabled={loading}
              >
                <div
                  className="m-icon"
                  style={{ background: 'var(--acl)', color: 'var(--ac)', fontWeight: 800, fontSize: 18 }}
                >
                  {child.initials || child.name?.slice(0,2).toUpperCase()}
                </div>
                <div>
                  <div style={{ fontWeight: 800, fontSize: 17, color: 'var(--tx)' }}>{child.name}</div>
                  <div style={{ color: 'var(--sub)', fontSize: 13, marginTop: 2 }}>
                    Working on: <strong>{child.currentLetter || 'b'}</strong>
                    &nbsp;·&nbsp; 🔥 {child.streakDays || 0} day streak
                  </div>
                </div>
                <span className="m-arrow">›</span>
              </button>
            ))}
          </>
        )}

        {/* ── PARENT / EDUCATOR: enter PIN ── */}
        {(role === 'parent' || role === 'educator') && (
          <>
            <div style={{ marginBottom: 24 }}>
              <div style={{ fontSize: 22, fontWeight: 800, color: 'var(--tx)', marginBottom: 4 }}>
                {role === 'parent' ? '👨‍👩‍👧 Parent login' : '🎓 Educator login'}
              </div>
              <div style={{ color: 'var(--sub)', fontSize: 14 }}>Enter your PIN to continue</div>
            </div>

            <div className="card">
              <div style={{ marginBottom: 16 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--sub)', marginBottom: 8 }}>YOUR PIN</div>
                <input
                  type="password"
                  inputMode="numeric"
                  maxLength={6}
                  value={pin}
                  onChange={(e) => setPin(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handlePinLogin()}
                  placeholder="Enter PIN"
                  style={{
                    width: '100%',
                    padding: '14px 16px',
                    borderRadius: 12,
                    border: `1.5px solid ${error ? 'var(--er)' : 'var(--bd)'}`,
                    background: 'var(--alt)',
                    fontSize: 24,
                    fontWeight: 700,
                    letterSpacing: '0.3em',
                    color: 'var(--tx)',
                    outline: 'none',
                  }}
                />
              </div>

              {error && (
                <div style={{ color: 'var(--er)', fontSize: 13, marginBottom: 12, fontWeight: 600 }}>
                  {error}
                </div>
              )}

              <button
                className="btn btn-ac"
                onClick={handlePinLogin}
                disabled={loading || !pin}
                style={{ marginBottom: 0 }}
              >
                {loading ? 'Checking…' : 'Continue →'}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
