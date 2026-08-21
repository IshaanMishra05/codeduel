import axios from 'axios'
import type { User, AuthResponse, Problem, Room, Submission } from '../types'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const client = axios.create({
  baseURL: API_BASE_URL,
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const authAPI = {
  register: (username: string, email: string, password: string) =>
    client.post<AuthResponse>('/auth/register', { username, email, password }),
  login: (username: string, password: string) =>
    client.post<AuthResponse>('/auth/login', { username, password }),
  getProfile: () => client.get<User>('/users/me'),
}

export const problemsAPI = {
  getAll: () => client.get<Problem[]>('/problems'),
  getById: (id: string) => client.get<Problem>(`/problems/${id}`),
  create: (title: string, description: string, difficulty: string, testCases: any) =>
    client.post('/problems', { title, description, difficulty, testCases }),
}

export const roomsAPI = {
  getOpen: () => client.get<Room[]>('/rooms'),
  create: (problemId: string, timeLimitSeconds: number) =>
    client.post<Room>('/rooms', { problemId, timeLimitSeconds }),
  join: (roomCode: string) =>
    client.post<Room>('/rooms/join', { roomCode }),
  markReady: (roomCode: string) =>
    client.post(`/rooms/${roomCode}/ready`, {}),
}

export const submissionsAPI = {
  submit: (matchId: string, code: string, language: string) =>
    client.post<Submission>('/submissions', { matchId, code, language }),
  getByMatch: (matchId: string) =>
    client.get<Submission[]>(`/submissions/match/${matchId}`),
}

export const usersAPI = {
  getLeaderboard: () => client.get<any[]>('/leaderboard'),
}

export default client
