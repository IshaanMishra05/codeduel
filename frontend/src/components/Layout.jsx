import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { currentUser, logout } = useAuth()

  return (
    <>
      <nav className="nav">
        <NavLink to="/problems" className="nav-logo">
          CodeDuel
        </NavLink>
        <div className="nav-links">
          <NavLink
            to="/problems"
            className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
          >
            Problems
          </NavLink>
          <NavLink
            to="/rooms"
            className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
          >
            Rooms
          </NavLink>
          <NavLink
            to="/leaderboard"
            className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
          >
            Leaderboard
          </NavLink>
        </div>
        <div className="nav-user">
          <span className="nav-username">{currentUser?.username}</span>
          <button type="button" className="btn btn-ghost" onClick={logout}>
            Logout
          </button>
        </div>
      </nav>
      <Outlet />
    </>
  )
}
