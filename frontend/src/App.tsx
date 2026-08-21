import { useEffect } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store'
import { wsService } from './services/websocket'
import { ProtectedRoute } from './components/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { HomePage } from './pages/HomePage'
import { MatchPage } from './pages/MatchPage'
import { LeaderboardPage } from './pages/LeaderboardPage'
import './App.css'

function App() {
  const { token } = useAuthStore()

  useEffect(() => {
    if (token && !wsService.isConnected()) {
      wsService.connect(token)
    }
  }, [token])

  return (
    <Routes>
      <Route
        path="/login"
        element={token ? <Navigate to="/" /> : <LoginPage />}
      />
      <Route
        path="/register"
        element={token ? <Navigate to="/" /> : <RegisterPage />}
      />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <HomePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/match/:code"
        element={
          <ProtectedRoute>
            <MatchPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/leaderboard"
        element={
          <ProtectedRoute>
            <LeaderboardPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  )
}

export default App

