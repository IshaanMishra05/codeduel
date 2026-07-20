import { useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function AuthPage() {
  const { login, register, isAuthenticated, loading } = useAuth()
  const [tab, setTab] = useState('login')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  if (isAuthenticated) {
    return <Navigate to="/problems" replace />
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      if (tab === 'login') {
        await login(username, password)
      } else {
        await register(username, email, password)
      }
    } catch (err) {
      setError(err.message || 'Something went wrong')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '1.5rem',
      }}
    >
      <div className="card" style={{ width: '100%', maxWidth: 420, padding: '2.5rem' }}>
        <h1
          style={{
            fontSize: '2rem',
            fontWeight: 700,
            textAlign: 'center',
            marginBottom: '0.25rem',
            background: 'linear-gradient(135deg, #7c3aed, #a78bfa)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}
        >
          CodeDuel
        </h1>
        <p
          style={{
            textAlign: 'center',
            color: 'var(--text-muted)',
            marginBottom: '2rem',
            fontSize: '0.95rem',
          }}
        >
          Real-time 1v1 competitive coding
        </p>

        <div
          style={{
            display: 'flex',
            background: 'var(--bg)',
            borderRadius: 'var(--radius)',
            padding: '4px',
            marginBottom: '1.75rem',
          }}
        >
          {['login', 'register'].map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => {
                setTab(t)
                setError('')
              }}
              style={{
                flex: 1,
                padding: '0.6rem',
                borderRadius: '8px',
                fontWeight: 600,
                fontSize: '0.9rem',
                background: tab === t ? 'var(--accent)' : 'transparent',
                color: tab === t ? 'white' : 'var(--text-muted)',
                transition: 'all 0.2s ease',
              }}
            >
              {t === 'login' ? 'Sign In' : 'Create Account'}
            </button>
          ))}
        </div>

        <form onSubmit={handleSubmit} className="form-stack">
          <div>
            <label className="label" htmlFor="username">
              Username
            </label>
            <input
              id="username"
              className="input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="username"
            />
          </div>

          {tab === 'register' && (
            <div>
              <label className="label" htmlFor="email">
                Email
              </label>
              <input
                id="email"
                type="email"
                className="input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </div>
          )}

          <div>
            <label className="label" htmlFor="password">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete={tab === 'login' ? 'current-password' : 'new-password'}
            />
          </div>

          {error && <p className="error-msg">{error}</p>}

          <button type="submit" className="btn btn-primary" disabled={submitting} style={{ marginTop: '0.5rem' }}>
            {submitting ? 'Please wait...' : tab === 'login' ? 'Sign In' : 'Create Account'}
          </button>
        </form>
      </div>
    </div>
  )
}
