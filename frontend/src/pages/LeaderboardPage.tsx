import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { usersAPI } from '../services/api'
import toast from 'react-hot-toast'
import { ArrowLeft, Trophy } from 'lucide-react'

interface LeaderboardEntry {
  rank: number
  username: string
  eloRating: number
  matchesPlayed: number
}

export function LeaderboardPage() {
  const navigate = useNavigate()
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadLeaderboard()
  }, [])

  const loadLeaderboard = async () => {
    try {
      const res = await usersAPI.getLeaderboard()
      const ranked = res.data.map((user: any, index: number) => ({
        rank: index + 1,
        username: user.username,
        eloRating: user.eloRating,
        matchesPlayed: user.matchesPlayed,
      }))
      setLeaderboard(ranked)
    } catch (error) {
      toast.error('Failed to load leaderboard')
    } finally {
      setLoading(false)
    }
  }

  const getMedalIcon = (rank: number) => {
    if (rank === 1) return '🥇'
    if (rank === 2) return '🥈'
    if (rank === 3) return '🥉'
    return rank.toString()
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 via-purple-900 to-black">
      {/* Header */}
      <div className="bg-slate-900 border-b border-slate-700 px-6 py-4">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => navigate('/')}
            className="flex items-center space-x-2 px-4 py-2 bg-slate-700 hover:bg-slate-600 text-white rounded-lg transition"
          >
            <ArrowLeft size={20} />
            <span>Back</span>
          </button>
          <Trophy className="text-yellow-400" size={32} />
          <h1 className="text-3xl font-bold text-white">Leaderboard</h1>
        </div>
      </div>

      <div className="p-8 max-w-4xl mx-auto">
        {loading ? (
          <div className="text-center py-12">
            <p className="text-gray-400">Loading leaderboard...</p>
          </div>
        ) : leaderboard.length === 0 ? (
          <div className="bg-slate-800 rounded-lg p-8 text-center border border-slate-700">
            <p className="text-gray-400">No players yet</p>
          </div>
        ) : (
          <div className="bg-slate-800 rounded-lg border border-slate-700 overflow-hidden">
            <table className="w-full">
              <thead className="bg-slate-900 border-b border-slate-700">
                <tr>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">
                    Rank
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">
                    Player
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">
                    ELO Rating
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">
                    Matches Played
                  </th>
                </tr>
              </thead>
              <tbody>
                {leaderboard.map((entry, index) => (
                  <tr
                    key={index}
                    className={`border-t border-slate-700 hover:bg-slate-700 transition ${
                      index % 2 === 0 ? 'bg-slate-800' : 'bg-slate-750'
                    }`}
                  >
                    <td className="px-6 py-4">
                      <span className="text-2xl font-bold">
                        {getMedalIcon(entry.rank)}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <p className="font-semibold text-white">{entry.username}</p>
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-3 py-1 bg-yellow-900 text-yellow-200 rounded-full font-bold text-sm">
                        {entry.eloRating}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <p className="text-gray-300">{entry.matchesPlayed}</p>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Info Box */}
        <div className="mt-8 bg-slate-800 rounded-lg p-6 border border-slate-700">
          <h2 className="text-lg font-bold text-white mb-3">How ELO Works</h2>
          <ul className="text-gray-300 space-y-2 text-sm">
            <li>
              ✓ <span className="text-blue-400">New players</span> start with 1000 ELO
            </li>
            <li>
              ✓ <span className="text-blue-400">K-factor 40</span> for players with &lt;10 matches
            </li>
            <li>
              ✓ <span className="text-blue-400">K-factor 20</span> for established players
            </li>
            <li>
              ✓ <span className="text-blue-400">Minimum ELO</span> is 100 (no negative rating)
            </li>
          </ul>
        </div>
      </div>
    </div>
  )
}
