# CodeDuel ⚔️

A real-time **1v1 competitive coding platform** built with Spring Boot. Two players join a room, get the same problem, write code, and the first one to pass all test cases wins. ELO rating updates after every match.

---

## What it does

- Players register, log in, and get a JWT token
- One player creates a room (gets a 6-letter code), shares it with an opponent
- Both players ready up — match starts automatically
- Each player writes code in their preferred language (Java / Python / C++)
- Code is executed server-side against hidden test cases with a time limit
- First to pass all tests wins — ELO ratings update instantly
- Results broadcast to both players over WebSocket in real time

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Database | PostgreSQL + JPA/Hibernate |
| Auth | Spring Security + JWT (JJWT 0.12) |
| Real-time | WebSockets with STOMP protocol |
| Code Execution | Java ProcessBuilder (sandboxed subprocess) |
| Testing | JUnit 5 + Mockito + AssertJ |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/codeduel/
├── config/          # Security, WebSocket, ModelMapper config
├── controller/      # REST endpoints (Auth, Problems, Rooms, Submissions, Users)
├── dto/             # Request and response shapes (separate from entities)
├── entity/          # JPA entities — User, Problem, TestCase, Match, MatchPlayer, Submission
├── exception/       # GlobalExceptionHandler + custom exceptions
├── repository/      # Spring Data JPA interfaces
├── security/        # JwtService + JwtAuthFilter
├── service/         # Business logic — Auth, Match, Submission, ELO, CodeExecution
└── websocket/       # STOMP controller + event publisher
```

---

## How to Run

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 14+
- `javac`, `python3`, `g++` on PATH (for code execution)

### 1. Create the database
```sql
CREATE DATABASE codeduel;
```

### 2. Configure `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/codeduel
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

app.jwt.secret=YOUR_64_CHAR_HEX_SECRET
```

Generate a secret:
```bash
# Mac/Linux
openssl rand -hex 32

# Windows PowerShell
-join ((1..32) | ForEach-Object { '{0:x2}' -f (Get-Random -Max 256) })
```

### 3. Run
```bash
mvn spring-boot:run
```

API is live at `http://localhost:8080`. Hibernate auto-creates all tables on first run.

### 4. Run Tests
```bash
mvn test
```

---

## API Endpoints

### Auth — no token required
| Method | Endpoint | Body |
|---|---|---|
| POST | `/api/auth/register` | `{ username, email, password }` |
| POST | `/api/auth/login` | `{ username, password }` → returns `{ token, ... }` |

### Problems
| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/problems` | ✅ Player |
| GET | `/api/problems/{id}` | ✅ Player |
| POST | `/api/problems` | 🔒 Admin only |
| PUT | `/api/problems/{id}` | 🔒 Admin only |
| DELETE | `/api/problems/{id}` | 🔒 Admin only |

### Rooms
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/rooms` | List open rooms |
| POST | `/api/rooms` | Create room `{ problemId, timeLimitSeconds }` |
| POST | `/api/rooms/join` | Join room `{ roomCode }` |
| POST | `/api/rooms/{code}/ready` | Mark yourself ready |

### Submissions
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/submissions` | Submit code `{ matchId, code, language }` |
| GET | `/api/submissions/match/{matchId}` | Your submissions in a match |

### Users
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/me` | Your profile |
| GET | `/api/leaderboard` | Top players by ELO |

---

## WebSocket Events

Connect to `ws://localhost:8080/ws/websocket` using STOMP.

**Subscribe to:**
- `/topic/match/{roomCode}` — room events (PLAYER_JOINED, PLAYER_READY, MATCH_STARTED, MATCH_FINISHED)
- `/topic/match/{roomCode}/progress` — live submission results for both players

**Send to:**
- `/app/match/{roomCode}/ready` — mark yourself ready

**Example MATCH_FINISHED payload:**
```json
{
  "type": "MATCH_FINISHED",
  "winnerUsername": "alice",
  "winnerEloDelta": 16,
  "loserEloDelta": -16
}
```

---

## Code Execution Engine

Submissions run as native OS subprocesses via `ProcessBuilder`:

| Language | Compile | Run |
|---|---|---|
| `java` | `javac Solution.java` | `java -Xmx128m Solution` |
| `python` | — | `python3 Solution.py` |
| `cpp` | `g++ -O2 -o solution solution.cpp` | `./solution` |

- **Timeout:** 5000ms (configurable via `app.execution.timeout-ms`)
- **Memory limit:** 128MB JVM heap for Java
- **Output:** stdout compared against expected output per test case, stderr captured for error messages

---

## ELO Rating System

Standard chess Elo formula with a dynamic K-factor:

```
Expected = 1 / (1 + 10^((opponentRating - playerRating) / 400))
NewRating = OldRating + K × (1 - Expected)   [winner]
NewRating = OldRating + K × (0 - Expected)   [loser]
```

| Matches Played | K Factor |
|---|---|
| Under 10 | 40 (new player, ratings move faster) |
| 10 or more | 20 (established player) |

Minimum rating floor: **100**

---

## To Create Your First Problem (Admin Setup)

1. Register an account via `POST /api/auth/register`
2. Manually set your role in the database:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
   ```
3. Log in again to get a new token with admin privileges
4. Use `POST /api/problems` with test cases to create problems

---

## Key Design Decisions

**Why WebSockets over polling?** Match state (opponent joined, submission result, match finished) needs to appear instantly on both clients simultaneously. Polling would add 1-3s latency and unnecessary server load.

**Why ProcessBuilder over a Judge0 API?** Keeps the execution engine self-contained with no external API dependency or rate limits. The tradeoff is that `javac`, `python3`, and `g++` must be available on the host.

**Why JWT with stateless sessions?** WebSocket connections and REST calls share the same auth mechanism. Stateless JWTs mean no session store is needed, simplifying horizontal scaling.

**Why separate DTOs from entities?** Prevents leaking internal fields (like `password`) in API responses, and decouples the database schema from the API contract.
