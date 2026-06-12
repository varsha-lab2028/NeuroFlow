const BASE = '/api/students'

export async function getStudent(id) {
  const res = await fetch(`${BASE}/${id}`)
  return res.json()
}

export async function getHomeData(studentId) {
  const res = await fetch(`${BASE}/${studentId}/home-data`)
  return res.json()
}

export async function getByParent(parentId) {
  const res = await fetch(`${BASE}/by-parent/${parentId}`)
  return res.json()
}

export async function getByEducator(educatorId) {
  const res = await fetch(`${BASE}/by-educator/${educatorId}`)
  return res.json()
}

export async function getOverviewStats() {
  const res = await fetch(`${BASE}/stats/overview`)
  return res.json()
}
