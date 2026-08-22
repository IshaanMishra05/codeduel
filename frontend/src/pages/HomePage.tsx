import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore, useGameStore } from '../store'
import { roomsAPI, problemsAPI } from '../services/api'
import type { Problem, Room } from '../types'
import toast from 'react-hot-toast'
import { Plus, LogOut, Trophy } from 'lucide-react'

export function HomePage() {
  const navigate = useNavigate()
  const { user, logout } = useAuthStore()
  const { setCurrentProblem, setCurrentRoom } = useGameStore()
  const [problems, setProblems] = useState<Problem[]>([])
  const [rooms, setRooms] = useState<Room[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedProblem, setSelectedProblem] = useState<string>('')
  const [timeLimit, setTimeLimit] = useState(300)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [problemsRes, roomsRes] = await Promise.all([
        problemsAPI.getAll(),
        roomsAPI.getOpen(),
      ])
      setProblems(problemsRes.data)
      setRooms(roomsRes.data)
    } catch (error) {
      toast.error('Failed to load data')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateRoom = async () => {
    if (!selectedProblem) {
      toast.error('Please select a problem')
      return
    }

    try {
      const res = await roomsAPI.create(selectedProblem, timeLimit)
      setCurrentRoom(res.data)
      navigate(`/match/${res.data.code}`)
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to create room')
    }
  }

  const handleJoinRoom = async (room: Room) => {
    try {
      const res = await roomsAPI.join(room.code)
      setCurrentRoom(res.data)
      setCurrentProblem(res.data.problem)
      navigate(`/match/${res.data.code}`)
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to join room')
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 via-purple-900 to-black">
      {/* Header */}
      <div className="bg-slate-900 border-b border-slate-700 px-6 py-4 flex justify-between items-center">
        <div className="flex items-center space-x-2">
          <Trophy className="text-yellow-400" size={28} />
          <h1 className="text-2xl font-bold text-white">CodeDuel</h1>
        </div>
        <div className="flex items-center space-x-4">
          <div className="text-gray-300">
            <p className="font-semibold">{user?.username}</p>
            <p className="text-sm text-gray-400">ELO: {user?.eloRating}</p>
          </div>
          <button
            onClick={() => {
              logout()
              navigate('/login')
            }}
            className="flex items-center space-x-1 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition"
          >
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </div>

      <div className="p-8 max-w-7xl mx-auto">
        {/* Create Room Section */}
        <div className="bg-slate-800 rounded-lg p-6 mb-8 border border-slate-700">
          <h2 className="text-xl font-bold text-white mb-4 flex items-center space-x-2">
            <Plus size={22} />
            <span>Create New Room</span>
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Select Problem
              </label>
              <select
                value={selectedProblem}
                onChange={(e) => setSelectedProblem(e.target.value)}
                className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
              >
                <option value="">Choose a problem...</option>
                {problems.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title} ({p.difficulty})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Time Limit (seconds)
              </label>
              <input
                type="number"
                min="60"
                max="1800"
                value={timeLimit}
                onChange={(e) => setTimeLimit(Number(e.target.value))}
                className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
              />
            </div>

            <div className="flex items-end">
              <button
                onClick={handleCreateRoom}
                className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-lg transition"
              >
                Create Room
              </button>
            </div>
          </div>
        </div>

        {/* Open Rooms Section */}
        <div>
          <h2 className="text-xl font-bold text-white mb-4">Open Rooms</h2>

          {loading ? (
            <div className="text-center py-8">
              <p className="text-gray-400">Loading rooms...</p>
            </div>
          ) : rooms.length === 0 ? (
            <div className="bg-slate-800 rounded-lg p-8 text-center border border-slate-700">
              <p className="text-gray-400">No open rooms. Create one to get started!</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {rooms.map((room) => (
                <div
                  key={room.code}
                  className="bg-slate-800 rounded-lg p-6 border border-slate-700 hover:border-blue-500 transition"
                >
                  <div className="flex justify-between items-start mb-4">
                    <div>
                      <h3 className="text-lg font-semibold text-white">
                        {room.problem.title}
                      </h3>
                      <p className="text-sm text-blue-400">Code: {room.code}</p>
                    </div>
                    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                      room.problem.difficulty === 'EASY'
                        ? 'bg-green-900 text-green-200'
                        : room.problem.difficulty === 'MEDIUM'
                          ? 'bg-yellow-900 text-yellow-200'
                          : 'bg-red-900 text-red-200'
                    }`}>
                      {room.problem.difficulty}
                    </span>
                  </div>

                  <div className="space-y-2 mb-4 text-sm text-gray-300">
                    <p>Created by: <span className="text-blue-400">{room.createdBy}</span></p>
                    <p>Time Limit: <span className="text-blue-400">{room.timeLimitSeconds}s</span></p>
                    <p>Players: <span className="text-blue-400">1/2</span></p>
                  </div>

                  <button
                    onClick={() => handleJoinRoom(room)}
                    className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition"
                  >
                    Join Room
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Leaderboard Link */}
        <div className="mt-8 flex flex-col sm:flex-row gap-4 justify-center items-center">
          {user?.role === 'ADMIN' && (
            <button
              onClick={() => navigate('/admin')}
              className="px-6 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-lg transition"
            >
              Admin Panel
            </button>
          )}
          <button
            onClick={() => navigate('/leaderboard')}
            className="px-6 py-2 bg-purple-600 hover:bg-purple-700 text-white font-bold rounded-lg transition"
          >
            View Leaderboard
          </button>
        </div>
      </div>
    </div>
  )
}
