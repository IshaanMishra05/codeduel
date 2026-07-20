import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { useAuth } from './AuthContext'

const WS_URL = 'http://localhost:8080/ws'

const WebSocketContext = createContext(null)

export function WebSocketProvider({ children }) {
  const { token } = useAuth()
  const clientRef = useRef(null)
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    if (!token) {
      setConnected(false)
      if (clientRef.current?.connected) {
        clientRef.current.disconnect()
      }
      clientRef.current = null
      return
    }

    const socket = new SockJS(WS_URL)
    const client = Stomp.over(socket)
    client.debug = () => {}

    client.connect(
      { Authorization: `Bearer ${token}` },
      () => {
        clientRef.current = client
        setConnected(true)
      },
      () => {
        setConnected(false)
        clientRef.current = null
      }
    )

    return () => {
      setConnected(false)
      if (client.connected) {
        client.disconnect()
      }
      clientRef.current = null
    }
  }, [token])

  const subscribe = useCallback(
    (matchId, handler) => {
      if (!clientRef.current?.connected) return () => {}

      const subscription = clientRef.current.subscribe(
        `/topic/match/${matchId}`,
        (message) => {
          try {
            handler(JSON.parse(message.body))
          } catch {
            /* ignore malformed messages */
          }
        }
      )

      return () => subscription.unsubscribe()
    },
    [connected]
  )

  const sendReady = useCallback((matchId, userId) => {
    if (!clientRef.current?.connected) return
    clientRef.current.send(
      `/app/match/${matchId}/ready`,
      {},
      JSON.stringify({ type: 'PLAYER_READY', matchId, userId })
    )
  }, [])

  const sendSubmit = useCallback((matchId, userId, code, language) => {
    if (!clientRef.current?.connected) return
    clientRef.current.send(
      `/app/match/${matchId}/submit`,
      {},
      JSON.stringify({ userId, matchId, code, language })
    )
  }, [])

  return (
    <WebSocketContext.Provider
      value={{ connected, subscribe, sendReady, sendSubmit }}
    >
      {children}
    </WebSocketContext.Provider>
  )
}

export function useWebSocket() {
  const ctx = useContext(WebSocketContext)
  if (!ctx) throw new Error('useWebSocket must be used within WebSocketProvider')
  return ctx
}
