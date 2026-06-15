# CodeDuel ⚔️

> **Real-time 1v1 competitive coding platform** — two developers, one problem, one winner.

CodeDuel is a full-stack backend built with **Spring Boot 3**, **PostgreSQL**, **JWT authentication**, and **WebSockets (STOMP)**. Players are matched in live coding rooms, submit solutions that run against hidden test cases, and earn or lose Elo rating based on who solves the problem first.

---

## ✨ Features

| Feature | Details |
|---|---|
| **Auth** | Register / login with JWT; BCrypt password hashing; role-based access (PLAYER / ADMIN) |
| **Problems** | Admin CRUD for problems + test cases (visible & hidden); filter by difficulty and language |
| **Rooms** | 6-character random room codes; join flow; ready-up system; auto-start when both players ready |
| **Code Execution** | ProcessBuilder sandbox for Java, Python, C++; per-test-case stdin/stdout comparison; 5 s timeout; compile error capture |
| **Real-time** | STOMP WebSocket events — player joined, ready, submission result, match finished |
| **ELO Rating** | Standard chess Elo formula; dynamic K-factor (K=40 for new players, K=20 established); Elo floor at 100 |
| **Leaderboard** | Top N players sorted by Elo; win-rate calculation |

---

## 🏗 Architecture

```
Client (React / any)
   │
   ├── REST  ──────────►  Spring Boot Controllers
   │                           │
   └── WebSocket (STOMP) ─►  MatchWebSocketController
                               │
                        ┌──────▼──────────────────┐
                        │      Service Layer        │
                        │  AuthService             │
                        │  ProblemService          │
                        │  MatchService            │
                        │  SubmissionService       │  ◄── CodeExecutionService
                        │  UserService             │
                        │  EloService              │
                        └──────────────────────────┘
                               │
                        ┌──────▼──────────────────┐
                        │    JPA / Hibernate        │
                        │    PostgreSQL             │
                        └──────────────────────────┘
```

### WebSocket Event Flow

```
Client A                 Server                    Client B
   │                       │                          │
   │── POST /rooms ────────►│                          │
   │◄── { roomCode } ───────│                          │
   │                       │                          │
   │                       │◄─── POST /rooms/join ────│
   │◄── PLAYER_JOINED ──────│─── PLAYER_JOINED ───────►│
   │                       │                          │
   │── /app/match/{code}/ready ──►│                   │
   │◄─ PLAYER_READY ────────│──── PLAYER_READY ───────►│
   │◄─ MATCH_STARTED ───────│──── MATCH_STARTED ──────►│
   │                       │                          │
   │── POST /submissions ──►│  (code runs here)        │
   │◄─ SUBMISSION_RESULT ───│── SUBMISSION_RESULT ────►│
   │◄─ MATCH_FINISHED ──────│──── MATCH_FINISHED ─────►│
```

---

## 🗂 Project Structure

```
src/
├── main/java/com/codeduel/
│   ├── CodeDuelApplication.java
│   ├── config/
│   │   ├── AppConfig.java          # ModelMapper bean
│   │   ├── SecurityConfig.java     # Spring Security + CORS
│   │   └── WebSocketConfig.java    # STOMP broker config
│   ├── controller/
│   │   ├── AuthController.java     # POST /api/auth/register, /login
│   │   ├── ProblemController.java  # GET/POST/PUT/DELETE /api/problems
│   │   ├── RoomController.java     # POST /api/rooms, /join, /{code}/ready
│   │   ├── SubmissionController.java
│   │   └── UserController.java     # /api/users/me, /api/leaderboard
│   ├── dto/
│   │   ├── request/                # Validated inbound payloads
│   │   └── response/               # Outbound JSON shapes
│   ├── entity/                     # JPA entities (User, Problem, Match …)
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BadRequestException.java
│   │   ├── ConflictException.java
│   │   └── ResourceNotFoundException.java
│   ├── repository/                 # Spring Data JPA interfaces
│   ├── security/
│   │   ├── JwtService.java         # Token generation + validation
│   │   └── JwtAuthFilter.java      # OncePerRequestFilter
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── CodeExecutionService.java  # ProcessBuilder sandbox
│   │   ├── EloService.java
│   │   ├── MatchService.java
│   │   ├── ProblemService.java
│   │   ├── SubmissionService.java
│   │   └── UserService.java
│   └── websocket/
│       ├── MatchEvent.java          # Event payload types
│       ├── MatchEventPublisher.java # SimpMessagingTemplate wrapper
│       └── MatchWebSocketController.java
└── test/java/com/codeduel/service/
    ├── AuthServiceTest.java
    ├── EloServiceTest.java
    └── MatchServiceTest.java
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 14+
- `javac`, `python3`, `g++` on PATH (for code execution)

### 1. Database Setup

```sql
CREATE DATABASE codeduel;
```

### 2. Configure `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/codeduel
spring.datasource.username=postgres
spring.datasource.password=yourpassword

