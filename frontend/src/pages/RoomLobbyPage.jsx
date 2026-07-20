import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useWebSocket } from '../context/WebSocketContext'

export default function RoomLobbyPage() {
  const { matchId } = useParams()
  const navigate = useNavigate()
  const { currentUser } = useAuth()
  const { connected, subscribe, sendReady } = useWebSocket()

  const [match, setMatch] = useState(null)
  const [players, setPlayers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [readySent, setReadySent] = useState(false)
  const [readyLoading, setReadyLoading] = useState(false)

  const loadData = useCallback(() => {
    Promise.all([api.getMatch(matchId), api.getMatchPlayers(matchId)])
      .then(([m, p]) => {
        setMatch(m)
        setPlayers(p)
        if (m.status === 'ACTIVE') {
          navigate(`/match/${matchId}`, { replace: true })
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [matchId, navigate])

  useEffect(() => {
    loadData()
  }, [loadData])

  const handleEvent = useCallback(
    (event) => {
      if (event.type === 'PLAYER_READY') {
        loadData()
      }
      if (event.type === 'MATCH_STARTED') {
        navigate(`/match/${matchId}`, { replace: true })
      }
    },
    [loadData, matchId, navigate]
  )

  useEffect(() => {
    if (!connected) return
    const unsub = subscribe(Number(matchId), handleEvent)
    return unsub
  }, [connected, matchId, subscribe, handleEvent])

  const handleReady = () => {
    setReadyLoading(true)
    sendReady(Number(matchId), currentUser.id)
    setReadySent(true)
    setReadyLoading(false)
  }

  const slot1 = players[0]
  const slot2 = players[1]

  const isCurrentReady = (player) =>
    player?.username === currentUser?.username && readySent
      ? true
      : player?.isReady

  if (loading) return <div className="loading">Loading lobby...</div>

  return (
    <div
      style={{
        minHeight: 'calc(100vh - 65px)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '2rem',
      }}
    >
      {match && (
        <p style={{ color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
          {match.problemTitle}
        </p>
      )}
      {match?.roomCode && (
        <p className="mono" style={{ color: 'var(--accent)', marginBottom: '2.5rem', letterSpacing: '0.1em' }}>
          Room {match.roomCode}
        </p>
      )}

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '3rem',
          width: '100%',
          maxWidth: 800,
          justifyContent: 'center',
        }}
      >
        <PlayerSlot
          player={slot1}
          label="Player 1"
          isReady={isCurrentReady(slot1)}
          isYou={slot1?.username === currentUser?.username}
        />

        <div
          style={{
            fontSize: '2.5rem',
            fontWeight: 800,
            color: 'var(--accent)',
            animation: 'pulse 2s ease-in-out infinite',
          }}
        >
          VS
        </div>

        <PlayerSlot
          player={slot2}
          label="Player 2"
          isReady={isCurrentReady(slot2)}
          isYou={slot2?.username === currentUser?.username}
          waiting={!slot2}
        />
      </div>

      {!connected && (
        <p style={{ color: 'var(--amber)', marginTop: '2rem', fontSize: '0.875rem' }}>
          Connecting to live server...
        </p>
      )}

      {error && <p className="error-msg" style={{ marginTop: '1.5rem' }}>{error}</p>}

      <button
        type="button"
        className="btn btn-primary"
        style={{ marginTop: '3rem', padding: '0.85rem 3rem', fontSize: '1rem' }}
        onClick={handleReady}
        disabled={readyLoading || readySent || !connected || players.length < 1}
      >
        {readySent ? 'Waiting for opponent...' : 'Ready'}
      </button>
    </div>
  )
}

function PlayerSlot({ player, label, isReady, isYou, waiting }) {
  return (
    <div
      className="card"
      style={{
        flex: 1,
        maxWidth: 280,
        padding: '2rem',
        textAlign: 'center',
        borderColor: isYou ? 'var(--accent)' : undefined,
      }}
    >
      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.75rem' }}>
        {label}{isYou ? ' (You)' : ''}
      </div>
      {waiting ? (
        <>
          <div style={{ fontSize: '1.25rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            Waiting...
          </div>
          <div style={{ marginTop: '0.75rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Share room code
          </div>
        </>
      ) : (
        <>
          <div style={{ fontSize: '1.35rem', fontWeight: 700 }}>{player.username}</div>
          <div
            style={{
              marginTop: '0.75rem',
              fontSize: '0.85rem',
              fontWeight: 600,
              color: isReady ? 'var(--success)' : 'var(--amber)',
            }}
          >
            {isReady ? '● Ready' : '○ Waiting'}
          </div>
        </>
      )}
    </div>
  )
}
