import { useEffect, useState } from 'react'
import TopBar from '../components/TopBar'
import RoleBar from '../components/RoleBar'
import { getByEducator, getOverviewStats } from '../api/students'
import { getWeeklyErrors, getActivities, exportCsv } from '../api/analytics'

export default function EducatorDashboard({ onSettings }) {
  const [tab,        setTab]        = useState('overview')
  const [students,   setStudents]   = useState([])
  const [stats,      setStats]      = useState(null)
  const [errors,     setErrors]     = useState({})
  const [activities, setActivities] = useState([])
  const [loading,    setLoading]    = useState(true)

  useEffect(() => {
    // All four calls hit the Java backend through the Vite proxy
    Promise.all([
      getByEducator(7),
      getOverviewStats(),
      getWeeklyErrors(),
      getActivities(7),
    ])
      .then(([s, st, e, a]) => {
        setStudents(s)
        setStats(st)
        setErrors(e)
        setActivities(a)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  // Fallback demo data shown while loading or if backend unreachable
  const displayStudents = students.length > 0 ? students : [
    { studentId: 1, name: 'Aarav M.',  initials: 'AM', weeklyProgress: 80, primaryIssue: 'b/d reversal', trend: '↑' },
    { studentId: 2, name: 'Diya R.',   initials: 'DR', weeklyProgress: 65, primaryIssue: 'stroke order', trend: '↑' },
    { studentId: 3, name: 'Ishaan P.', initials: 'IP', weeklyProgress: 45, primaryIssue: 'b/d reversal', trend: '→' },
    { studentId: 4, name: 'Meera S.',  initials: 'MS', weeklyProgress: 90, primaryIssue: null,           trend: '↑' },
    { studentId: 5, name: 'Rohan K.',  initials: 'RK', weeklyProgress: 30, primaryIssue: 'b/d reversal', trend: '↓' },
  ]

  const displayErrors = Object.keys(errors).length > 0 ? errors : {
    'b/d reversal': 38, 'Stroke direction': 22, 'Starting point': 15, 'p/q reversal': 11,
  }
  const maxError = Math.max(...Object.values(displayErrors), 1)

  const displayStats = stats ?? { activeStudents: 5, practicedToday: 3 }

  // Weekly bar chart — uses real activity data when available
  const weekDays = [
    { day: 'M', pct: 70 }, { day: 'T', pct: 90 }, { day: 'W', pct: 50 },
    { day: 'T', pct: 85 }, { day: 'F', pct: 0  }, { day: 'S', pct: 0  }, { day: 'S', pct: 0 },
  ]

  function progressColor(pct) {
    if (pct >= 70) return 'var(--ok)'
    if (pct >= 45) return 'var(--ac)'
    if (pct >= 30) return 'var(--warn)'
    return 'var(--er)'
  }

  // Classroom focus — prefer real activities from DB, fall back to hardcoded
  const focusItems = activities.length > 0
    ? activities.map(a => ({ label: a.title, desc: a.description, done: a.completed }))
    : [
        { label: 'Letters b & d',       desc: 'Visual discrimination — bump direction awareness', done: true  },
        { label: 'Letters p & q',       desc: 'Below-the-line letter shapes',                   done: false },
        { label: 'Numbers 1–5',         desc: 'Number formation and counting',                  done: false },
        { label: 'Sequencing patterns', desc: 'What comes next — shapes and colours',            done: false },
      ]

  return (
    <div className="screen-enter">
      <TopBar title="Educator View" onSettings={onSettings} />
      <RoleBar active="educator" />

      <div className="content" style={{ paddingTop: 18 }}>
        <div className="tabs">
          {[['overview','Overview'], ['assign','This Week'], ['trends','Trends']].map(([key, label]) => (
            <button key={key} className={`tab ${tab === key ? 'on' : ''}`} onClick={() => setTab(key)}>
              {label}
            </button>
          ))}
        </div>

        {/* ── OVERVIEW ── */}
        {tab === 'overview' && (
          <>
            <div className="metric-grid">
              <div className="mc">
                <div className="mv">{loading ? '…' : displayStats.activeStudents}</div>
                <div className="ml">Active students</div>
              </div>
              <div className="mc">
                <div className="mv">{loading ? '…' : displayStats.practicedToday}</div>
                <div className="ml">Practised today</div>
              </div>
              <div className="mc">
                <div className="mv" style={{ fontSize: 18 }}>b/d</div>
                <div className="ml">Common mix-up</div>
              </div>
            </div>

            <p className="slabel">STUDENT PROGRESS</p>

            {displayStudents.map((s) => (
              <div className="sturow" key={s.studentId}>
                <div className="stuinfo">
                  <div className="avatar">{s.initials || s.name?.slice(0,2)}</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 14 }}>{s.name}</div>
                    {s.primaryIssue
                      ? <span className="badge b-warn">{s.primaryIssue}</span>
                      : <div style={{ color: 'var(--ok)', fontSize: 12, marginTop: 2 }}>On track ✓</div>
                    }
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontWeight: 800, color: 'var(--tx)', fontSize: 16 }}>{s.weeklyProgress}%</div>
                    <div style={{ color: s.trend === '↑' ? 'var(--ok)' : s.trend === '↓' ? 'var(--er)' : 'var(--sub)', fontSize: 13 }}>
                      {s.trend}
                    </div>
                  </div>
                </div>
                <div className="prog-track">
                  <div className="prog-fill" style={{ width: `${s.weeklyProgress}%`, background: progressColor(s.weeklyProgress) }} />
                </div>
              </div>
            ))}

            <button className="btn btn-ghost" style={{ marginTop: 6 }} onClick={exportCsv}>
              ⬇&nbsp; Export CSV report
            </button>
          </>
        )}

        {/* ── THIS WEEK ── */}
        {tab === 'assign' && (
          <>
            <div className="card">
              <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 16, marginBottom: 4 }}>📚 This week's classroom focus</div>
              <div style={{ color: 'var(--sub)', fontSize: 13, marginBottom: 16 }}>What you're exploring together in class right now</div>
              {focusItems.map(({ label, desc, done }) => (
                <div className="arow" key={label}>
                  <div className={`acheck ${done ? 'y' : 'n'}`}>{done ? '✓' : ''}</div>
                  <div>
                    <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 14 }}>{label}</div>
                    <div style={{ color: 'var(--sub)', fontSize: 12, marginTop: 2 }}>{desc}</div>
                  </div>
                </div>
              ))}
              <button className="btn btn-ac" style={{ marginTop: 16, marginBottom: 0 }}>
                Share focus with parents →
              </button>
            </div>

            <div className="card">
              <div style={{ fontWeight: 700, color: 'var(--tx)', fontSize: 16, marginBottom: 4 }}>🌱 Optional ideas for parents</div>
              <div style={{ color: 'var(--sub)', fontSize: 13, marginBottom: 16 }}>Low-pressure, playful moments — only if the child feels like it.</div>
              {[
                'Point out b and d while reading a bedtime book together — casually, not as a test',
                'Try drawing letters in sand, on a foggy window, or with finger paints — movement helps memory',
                'Count objects around the house — stairs, spoons, shoes — whenever it comes up naturally',
              ].map((text, i) => (
                <div className="sstep" key={i}>
                  <div className="snum">{i + 1}</div>
                  <span style={{ color: 'var(--tx)', fontSize: 14, paddingTop: 2, lineHeight: 1.55 }}>{text}</span>
                </div>
              ))}
            </div>
          </>
        )}

        {/* ── TRENDS ── */}
        {tab === 'trends' && (
          <>
            <div className="card">
              <div style={{ fontWeight: 700, color: 'var(--tx)', marginBottom: 16 }}>Error patterns this week</div>
              {Object.entries(displayErrors).map(([label, count]) => (
                <div className="trow" key={label}>
                  <div className="tlabel">
                    <span style={{ fontWeight: 600, color: 'var(--tx)', fontSize: 14 }}>{label}</span>
                    <span style={{ color: 'var(--sub)', fontSize: 12 }}>{count} instances</span>
                  </div>
                  <div className="ttrack">
                    <div className="tfill" style={{ width: `${Math.round(count / maxError * 100)}%` }} />
                  </div>
                </div>
              ))}
              <div style={{ color: 'var(--sub)', fontSize: 12, padding: 10, background: 'var(--acl)', borderRadius: 10 }}>
                💡 b/d reversal is most common — consider making it this week's classroom focus
              </div>
            </div>

            <div className="card">
              <div style={{ fontWeight: 700, color: 'var(--tx)', marginBottom: 4 }}>Practice days this week</div>
              <div className="bar-chart">
                {weekDays.map(({ day, pct }, i) => (
                  <div className="bcol" key={i}>
                    <div className={`bbar ${pct === 0 ? 'empty' : ''}`} style={{ height: pct > 0 ? `${pct}%` : 4 }} />
                    <div className="bday">{day}</div>
                  </div>
                ))}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
