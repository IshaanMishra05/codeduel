import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Editor from '@monaco-editor/react'
import { api } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useWebSocket } from '../context/WebSocketContext'

const DEFAULT_CODE = `public class Solution {
    public static void main(String[] args) {
        // Write your solution here
    }
}
`

function difficultyBadge(difficulty) {
  const d = (difficulty || '').toUpperCase()
  if (d === 'EASY') return 'badge badge-easy'
  if (d === 'HARD') return 'badge badge-hard'
  return 'badge badge-medium'
}

export default function MatchPage() {
  const { matchId } = useParams()
  const navigate = useNavigate()
  const { currentUser, refreshUser } = useAuth()
  const { connected, subscribe, sendSubmit } = useWebSocket()

  const [match, setMatch] = useState(null)
  const [problem, setProblem] = useState(null)
  const [testCases, setTestCases] = useState([])
  const [players, setPlayers] = useState([])
  const [code, setCode] = useState(DEFAULT_CODE)
  const [myResult, setMyResult] = useState(null)
  const [opponentResult, setOpponentResult] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [finished, setFinished] = useState(null)
  const [preMatchElo, setPreMatchElo] = useState(null)
  const [eloChange, setEloChange] = useState(null)

  useEffect(() => {
    setPreMatchElo(currentUser?.eloRating ?? null)
  }, [])

  useEffect(() => {
    setLoading(true)
    Promise.all([api.getMatch(matchId), api.getMatchPlayers(matchId)])
      .then(async ([m, p]) => {
        setMatch(m)
        setPlayers(p)
        if (m.problemId) {
          const [prob, tc] = await Promise.all([
            api.getProblem(m.problemId),
            api.getTestCases(m.problemId),
          ])
          setProblem(prob)
          setTestCases(tc.filter((t) => !t.isHidden))
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [matchId])

  const opponent = players.find((p) => p.username !== currentUser?.username)

  const handleEvent = useCallback(
    async (event) => {
      if (event.type === 'SUBMISSION_RESULT') {
        if (event.userId === currentUser?.id) {
          setMyResult({
            testsPassed: event.testsPassed,
            totalTests: event.totalTests,
            status: event.status,
          })
          setSubmitting(false)
        } else {
          setOpponentResult({
            testsPassed: event.testsPassed,
            totalTests: event.totalTests,
            status: event.status,
          })
        }
      }

      if (event.type === 'MATCH_FINISHED') {
        const won = event.userId === currentUser?.id
        setFinished({ won, winnerId: event.userId })
        const updated = await refreshUser()
        if (updated && preMatchElo != null) {
          setEloChange(updated.eloRating - preMatchElo)
        }
      }
    },
    [currentUser?.id, preMatchElo, refreshUser]
  )

  useEffect(() => {
    if (!connected) return
    const unsub = subscribe(Number(matchId), handleEvent)
    return unsub
  }, [connected, matchId, subscribe, handleEvent])

  const handleSubmit = () => {
    setSubmitting(true)
    setError('')
    sendSubmit(Number(matchId), currentUser.id, code, 'java')
  }

  if (loading) return <div className="loading">Loading match...</div>

  return (
    <div style={{ display: 'flex', height: 'calc(100vh - 65px)', overflow: 'hidden' }}>
      <div
        style={{
          width: '40%',
          overflowY: 'auto',
          padding: '1.5rem',
          borderRight: '1px solid var(--border)',
        }}
      >
        {problem && (
          <>
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.75rem', flexWrap: 'wrap' }}>
              <span className={difficultyBadge(problem.difficulty)}>{problem.difficulty}</span>
              <span className="badge badge-lang">{problem.language}</span>
            </div>
            <h1 style={{ fontSize: '1.35rem', fontWeight: 700, marginBottom: '1rem' }}>{problem.title}</h1>
            <p style={{ color: 'var(--text-muted)', lineHeight: 1.7, whiteSpace: 'pre-wrap', marginBottom: '1.5rem' }}>
              {problem.description}
            </p>
          </>
        )}

        <h3 style={{ fontSize: '0.95rem', fontWeight: 600, marginBottom: '0.75rem' }}>Sample Tests</h3>
        {testCases.map((tc, i) => (
          <div
            key={tc.id}
            className="mono"
            style={{
              padding: '0.75rem',
              background: 'var(--card-bg)',
              borderRadius: '8px',
              marginBottom: '0.5rem',
              fontSize: '0.8rem',
              border: '1px solid var(--border)',
            }}
          >
            <div><span style={{ color: 'var(--text-muted)' }}>In:</span> {tc.input}</div>
            <div><span style={{ color: 'var(--text-muted)' }}>Out:</span> {tc.expectedOutput}</div>
          </div>
        ))}

        <div className="card" style={{ padding: '1rem', marginTop: '1.5rem' }}>
          <h3 style={{ fontSize: '0.9rem', fontWeight: 600, marginBottom: '0.75rem' }}>
            Opponent — {opponent?.username ?? 'Unknown'}
          </h3>
          <ResultPanel result={opponentResult} label="Latest submission" />
        </div>
      </div>

      <div style={{ width: '60%', display: 'flex', flexDirection: 'column' }}>
        <div style={{ flex: 1, minHeight: 0 }}>
          <Editor
            height="100%"
            language="java"
            theme="vs-dark"
            value={code}
            onChange={(v) => setCode(v ?? '')}
            options={{
              fontFamily: 'JetBrains Mono, monospace',
              fontSize: 14,
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              padding: { top: 16 },
            }}
          />
        </div>

        <div style={{ padding: '1rem 1.5rem', borderTop: '1px solid var(--border)', background: 'var(--card-bg)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '0.75rem' }}>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleSubmit}
              disabled={submitting || !connected}
            >
              {submitting ? 'Running...' : 'Submit'}
            </button>
            {!connected && (
              <span style={{ color: 'var(--amber)', fontSize: '0.85rem' }}>Reconnecting...</span>
            )}
            {error && <span className="error-msg">{error}</span>}
          </div>
          <ResultPanel result={myResult} label="Your results" />
        </div>
      </div>

      {finished && (
        <div className={`overlay-full ${finished.won ? 'overlay-victory' : 'overlay-defeat'}`}>
          <div className="overlay-title">{finished.won ? 'Victory' : 'Defeat'}</div>
          {eloChange != null && (
            <p style={{ fontSize: '1.25rem', marginBottom: '2rem', color: 'var(--text)' }}>
              ELO {eloChange >= 0 ? '+' : ''}{eloChange}
            </p>
          )}
          <button type="button" className="btn btn-primary" onClick={() => navigate('/problems')}>
            Back to Problems
          </button>
        </div>
      )}
    </div>
  )
}

function ResultPanel({ result, label }) {
  if (!result) {
    return (
      <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
        {label}: No submissions yet
      </p>
    )
  }

  const passed = result.testsPassed === result.totalTests && result.status === 'ACCEPTED'

  return (
    <div style={{ fontSize: '0.875rem' }}>
      <div style={{ color: 'var(--text-muted)', marginBottom: '0.35rem' }}>{label}</div>
      <div style={{ fontWeight: 600 }}>
        {result.testsPassed}/{result.totalTests} tests passed
      </div>
      <div
        style={{
          marginTop: '0.25rem',
          fontWeight: 600,
          color: passed ? 'var(--success)' : result.status === 'COMPILE_ERROR' ? 'var(--error)' : 'var(--amber)',
        }}
      >
        {result.status}
      </div>
    </div>
  )
}
