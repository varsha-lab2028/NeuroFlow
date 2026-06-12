import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)  // { userId, name, role }
  const [student, setStudent] = useState(null)  // current child's student record

  function login(userData, studentData = null) {
    setUser(userData)
    setStudent(studentData)
  }

  function logout() {
    setUser(null)
    setStudent(null)
  }

  return (
    <AuthContext.Provider value={{ user, student, setStudent, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
