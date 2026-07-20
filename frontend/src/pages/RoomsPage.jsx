import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function RoomsPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const preselectedProblemId = searchParams.get('problemId')
  const { currentUser } = useAuth()

  const [problems, setProblems] = useState([])
  const [selectedProblemId, setSelectedProblemId] = useState(preselectedProblemId || '')
  const [roomCode, setRoomCode] = useState('')
  const [createdRoomCode, setCreatedRoomCode] = useState('')
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [joining, setJoining] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .getProblems()
      .then((p) => {
        setProblems(p)
        if (preselectedProblemId) {
          setSelectedProblemId(preselectedProblemId)
        } else if (p.length > 0) {
          setSelectedProblemId(String(p[0].id))
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [preselectedProblemId])

  const handleCreate = async () => {
    if (!selectedProblemId) return
    setCreating(true)
    setError('')
    setCreatedRoomCode('')
    try {
      const match = await api.createMatch({ problemId: Number(selectedProblemId) })
      await api.joinMatch({ matchId: match.id, userId: currentUser.id })
      setCreatedRoomCode(match.roomCode)
      navigate(`/rooms/${match.id}`)
    } catch (err) {
      setError(err.message)
    } finally {
      setCreating(false)
    }
  }

  const handleJoin = async () => {
    if (!roomCode.trim()) return
    setJoining(true)
    setError('')
    try {
      const match = await api.getMatchByRoom(roomCode.trim().toUpperCase())
      await api.joinMatch({ matchId: match.id, userId: currentUser.id })
      navigate(`/rooms/${match.id}`)
    } catch (err) {
      setError(err.message)
    } finally {
      setJoining(false)
    }
  }

  if (loading) return <div className="loading">Loading...</div>

  return (
    <div className="page">
      <h1 className="page-title">Rooms</h1>
      <p className="page-subtitle">Create a duel room or join an existing one</p>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '1.5rem',
        }}
      >
        <div className="card" style={{ padding: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1.25rem' }}>Create Room</h2>
          <div style={{ marginBottom: '1rem' }}>
            <label className="label">Select Problem</label>
            <select
              className="input"
              value={selectedProblemId}
              onChange={(e) => setSelectedProblemId(e.target.value)}
            >
              {problems.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.title}
                </option>
              ))}
            </select>
          </div>
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleCreate}
            disabled={creating || !selectedProblemId}
          >
            {creating ? 'Creating...' : 'Create Room'}
          </button>
          {createdRoomCode && (
            <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '0.5rem' }}>
                Share this room code
              </p>
              <div
                className="mono"
                style={{
                  fontSize: '2.5rem',
                  fontWeight: 700,
                  letterSpacing: '0.15em',
                  color: 'var(--accent)',
                }}
              >
                {createdRoomCode}
              </div>
            </div>
          )}
        </div>

        <div className="card" style={{ padding: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '1.25rem' }}>Join Room</h2>
          <div style={{ marginBottom: '1rem' }}>
            <label className="label">Room Code</label>
            <input
              className="input mono"
              value={roomCode}
              onChange={(e) => setRoomCode(e.target.value.toUpperCase())}
              placeholder="ABC123"
              maxLength={6}
              style={{ textTransform: 'uppercase', letterSpacing: '0.1em' }}
            />
          </div>
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleJoin}
            disabled={joining || !roomCode.trim()}
          >
            {joining ? 'Finding...' : 'Find Room'}
          </button>
        </div>
      </div>

      {error && <p className="error-msg" style={{ marginTop: '1.5rem' }}>{error}</p>}
    </div>
  )
}
