import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import { useAuth } from '../context/AuthContext'

function difficultyBadge(difficulty) {
  const d = (difficulty || '').toUpperCase()
  if (d === 'EASY') return 'badge badge-easy'
  if (d === 'HARD') return 'badge badge-hard'
  return 'badge badge-medium'
}

export default function ProblemsPage() {
  const { currentUser } = useAuth()
  const [problems, setProblems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [form, setForm] = useState({
    title: '',
    description: '',
    difficulty: 'EASY',
    language: 'java',
  })
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const loadProblems = () => {
    setLoading(true)
    setError('')
    api
      .getProblems()
      .then(setProblems)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadProblems()
  }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setFormError('')
    setSubmitting(true)
    try {
      await api.createProblem(form)
      setDrawerOpen(false)
      setForm({ title: '', description: '', difficulty: 'EASY', language: 'java' })
      loadProblems()
    } catch (err) {
      setFormError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page">
      <h1 className="page-title">Problems</h1>
      <p className="page-subtitle">Pick a challenge and enter the arena</p>

      {loading && <div className="loading">Loading problems...</div>}
      {error && <p className="error-msg">{error}</p>}

      {!loading && !error && (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
            gap: '1.25rem',
          }}
        >
          {problems.map((p) => (
            <Link key={p.id} to={`/problems/${p.id}`} className="card card-interactive" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '0.75rem' }}>{p.title}</h3>
              <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                <span className={difficultyBadge(p.difficulty)}>{p.difficulty}</span>
                <span className="badge badge-lang">{p.language}</span>
              </div>
            </Link>
          ))}
        </div>
      )}

      {currentUser?.role === 'ADMIN' && (
        <>
          <button
            type="button"
            className="fab"
            onClick={() => setDrawerOpen(true)}
            title="Create Problem"
          >
            +
          </button>

          {drawerOpen && (
            <>
              <div className="drawer-overlay" onClick={() => setDrawerOpen(false)} />
              <div className="drawer">
                <h2 style={{ fontSize: '1.35rem', fontWeight: 700, marginBottom: '1.5rem' }}>
                  Create Problem
                </h2>
                <form onSubmit={handleCreate} className="form-stack">
                  <div>
                    <label className="label">Title</label>
                    <input
                      className="input"
                      value={form.title}
                      onChange={(e) => setForm({ ...form, title: e.target.value })}
                      required
                    />
                  </div>
                  <div>
                    <label className="label">Description</label>
                    <textarea
                      className="input"
                      rows={5}
                      value={form.description}
                      onChange={(e) => setForm({ ...form, description: e.target.value })}
                      required
                      style={{ resize: 'vertical' }}
                    />
                  </div>
                  <div>
                    <label className="label">Difficulty</label>
                    <select
                      className="input"
                      value={form.difficulty}
                      onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
                    >
                      <option value="EASY">Easy</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HARD">Hard</option>
                    </select>
                  </div>
                  <div>
                    <label className="label">Language</label>
                    <input
                      className="input"
                      value={form.language}
                      onChange={(e) => setForm({ ...form, language: e.target.value })}
                      required
                    />
                  </div>
                  {formError && <p className="error-msg">{formError}</p>}
                  <div className="form-actions">
                    <button type="submit" className="btn btn-primary" disabled={submitting}>
                      {submitting ? 'Creating...' : 'Create'}
                    </button>
                    <button type="button" className="btn btn-ghost" onClick={() => setDrawerOpen(false)}>
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            </>
          )}
        </>
      )}
    </div>
  )
}
