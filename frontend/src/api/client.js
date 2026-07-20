const BASE_URL = 'http://localhost:8080/api'

async function handleResponse(res) {
  if (res.status === 401 || res.status === 403) {
    localStorage.removeItem('token')
    if (window.location.pathname !== '/') {
      window.location.href = '/'
    }
    throw new Error('Session expired. Please sign in again.')
  }

  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || res.statusText || 'Request failed')
  }

  if (res.status === 204) return null
  const contentType = res.headers.get('content-type')
  if (contentType?.includes('application/json')) {
    return res.json()
  }
  return null
}

export async function apiRequest(path, options = {}, auth = true) {
  const headers = { 'Content-Type': 'application/json', ...options.headers }

  if (auth) {
    const token = localStorage.getItem('token')
    if (token) headers.Authorization = `Bearer ${token}`
  }

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers })
  return handleResponse(res)
}

export const api = {
  register: (body) =>
    apiRequest('/auth/register', { method: 'POST', body: JSON.stringify(body) }, false),

  login: (body) =>
    apiRequest('/auth/login', { method: 'POST', body: JSON.stringify(body) }, false),

  getUsers: () => apiRequest('/users'),

  getLeaderboard: () => apiRequest('/users/leaderboard'),

  getUser: (id) => apiRequest(`/users/${id}`),

  getProblems: () => apiRequest('/problems'),

  getProblem: (id) => apiRequest(`/problems/${id}`),

  createProblem: (body) =>
    apiRequest('/problems', { method: 'POST', body: JSON.stringify(body) }),

  deleteProblem: (id) =>
    apiRequest(`/problems/${id}`, { method: 'DELETE' }),

  getTestCases: (problemId) =>
    apiRequest(`/test-cases/problem/${problemId}`),

  createTestCase: (body) =>
    apiRequest('/test-cases', { method: 'POST', body: JSON.stringify(body) }),

  getMatches: () => apiRequest('/matches'),

  getMatch: (id) => apiRequest(`/matches/${id}`),

  getMatchByRoom: (roomCode) => apiRequest(`/matches/room/${roomCode}`),

  createMatch: (body) =>
    apiRequest('/matches', { method: 'POST', body: JSON.stringify(body) }),

  getMatchPlayers: (matchId) =>
    apiRequest(`/matchplayers/match/${matchId}`),

  joinMatch: (body) =>
    apiRequest('/matchplayers', { method: 'POST', body: JSON.stringify(body) }),
}
