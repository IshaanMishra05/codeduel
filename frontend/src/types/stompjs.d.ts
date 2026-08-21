declare module 'stompjs' {
  export class Client {
    constructor(config: any)
    brokerURL?: string
    connectHeaders?: Record<string, string>
    debug?: (str: string) => void
    reconnectDelay?: number
    heartbeatIncoming?: number
    heartbeatOutgoing?: number
    onConnect?: () => void
    onStompError?: (frame: any) => void
    activate(): void
    deactivate(): void
    subscribe(topic: string, callback: (message: any) => void): any
    publish(options: { destination: string; body: string }): void
    active: boolean
  }
}
