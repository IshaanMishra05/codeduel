import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { WebSocketProvider } from './context/WebSocketContext'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import AuthPage from './pages/AuthPage'
import ProblemsPage from './pages/ProblemsPage'
import ProblemDetailPage from './pages/ProblemDetailPage'
import RoomsPage from './pages/RoomsPage'
import RoomLobbyPage from './pages/RoomLobbyPage'
import MatchPage from './pages/MatchPage'
import LeaderboardPage from './pages/LeaderboardPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <WebSocketProvider>
          <Routes>
            <Route path="/" element={<AuthPage />} />
            <Route
              element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }
            >
              <Route path="/problems" element={<ProblemsPage />} />
              <Route path="/problems/:id" element={<ProblemDetailPage />} />
              <Route path="/rooms" element={<RoomsPage />} />
              <Route path="/rooms/:matchId" element={<RoomLobbyPage />} />
              <Route path="/leaderboard" element={<LeaderboardPage />} />
            </Route>
            <Route
              path="/match/:matchId"
              element={
                <ProtectedRoute>
                  <MatchPage />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </WebSocketProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
