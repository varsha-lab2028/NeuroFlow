const BASE = '/api/analytics'

export async function getWeeklyErrors() {
  const res = await fetch(`${BASE}/errors/weekly`)
  return res.json()
}

export async function getParentSummary(studentId) {
  const res = await fetch(`${BASE}/parent-summary/${studentId}`)
  return res.json()
}

export async function getActivities(educatorId = 7) {
  const res = await fetch(`${BASE}/activities?educatorId=${educatorId}`)
  return res.json()
}

export async function createActivity(activity) {
  const res = await fetch(`${BASE}/activities`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(activity),
  })
  return res.json()
}

export async function markActivityComplete(id, completed = true) {
  const res = await fetch(`${BASE}/activities/${id}/complete`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ completed }),
  })
  return res.json()
}

export async function shareActivity(id, shared = true) {
  const res = await fetch(`${BASE}/activities/${id}/share`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ shared }),
  })
  return res.json()
}

export async function exportCsv() {
  const res = await fetch(`${BASE}/export/csv`)
  const blob = await res.blob()
  const url  = URL.createObjectURL(blob)
  const a    = document.createElement('a')
  a.href     = url
  a.download = `NeuroFlow_Report_${new Date().toISOString().slice(0,10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
