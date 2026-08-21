import { create } from 'zustand'
import type { User, Problem, Room, Match, Submission } from '../types'

interface AuthStore {
  user: User | null
  token: string | null
  isLoading: boolean
  setUser: (user: User | null) => void
  setToken: (token: string | null) => void
  setIsLoading: (loading: boolean) => void
  logout: () => void
}

export const useAuthStore = create<AuthStore>((set) => ({
  user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null,
  token: localStorage.getItem('token'),
  isLoading: false,
  setUser: (user) => {
    set({ user })
    if (user) localStorage.setItem('user', JSON.stringify(user))
    else localStorage.removeItem('user')
  },
  setToken: (token) => {
    set({ token })
    if (token) localStorage.setItem('token', token)
    else localStorage.removeItem('token')
  },
  setIsLoading: (loading) => set({ isLoading: loading }),
  logout: () => {
    set({ user: null, token: null })
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  },
}))

interface GameStore {
  currentRoom: Room | null
  currentMatch: Match | null
  currentProblem: Problem | null
  submissions: Submission[]
  opponentSubmission: Submission | null
  setCurrentRoom: (room: Room | null) => void
  setCurrentMatch: (match: Match | null) => void
  setCurrentProblem: (problem: Problem | null) => void
  setSubmissions: (submissions: Submission[]) => void
  addSubmission: (submission: Submission) => void
  setOpponentSubmission: (submission: Submission | null) => void
}

export const useGameStore = create<GameStore>((set) => ({
  currentRoom: null,
  currentMatch: null,
  currentProblem: null,
  submissions: [],
  opponentSubmission: null,
  setCurrentRoom: (room) => set({ currentRoom: room }),
  setCurrentMatch: (match) => set({ currentMatch: match }),
  setCurrentProblem: (problem) => set({ currentProblem: problem }),
  setSubmissions: (submissions) => set({ submissions }),
  addSubmission: (submission) =>
    set((state) => ({ submissions: [...state.submissions, submission] })),
  setOpponentSubmission: (submission) => set({ opponentSubmission: submission }),
}))
