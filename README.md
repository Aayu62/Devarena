Here’s your content restructured into a **GitHub‑friendly README.md** format, keeping everything exactly as you wrote but organized with proper Markdown hierarchy and spacing:

```markdown
# DevArena

A competitive coding platform where developers can battle each other in real-time coding challenges.

## Project Status

| Module   | Status      |
|----------|-------------|
| Backend  | ✅ Complete  |
| Frontend | 🚧 In Progress |

---

## Backend

Built with **Spring Boot 4.0.2** | **Java 17** | **PostgreSQL**

### Features

- **Authentication** — JWT-based login and registration with Spring Security
- **Problems** — Problem management with multi-language starter code and test case generation
- **Battles** — Real-time 1v1 coding battles with ranked matchmaking and friendly duels (join by code)
- **Code Execution** — Integrated with Judge0 for running and evaluating submissions
- **Rating System** — ELO-style rating with history tracking
- **Alliances** — Team/group system with roles, membership, and group chat
- **Friends** — Friend requests and social features
- **Leaderboard** — Global rankings and profile stats
- **WebSocket** — Real-time communication for battles and alliance chat

### Tech Stack

- Spring Boot (Web MVC, Security, Data JPA, WebSocket)
- PostgreSQL
- JWT (jjwt 0.11.5)
- Lombok
- Judge0 (code execution engine)

### Project Structure

```
backend/
├── auth/       # JWT auth, login, register
├── battle/     # Battle logic, matchmaking, submissions, rating
├── problem/    # Problem CRUD, driver code generation, test cases
├── user/       # User profiles, leaderboard, search
├── alliance/   # Alliance management and group chat
├── friend/     # Friend system
├── config/     # Security, WebSocket, converter configs
└── common/     # Shared enums, exceptions, utilities
```

### Running Locally

1. Make sure PostgreSQL is running and update `application.properties` with your DB credentials  
2. From the `backend/` directory:

```bash
./mvnw spring-boot:run
```

---

## Frontend

🚧 Work in progress — React-based frontend is currently under development.
```
