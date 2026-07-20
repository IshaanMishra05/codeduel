import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import { useAuth } from '../context/AuthContext'

function difficultyBadge(difficulty) {
  const d = (difficulty || '').toUpperCase()
  if (d === 'EASY') return 'badge badge-easy'
  if (d === 'HARD') return 'badge badge-hard'
  return 'badge badge-medium'
}

export default function ProblemDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { currentUser } = useAuth()
  const [problem, setProblem] = useState(null)
  const [testCases, setTestCases] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    setLoading(true)
    setError('')
    Promise.all([api.getProblem(id), api.getTestCases(id)])
      .then(([p, tc]) => {
        setProblem(p)
        setTestCases(tc)
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  const handleDelete = async () => {
    if (!window.confirm('Delete this problem?')) return
    setDeleting(true)
    try {
      await api.deleteProblem(id)
      navigate('/problems')
    } catch (err) {
      setError(err.message)
      setDeleting(false)
    }
  }

  if (loading) return <div className="loading">Loading problem...</div>
  if (error && !problem) return <div className="page"><p className="error-msg">{error}</p></div>
  if (!problem) return null

  return (
    <div className="page">
      <Link to="/problems" style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
        ← Back to problems
      </Link>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '2rem',
          marginTop: '1.5rem',
        }}
      >
        <div className="card" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
            <span className={difficultyBadge(problem.difficulty)}>{problem.difficulty}</span>
            <span className="badge badge-lang">{problem.language}</span>
          </div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem' }}>{problem.title}</h1>
          <p style={{ color: 'var(--text-muted)', lineHeight: 1.7, whiteSpace: 'pre-wrap' }}>
            {problem.description}
          </p>
          {problem.createdBy && (
            <p style={{ marginTop: '1.5rem', fontSize: '0.875rem', color: 'var(--text-muted)' }}>
              Created by <strong style={{ color: 'var(--text)' }}>{problem.createdBy}</strong>
            </p>
          )}
          {currentUser?.role === 'ADMIN' && (
            <button
              type="button"
              className="btn btn-ghost"
              onClick={handleDelete}
              disabled={deleting}
              style={{ marginTop: '1.5rem', borderColor: 'var(--error)', color: 'var(--error)' }}
            >
              {deleting ? 'Deleting...' : 'Delete Problem'}
            </button>
          )}
        </div>

        <div className="card" style={{ padding: '2rem' }}>
          <h2 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>Test Cases</h2>
          {testCases.length === 0 && (
            <p style={{ color: 'var(--text-muted)' }}>No visible test cases</p>
          )}
          {testCases.map((tc, i) => (
            <div
              key={tc.id}
              style={{
                padding: '1rem',
                background: 'var(--bg)',
                borderRadius: '8px',
                marginBottom: '0.75rem',
                border: '1px solid var(--border)',
              }}
            >
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                Case {i + 1}{tc.isHidden ? ' (hidden)' : ''}
              </div>
              <div className="mono" style={{ fontSize: '0.85rem' }}>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Input: </span>
                  {tc.input}
                </div>
                {!tc.isHidden ? (
                  <div style={{ marginTop: '0.35rem' }}>
                    <span style={{ color: 'var(--text-muted)' }}>Expected: </span>
                    {tc.expectedOutput}
                  </div>
                ) : (
                  <div style={{ marginTop: '0.35rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
                    Expected output hidden
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ marginTop: '2rem', textAlign: 'center' }}>
        <button
          type="button"
          className="btn btn-primary"
          style={{ padding: '0.85rem 2rem', fontSize: '1rem' }}
          onClick={() => navigate(`/rooms?problemId=${id}`)}
        >
          Duel on this problem
        </button>
      </div>

      {error && <p className="error-msg" style={{ marginTop: '1rem', textAlign: 'center' }}>{error}</p>}
    </div>
  )
}
