const BASE = '/api'

export async function login(role, pin = null, studentId = null) {
  const res = await fetch(`${BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ role, pin, studentId }),
  })
  return res.json()
}

export async function getChildren() {
  const res = await fetch(`${BASE}/auth/children`)
  return res.json()
}