app.jwt.secret=<64-char hex string>
app.jwt.expiration-ms=86400000
```

Generate a secret:
```bash
openssl rand -hex 32
```

### 3. Run

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080`.

### 4. Run Tests

```bash
mvn test
```

---

## 📡 REST API Reference

### Auth

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | ❌ | Register new player |
| POST | `/api/auth/login` | ❌ | Login, receive JWT |

**Register body:**
```json
{ "username": "alice", "email": "alice@example.com", "password": "secret123" }
```

**Login response:**
```json
{ "token": "eyJ...", "type": "Bearer", "userId": 1, "username": "alice", "role": "PLAYER" }
```

---

### Problems

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/problems` | ✅ | List all (filter: `?difficulty=EASY&language=java`) |
| GET | `/api/problems/{id}` | ✅ | Get problem (visible test cases only) |
| GET | `/api/problems/{id}/admin` | 🔒 ADMIN | Get problem with all test cases |
| POST | `/api/problems` | 🔒 ADMIN | Create problem + test cases |
| PUT | `/api/problems/{id}` | 🔒 ADMIN | Update problem |
| DELETE | `/api/problems/{id}` | 🔒 ADMIN | Delete problem |

**Create body:**
```json
{
  "title": "Two Sum",
  "description": "Given an array of integers...",
  "difficulty": "EASY",
  "language": "java",
  "starterCode": "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n    }\n}",
  "testCases": [
    { "input": "4\n2 7 11 15\n9", "expectedOutput": "0 1", "isHidden": false },
    { "input": "3\n3 2 4\n6",      "expectedOutput": "1 2", "isHidden": true  }
  ]
}
```

---

### Rooms

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/rooms` | ✅ | List open (WAITING) rooms |
| GET | `/api/rooms/{roomCode}` | ✅ | Get room state |
| POST | `/api/rooms` | ✅ | Create room |
| POST | `/api/rooms/join` | ✅ | Join by room code |
| POST | `/api/rooms/{roomCode}/ready` | ✅ | Mark yourself ready |

---

### Submissions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/submissions` | ✅ | Submit code for active match |
| GET | `/api/submissions/match/{matchId}` | ✅ | My submissions in a match |

**Submit body:**
```json
{ "matchId": 1, "code": "class Solution { ... }", "language": "java" }
```

**Submission response:**
```json
{
  "id": 42,
  "status": "ACCEPTED",
  "testsPassed": 10,
  "totalTests": 10,
  "executionMs": 124,
  "compilerOutput": null
}
```

Possible `status` values: `ACCEPTED`, `WRONG_ANSWER`, `COMPILE_ERROR`, `RUNTIME_ERROR`, `TIME_LIMIT`

---

### Users & Leaderboard

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users/me` | ✅ | My profile |
| GET | `/api/users/{id}` | ✅ | Public profile |
| GET | `/api/leaderboard` | ❌ | Top players (`?limit=50`) |

---

## 🔌 WebSocket API

Connect to `ws://localhost:8080/ws` (SockJS fallback available).

