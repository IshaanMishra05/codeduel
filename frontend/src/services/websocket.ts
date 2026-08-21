import * as StompJs from 'stompjs'

class WebSocketService {
  private client: StompJs.Client | null = null
  private listeners: Map<string, Set<(msg: any) => void>> = new Map()

  connect(token: string, onConnect?: () => void, onError?: (err: any) => void) {
    const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws/websocket'

    this.client = new StompJs.Client({
      brokerURL: wsUrl,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str: string) => {
        console.log('WS:', str)
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    })

    this.client.onConnect = () => {
      console.log('WebSocket connected')
      onConnect?.()
    }

    this.client.onStompError = (frame: any) => {
      console.error('WebSocket error:', frame)
      onError?.(frame)
    }

    this.client.activate()
  }

  disconnect() {
    if (this.client?.active) {
      this.client.deactivate()
    }
  }

  subscribe(topic: string, callback: (msg: any) => void) {
    if (!this.client?.active) {
      console.error('WebSocket not connected')
      return
    }

    this.client.subscribe(topic, (message: any) => {
      try {
        const data = JSON.parse(message.body)
        callback(data)
        // Notify all listeners for this topic
        this.listeners.get(topic)?.forEach((listener) => listener(data))
      } catch (e) {
        console.error('Error parsing message:', e)
      }
    })
  }

  addListener(topic: string, callback: (msg: any) => void) {
    if (!this.listeners.has(topic)) {
      this.listeners.set(topic, new Set())
    }
    this.listeners.get(topic)!.add(callback)
  }

  removeListener(topic: string, callback: (msg: any) => void) {
    this.listeners.get(topic)?.delete(callback)
  }

  send(destination: string, body: any) {
    if (!this.client?.active) {
      console.error('WebSocket not connected')
      return
    }
    this.client.publish({
      destination,
      body: JSON.stringify(body),
    })
  }

  isConnected(): boolean {
    return this.client?.active ?? false
  }
}

export const wsService = new WebSocketService()
