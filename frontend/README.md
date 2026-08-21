# CodeDuel Frontend

A real-time competitive 1v1 coding platform built with React, TypeScript, and Vite.

## Stack

- **Framework:** React 18 + TypeScript
- **Build Tool:** Vite
- **Routing:** React Router v6
- **State Management:** Zustand
- **Code Editor:** Monaco Editor
- **HTTP Client:** Axios
- **Real-time:** WebSocket with STOMP protocol
- **Styling:** Tailwind CSS
- **Notifications:** React Hot Toast
- **Icons:** Lucide React

## Features

✨ **User Authentication**
- Register and login with JWT tokens
- Persistent session management
- Protected routes

🎮 **Game Modes**
- Create competitive coding rooms
- Join existing rooms with a 6-letter code
- Real-time player status updates
- Live code execution feedback

💻 **Code Editor**
- Monaco Editor with syntax highlighting
- Support for Java, Python, and C++
- Real-time code submission
- Execution results and test case feedback

⭐ **Leaderboard**
- Global ELO-based ranking system
- View top players
- Track personal statistics

🔄 **Real-time Updates**
- WebSocket-based communication
- Live match events (player joined, ready, started, finished)
- Instant submission results
- Opponent progress tracking

## Prerequisites

- Node.js 16+ and npm
- A running CodeDuel backend API (http://localhost:8080)
- WebSocket support on backend

## Setup

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Configure environment variables:**
   ```bash
   cp .env.example .env.local
   ```

   Edit `.env.local`:
   ```
   VITE_API_URL=http://localhost:8080/api
   VITE_WS_URL=ws://localhost:8080/ws/websocket
   ```

   For production deployments, adjust these URLs accordingly.

3. **Start development server:**
   ```bash
   npm run dev
   ```

   The app will be available at `http://localhost:5173`

## Build

```bash
npm run build
```

Output goes to the `dist/` folder.

## Project Structure

```
src/
├── components/         # Reusable components
│   └── ProtectedRoute.tsx
├── pages/              # Page components
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   ├── HomePage.tsx
│   ├── MatchPage.tsx
│   └── LeaderboardPage.tsx
├── services/           # API and WebSocket clients
│   ├── api.ts          # REST API calls
│   └── websocket.ts    # WebSocket STOMP client
├── store/              # Zustand state management
│   └── index.ts        # Auth and Game stores
├── types/              # TypeScript interfaces
│   └── index.ts
├── App.tsx             # Main app with routing
├── main.tsx            # Entry point
└── index.css           # Global styles
```

## Key Components

### Login & Register Pages
- User authentication with JWT tokens
- Form validation
- Error handling with toast notifications

### Home Page
- Problem selection
- Room creation
- Browse open rooms
- Join with room code
- Quick access to leaderboard

### Match Page
- Code editor with language selection
- Problem description panel
- Real-time match status
- Live submission tracking
- Opponent progress visualization
- ELO delta calculation

### Leaderboard Page
- Top 50 players ranked by ELO
- Match statistics
- ELO system explanation

## WebSocket Events

The frontend subscribes to these topics:

- `/topic/match/{roomCode}` - Match events
  - `PLAYER_JOINED`
  - `PLAYER_READY`
  - `MATCH_STARTED`
  - `MATCH_FINISHED`

- `/topic/match/{roomCode}/progress` - Submission updates
  - Real-time test case results

## API Integration

The frontend communicates with these endpoints:

**Auth:**
- `POST /api/auth/register`
- `POST /api/auth/login`

**Problems:**
- `GET /api/problems`
- `GET /api/problems/{id}`

**Rooms:**
- `GET /api/rooms`
- `POST /api/rooms`
- `POST /api/rooms/join`
- `POST /api/rooms/{code}/ready`

**Submissions:**
- `POST /api/submissions`
- `GET /api/submissions/match/{matchId}`

**Users:**
- `GET /api/users/me`
- `GET /api/leaderboard`

## Environment Configuration

### Development
```
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws/websocket
```

### Production (Example with deployed backend)
```
VITE_API_URL=https://api.codeduel.com/api
VITE_WS_URL=wss://api.codeduel.com/ws/websocket
```

## Performance Optimizations

- Code splitting with React lazy loading
- Lazy-loaded Monaco Editor
- Optimized WebSocket reconnection
- State management with Zustand (minimal re-renders)
- Production build minification with Vite

## Browser Support

- Chrome/Chromium (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Troubleshooting

**"Failed to connect to WebSocket"**
- Ensure the backend is running
- Check `VITE_WS_URL` environment variable
- Verify WebSocket endpoint is accessible

**"Login failed"**
- Verify backend is running
- Check `VITE_API_URL` environment variable
- Ensure user credentials are correct

**"Code submission errors"**
- Ensure your backend has `javac`, `python3`, and `g++` installed
- Verify time limit is sufficient for code execution

## Development Tips

- Use `npm run dev` for hot module reloading
- Browser DevTools work seamlessly with React
- State can be inspected with Zustand DevTools
- Environment variables can be changed in `.env.local` without rebuilding

## Deployment

### Docker
```bash
npm run build
docker build -t codeduel-frontend .
docker run -p 80:80 codeduel-frontend
```

### Netlify / Vercel
```bash
npm run build
# Deploy the dist/ folder
```

### Static Hosting
```bash
npm run build
# Upload dist/ to any static hosting service
```

## License

MIT

## Support

For issues or questions, refer to the main CodeDuel repository.
