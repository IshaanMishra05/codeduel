import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api } from '../api/client'
import { getUsernameFromToken } from '../utils/jwt'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'))
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(!!localStorage.getItem('token'))

  const loadUser = useCallback(async (authToken) => {
    const username = getUsernameFromToken(authToken)
    if (!username) throw new Error('Invalid token')

    const users = await api.getUsers()
    const user = users.find((u) => u.username === username)
    if (!user) throw new Error('User not found')
    setCurrentUser(user)
    return user
  }, [])

  useEffect(() => {
    if (!token) {
      setCurrentUser(null)
      setLoading(false)
      return
    }

    setLoading(true)
    loadUser(token)
      .catch(() => {
        localStorage.removeItem('token')
        setToken(null)
        setCurrentUser(null)
      })
      .finally(() => setLoading(false))
  }, [token, loadUser])

  const login = async (username, password) => {
    const { token: newToken } = await api.login({ username, password })
    localStorage.setItem('token', newToken)
    setToken(newToken)
    await loadUser(newToken)
  }

  const register = async (username, email, password) => {
    const { token: newToken } = await api.register({ username, email, password })
    localStorage.setItem('token', newToken)
    setToken(newToken)
    await loadUser(newToken)
  }

  const logout = () => {
    localStorage.removeItem('token')
    setToken(null)
    setCurrentUser(null)
  }

  const refreshUser = async () => {
    if (!token || !currentUser) return null
    const user = await api.getUser(currentUser.id)
    if (user) setCurrentUser(user)
    return user
  }

  const value = useMemo(
    () => ({
      token,
      currentUser,
      loading,
      isAuthenticated: !!token && !!currentUser,
      login,
      register,
      logout,
      refreshUser,
    }),
    [token, currentUser, loading, loadUser]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
