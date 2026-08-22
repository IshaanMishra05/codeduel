export interface User {
  id: string
  username: string
  email: string
  eloRating: number
  matchesPlayed: number
  role: 'PLAYER' | 'ADMIN'
}

export interface AuthResponse {
  token: string
  user: User
}

export interface Problem {
  id: string | number
  title: string
  description: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD' | string
  timeLimit?: number
  language?: string
  createdBy?: string
}

export interface Room {
  code: string
  problemId: string
  problem: Problem
  createdBy: string
  status: 'WAITING' | 'IN_PROGRESS' | 'FINISHED'
  player1: RoomPlayer | null
  player2: RoomPlayer | null
  timeLimitSeconds: number
}

export interface RoomPlayer {
  userId: string
  username: string
  ready: boolean
}

export interface Match {
  id: string
  problem: Problem
  roomCode: string
  player1: {
    userId: string
    username: string
    eloRating: number
  }
  player2: {
    userId: string
    username: string
    eloRating: number
  }
  status: 'WAITING' | 'IN_PROGRESS' | 'FINISHED'
  startTime: string
  endTime?: string
  winner?: {
    username: string
    winnerEloDelta: number
    loserEloDelta: number
  }
}

export interface Submission {
  id: string
  code: string
  language: 'java' | 'python' | 'cpp'
  status: 'PENDING' | 'RUNNING' | 'PASSED' | 'FAILED'
  message?: string
  passedTestCases?: number
  totalTestCases?: number
}

export interface WebSocketMessage {
  type: 'PLAYER_JOINED' | 'PLAYER_READY' | 'MATCH_STARTED' | 'MATCH_FINISHED' | 'SUBMISSION_RESULT'
  payload: any
}
