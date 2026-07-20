import { useEffect, useState } from 'react'
import { api } from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function LeaderboardPage() {
  const { currentUser } = useAuth()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .getLeaderboard()
      .then(setUsers)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const winRate = (wins, losses) => {
    const total = wins + losses
    if (total === 0) return '0%'
    return `${Math.round((wins / total) * 100)}%`
  }

  if (loading) return <div className="loading">Loading leaderboard...</div>

  return (
    <div className="page">
      <h1 className="page-title">Leaderboard</h1>
      <p className="page-subtitle">Top coders ranked by ELO</p>

      {error && <p className="error-msg">{error}</p>}

      {!error && (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Rank</th>
                <th>Username</th>
                <th>ELO</th>
                <th>Wins</th>
                <th>Losses</th>
                <th>Win Rate</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user, index) => (
                <tr
                  key={user.id}
                  className={user.id === currentUser?.id ? 'highlight-row' : ''}
                >
                  <td style={{ color: 'var(--text-muted)' }}>{index + 1}</td>
                  <td style={{ fontWeight: 500 }}>{user.username}</td>
                  <td>{user.eloRating}</td>
                  <td style={{ color: 'var(--success)' }}>{user.wins}</td>
                  <td style={{ color: 'var(--error)' }}>{user.losses}</td>
                  <td>{winRate(user.wins, user.losses)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
