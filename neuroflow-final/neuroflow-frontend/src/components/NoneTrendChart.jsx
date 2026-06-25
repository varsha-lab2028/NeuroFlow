/**
 * NoneTrendChart — Correction buzz trend for Parent & Educator dashboards.
 * Uses a lightweight SVG line chart (no recharts dependency needed).
 * Install recharts if you want the full Recharts version from the handover doc.
 */
import { useEffect, useState } from 'react'
import { getNoneTrend } from '../api/practice'

function fillMissingDays(apiData, days = 14) {
  const map = {}
  apiData.forEach(d => { map[d.date] = d.count })
  return Array.from({ length: days }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (days - 1 - i))
    const iso   = d.toISOString().split('T')[0]
    const label = d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
    return { date: iso, label, count: map[iso] ?? 0 }
  })
}

function TrendBadge({ direction }) {
  const map = {
    improving:       { label: '↓ Improving',      color: '#3A9462', bg: '#EBF5EE' },
    stable:          { label: '→ Stable',          color: '#4E8FC5', bg: '#EEF4FA' },
    needs_attention: { label: '↑ Needs attention', color: '#C85C3A', bg: '#FEF3EE' },
  }
  const c = map[direction] || map.stable
  return (
    <span style={{
      background: c.bg, color: c.color, borderRadius: 20,
      padding: '3px 10px', fontSize: 12, fontWeight: 700,
    }}>
      {c.label}
    </span>
  )
}

/** SVG sparkline — no external lib required */
function SparkLine({ data, width = 320, height = 120 }) {
  const max   = Math.max(...data.map(d => d.count), 1)
  const pts   = data.map((d, i) => {
    const x = (i / (data.length - 1)) * width
    const y = height - (d.count / max) * height * 0.85 - 8
    return `${x},${y}`
  })
  const polyline = pts.join(' ')

  // area fill path
  const first = pts[0].split(',')
  const last  = pts[pts.length - 1].split(',')
  const area  = `M ${polyline.replace(/,/g, ' ').replace(/ (\d)/g, ' L $1')} L ${last[0]} ${height} L ${first[0]} ${height} Z`
    .replace('M ', 'M ')

  return (
    <svg viewBox={`0 0 ${width} ${height}`} width="100%" height={height}
         style={{ display: 'block', overflow: 'visible' }}>
      <defs>
        <linearGradient id="spark-fill" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="var(--ac)" stopOpacity="0.18" />
          <stop offset="100%" stopColor="var(--ac)" stopOpacity="0.02" />
        </linearGradient>
      </defs>
      {/* area */}
      <path d={`M ${pts.map(p => p.replace(',', ' ')).join(' L ')} L ${last[0]} ${height} L ${first[0]} ${height} Z`}
            fill="url(#spark-fill)" />
      {/* line */}
      <polyline points={polyline} fill="none" stroke="var(--ac)" strokeWidth="2.5"
                strokeLinecap="round" strokeLinejoin="round" />
      {/* dots */}
      {data.map((d, i) => {
        const [cx, cy] = pts[i].split(',')
        return d.count > 0
          ? <circle key={i} cx={cx} cy={cy} r="4" fill="var(--ac)" />
          : null
      })}
      {/* x-axis labels — every 7th */}
      {data.map((d, i) => {
        if (i % 7 !== 0 && i !== data.length - 1) return null
        const x = (i / (data.length - 1)) * width
        return (
          <text key={i} x={x} y={height + 14} textAnchor="middle"
                fontSize="10" fill="var(--sub)">{d.label}</text>
        )
      })}
    </svg>
  )
}

export default function NoneTrendChart({ studentId, studentName }) {
  const [data,      setData]      = useState([])
  const [direction, setDirection] = useState('stable')
  const [loading,   setLoading]   = useState(true)

  useEffect(() => {
    if (!studentId) return
    const load = () => {
      getNoneTrend(studentId)
        .then(json => {
          setDirection(json.direction || 'stable')
          setData(fillMissingDays(json.data || []))
          setLoading(false)
        })
        .catch(() => setLoading(false))
    }
    load()
    const iv = setInterval(load, 30000)
    return () => clearInterval(iv)
  }, [studentId])

  const avg7 = data.length
    ? Math.round(data.slice(-7).reduce((s, d) => s + d.count, 0) / Math.min(7, data.length))
    : 0

  if (loading) return (
    <div className="card">
      <p style={{ color: 'var(--sub)', fontSize: 13 }}>Loading correction buzz trend…</p>
    </div>
  )

  const hasData = data.some(d => d.count > 0)

  return (
    <div className="card">
      <div style={{
        display: 'flex', justifyContent: 'space-between',
        alignItems: 'flex-start', marginBottom: 12,
      }}>
        <div>
          <h3 style={{ margin: 0, fontSize: 15, fontWeight: 700 }}>
            📉 Correction buzz trend
            {studentName && <span style={{ fontWeight: 400, color: 'var(--sub)', fontSize: 13 }}> — {studentName}</span>}
          </h3>
          <p style={{ margin: '4px 0 0', fontSize: 12, color: 'var(--sub)' }}>
            Downward = fewer correction buzzes = more improvement
          </p>
        </div>
        <TrendBadge direction={direction} />
      </div>

      {!hasData ? (
        <p style={{ color: 'var(--sub)', fontSize: 13 }}>
          No correction buzzes recorded yet — buzzes appear here as practice sessions happen.
        </p>
      ) : (
        <div style={{ marginBottom: 14, paddingBottom: 18 }}>
          <SparkLine data={data} />
        </div>
      )}

      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
        {[
          { label: 'Last 7 days',       value: data.slice(-7).reduce((s, d) => s + d.count, 0) },
          { label: 'Today',             value: data[data.length - 1]?.count ?? 0 },
          { label: '7-day avg',         value: avg7 },
        ].map(({ label, value }) => (
          <div key={label} style={{
            flex: 1, minWidth: 80, background: 'var(--alt)',
            borderRadius: 12, padding: '8px 12px',
          }}>
            <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--tx)' }}>{value}</div>
            <div style={{ fontSize: 10, color: 'var(--sub)', marginTop: 2 }}>{label}</div>
          </div>
        ))}
      </div>
    </div>
  )
}