### Client → Server

Send to `/app/match/{roomCode}/ready` to mark yourself ready.

### Server → Client Subscriptions

| Topic | When |
|-------|------|
| `/topic/match/{roomCode}` | PLAYER_JOINED, PLAYER_READY, MATCH_FINISHED |
| `/topic/match/{roomCode}/progress` | SUBMISSION_RESULT (live, both players see it) |

**MATCH_FINISHED payload:**
```json
{
  "type": "MATCH_FINISHED",
  "winnerUsername": "alice",
  "winnerEloDelta": 16,
  "loserEloDelta": -16
}
```

**SUBMISSION_RESULT payload:**
```json
{
  "type": "SUBMISSION_RESULT",
  "submission": {
    "username": "alice",
    "status": "WRONG_ANSWER",
    "testsPassed": 7,
    "totalTests": 10,
    "executionMs": 88
  }
}
```

---

## ⚙️ Code Execution Engine

`CodeExecutionService` uses Java `ProcessBuilder` to run submitted code natively.

| Language | Compile | Run |
|----------|---------|-----|
| `java` | `javac Solution.java` | `java -Xmx128m Solution` |
| `python` | (interpreted) | `python3 Solution.py` |
| `cpp` | `g++ -O2 -o solution solution.cpp` | `./solution` |

**Limits:**
- Wall clock timeout: 5 000 ms (configurable via `app.execution.timeout-ms`)
- JVM heap: 128 MB (`-Xmx128m`)
- Max output lines captured: 1 000

> ⚠️ **Production note:** For a real deployment, wrap each subprocess in Docker or [nsjail](https://github.com/google/nsjail) and run it as an unprivileged OS user. The ProcessBuilder approach is appropriate for a portfolio demo.

---

## 📐 ELO Rating System

```
Expected score: E  = 1 / (1 + 10^((opponentRating − playerRating) / 400))
New rating:     R' = R + K × (actual − expected)
```

| Condition | K factor |
|-----------|---------|
| < 10 total matches | 40 (new player) |
| ≥ 10 total matches | 20 (established) |

Elo is floored at **100** to prevent players from going negative.

---

## 🛡 Security Model

- All endpoints except `/api/auth/**` and `/api/leaderboard/**` require a valid JWT
- JWTs are verified on every request via `JwtAuthFilter` (stateless — no sessions)
- Admin-only mutations use `@PreAuthorize("hasRole('ADMIN')")`
- Passwords are hashed with BCrypt (strength 10)
- CORS is configured for `localhost:3000` and `localhost:5173` (Vite / CRA defaults)

---

## 🧪 Testing

| Test class | What's covered |
|---|---|
| `AuthServiceTest` | Register success, duplicate username/email, login flow |
| `EloServiceTest` | Symmetry, favourites/underdogs, K-factor, boundary values |
| `MatchServiceTest` | Room creation, joining (full/inactive room), idempotent join, auto-start |

Run with:
```bash
mvn test
```

---

## 🗺 Roadmap / Future Improvements

- [ ] Docker Compose setup (app + postgres)
- [ ] Secure execution sandbox (nsjail / Docker-in-Docker)
- [ ] Match time-limit enforcement via scheduled tasks
- [ ] Spectator mode (WebSocket subscribe without participating)
- [ ] Multiple language support per problem
- [ ] Admin dashboard frontend (React)
- [ ] GitHub Actions CI pipeline

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Database | PostgreSQL 14 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JJWT 0.12 |
| Real-time | Spring WebSocket + STOMP |
| Mapping | ModelMapper |
| Build | Maven |
| Testing | JUnit 5 + Mockito + AssertJ |

---

## 👨‍💻 Author

Built as a portfolio project demonstrating Spring Boot backend development, real-time systems, and competitive programming infrastructure.
