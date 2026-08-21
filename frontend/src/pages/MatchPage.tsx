import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuthStore, useGameStore } from '../store'
import { wsService } from '../services/websocket'
import { submissionsAPI } from '../services/api'
import Editor from '@monaco-editor/react'
import toast from 'react-hot-toast'
import { Play, Copy, LogOut } from 'lucide-react'

const languageDefaults: Record<string, string> = {
  java: `public class Solution {
    public static void main(String[] args) {
        // Write your solution here
    }
}`,
  python: `# Write your solution here
`,
  cpp: `#include <bits/stdc++.h>
using namespace std;

int main() {
    // Write your solution here
    return 0;
}`,
}

export function MatchPage() {
  const { code: roomCode } = useParams<{ code: string }>()
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const {
    currentMatch,
    currentProblem,
    submissions,
    opponentSubmission,
    addSubmission,
    setOpponentSubmission,
  } = useGameStore()

  const [language, setLanguage] = useState<'java' | 'python' | 'cpp'>('python')
  const [code, setCode] = useState(languageDefaults.python)
  const [submitting, setSubmitting] = useState(false)
  const [matchStatus, setMatchStatus] = useState('WAITING')
  const [opponentReady, setOpponentReady] = useState(false)
  const [userReady, setUserReady] = useState(false)
  const [matchResult, setMatchResult] = useState<any>(null)

  useEffect(() => {
    if (!roomCode || !wsService.isConnected()) {
      wsService.connect(useAuthStore.getState().token!)
    }

    const topicMatch = `/topic/match/${roomCode}`
    const topicProgress = `/topic/match/${roomCode}/progress`

    wsService.subscribe(topicMatch, (message) => {
      handleMatchMessage(message)
    })

    wsService.subscribe(topicProgress, (message) => {
      if (message.username !== user?.username) {
        setOpponentSubmission(message)
      }
    })

    return () => {
      // Keep connection alive
    }
  }, [roomCode])

  const handleMatchMessage = (message: any) => {
    switch (message.type) {
      case 'PLAYER_JOINED':
        toast.success(`${message.username} joined the match`)
        break
      case 'PLAYER_READY':
        if (message.username !== user?.username) {
          setOpponentReady(true)
        }
        break
      case 'MATCH_STARTED':
        setMatchStatus('IN_PROGRESS')
        toast.success('Match started! Code submission is enabled.')
        break
      case 'MATCH_FINISHED':
        setMatchStatus('FINISHED')
        setMatchResult(message)
        toast.success(
          message.winnerUsername === user?.username
            ? '🎉 You won!'
            : 'Match finished!'
        )
        break
    }
  }

  const handleReady = async () => {
    if (!roomCode) return
    try {
      wsService.send(`/app/match/${roomCode}/ready`, {})
      setUserReady(true)
      toast.success('You are ready!')
    } catch (error) {
      toast.error('Failed to mark ready')
    }
  }

  const handleSubmit = async () => {
    if (!currentMatch || submitting) return

    setSubmitting(true)
    try {
      const response = await submissionsAPI.submit(
        currentMatch.id,
        code,
        language
      )
      addSubmission(response.data)
      toast.success('Code submitted!')
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Submission failed')
    } finally {
      setSubmitting(false)
    }
  }

  const handleCopyCode = () => {
    if (!roomCode) return
    navigator.clipboard.writeText(roomCode)
    toast.success('Room code copied to clipboard!')
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 via-purple-900 to-black">
      {/* Header */}
      <div className="bg-slate-900 border-b border-slate-700 px-6 py-3 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-white">
            {currentProblem?.title || 'Match'}
          </h1>
          <p className="text-sm text-gray-400">Room: {roomCode}</p>
        </div>

        <div className="flex items-center space-x-4">
          <button
            onClick={handleCopyCode}
            className="flex items-center space-x-1 px-3 py-1 bg-slate-700 hover:bg-slate-600 text-gray-300 rounded transition text-sm"
          >
            <Copy size={16} />
            <span>Copy Code</span>
          </button>
          <button
            onClick={() => {
              navigate('/')
              wsService.disconnect()
            }}
            className="flex items-center space-x-1 px-3 py-1 bg-red-600 hover:bg-red-700 text-white rounded transition text-sm"
          >
            <LogOut size={16} />
            <span>Leave</span>
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 p-4 h-[calc(100vh-70px)]">
        {/* Problem Description */}
        <div className="lg:col-span-1 bg-slate-800 rounded-lg p-4 border border-slate-700 overflow-y-auto">
          <h2 className="text-lg font-bold text-white mb-4">Problem</h2>
          <div className="text-gray-300 space-y-3 text-sm">
            <div>
              <h3 className="font-semibold text-blue-400">Description</h3>
              <p>{currentProblem?.description}</p>
            </div>
            <div>
              <h3 className="font-semibold text-blue-400">Difficulty</h3>
              <p>{currentProblem?.difficulty}</p>
            </div>
            <div>
              <h3 className="font-semibold text-blue-400">Time Limit</h3>
              <p>{currentProblem?.timeLimit}ms</p>
            </div>
          </div>

          {/* Match Status */}
          <div className="mt-6 pt-4 border-t border-slate-700">
            <h3 className="font-semibold text-white mb-3">Match Status</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-400">Your Status:</span>
                <span
                  className={
                    userReady ? 'text-green-400 font-semibold' : 'text-yellow-400'
                  }
                >
                  {userReady ? '✓ Ready' : 'Waiting...'}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Opponent:</span>
                <span
                  className={
                    opponentReady ? 'text-green-400 font-semibold' : 'text-yellow-400'
                  }
                >
                  {opponentReady ? '✓ Ready' : 'Waiting...'}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Match:</span>
                <span className="text-blue-400 font-semibold">{matchStatus}</span>
              </div>
            </div>

            {matchStatus === 'WAITING' && (
              <button
                onClick={handleReady}
                className="w-full mt-4 bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-lg transition"
              >
                Mark Ready
              </button>
            )}

            {matchStatus === 'FINISHED' && matchResult && (
              <div className="mt-4 p-3 bg-slate-700 rounded-lg border border-slate-600">
                <p className="text-yellow-400 font-semibold">
                  {matchResult.winnerUsername === user?.username
                    ? '🎉 You Won!'
                    : '😔 You Lost'}
                </p>
                <p className="text-sm text-gray-300 mt-2">
                  ELO: {matchResult.winnerEloDelta > 0 ? '+' : ''}{matchResult.winnerEloDelta}
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Code Editor */}
        <div className="lg:col-span-2 flex flex-col bg-slate-800 rounded-lg border border-slate-700 overflow-hidden">
          <div className="bg-slate-900 border-b border-slate-700 px-4 py-3 flex justify-between items-center">
            <select
              value={language}
              onChange={(e) => {
                setLanguage(e.target.value as any)
                setCode(languageDefaults[e.target.value])
              }}
              className="px-3 py-1 bg-slate-700 border border-slate-600 rounded text-white text-sm focus:outline-none"
            >
              <option value="python">Python 3</option>
              <option value="java">Java</option>
              <option value="cpp">C++</option>
            </select>

            <button
              onClick={handleSubmit}
              disabled={submitting || matchStatus !== 'IN_PROGRESS'}
              className="flex items-center space-x-2 px-4 py-1 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold rounded-lg transition text-sm"
            >
              <Play size={16} />
              <span>{submitting ? 'Submitting...' : 'Submit'}</span>
            </button>
          </div>

          <div className="flex-1 overflow-hidden">
            <Editor
              height="100%"
              language={language}
              value={code}
              onChange={(value) => setCode(value || '')}
              theme="vs-dark"
              options={{
                minimap: { enabled: false },
                fontSize: 14,
                fontFamily: 'Fira Code, monospace',
              }}
            />
          </div>
        </div>
      </div>

      {/* Submissions Panel */}
      {(submissions.length > 0 || opponentSubmission) && (
        <div className="bg-slate-900 border-t border-slate-700 p-4 max-h-32 overflow-y-auto">
          <h3 className="text-sm font-bold text-white mb-2">Recent Submissions</h3>
          <div className="flex gap-2 flex-wrap">
            {submissions.map((sub, i) => (
              <div
                key={i}
                className={`px-3 py-1 rounded text-xs font-semibold ${
                  sub.status === 'PASSED'
                    ? 'bg-green-900 text-green-200'
                    : sub.status === 'FAILED'
                      ? 'bg-red-900 text-red-200'
                      : 'bg-yellow-900 text-yellow-200'
                }`}
              >
                You: {sub.status}
              </div>
            ))}
            {opponentSubmission && (
              <div
                className={`px-3 py-1 rounded text-xs font-semibold ${
                  opponentSubmission.status === 'PASSED'
                    ? 'bg-green-900 text-green-200'
                    : opponentSubmission.status === 'FAILED'
                      ? 'bg-red-900 text-red-200'
                      : 'bg-yellow-900 text-yellow-200'
                }`}
              >
                Opponent: {opponentSubmission.status}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
