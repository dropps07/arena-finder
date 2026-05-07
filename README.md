# Arena Finder

A microservices platform to discover, search, and book nearby sports turfs — with an AI-powered natural language search.

Built with: Java 21 · Spring Boot 3.2 · PostgreSQL · Kafka · Docker · Spring Cloud Gateway

---

## Architecture

```
Browser / Mobile App
        │
        ▼
 [ API Gateway :8080 ]  ←  single entry point, JWT validation here
        │
   ┌────┼────┬──────────────┐
   ▼    ▼    ▼              ▼
 Auth Arena Booking        AI
 8081  8082  8083          8085
              │  │
              │  └─── Kafka ──► Notification :8084
              │
         (REST call: Booking verifies slot with Arena)
```

## Services

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | Routes all requests, validates JWTs |
| auth-service | 8081 | Login, register, JWT generation |
| arena-service | 8082 | Arena CRUD, turf management, geo search |
| booking-service | 8083 | Slot booking, availability checks |
| notification-service | 8084 | Email/notifications via Kafka events |
| ai-service | 8085 | Natural language → structured arena search |

---

## Setup (VS Code)

### Step 1 — Prerequisites

| Tool | Why | Install |
|---|---|---|
| Java 21 (Temurin) | Runs Spring Boot | [adoptium.net](https://adoptium.net) |
| Maven 3.9+ | Builds the project | [maven.apache.org](https://maven.apache.org/install.html) |
| Docker Desktop | Runs DBs and Kafka | [docker.com](https://www.docker.com/products/docker-desktop) |
| VS Code | Editor | [code.visualstudio.com](https://code.visualstudio.com) |

Verify installs:
```bash
java -version    # openjdk 21
mvn -version     # Apache Maven 3.9.x
docker version   # shows Client and Server
```

### Step 2 — Open correctly in VS Code

```bash
code arena-finder.code-workspace
```

> ⚠️ Open the `.code-workspace` file, NOT just the folder. This loads all 6 services correctly for the Java Language Server.

VS Code will prompt to install recommended extensions. **Click "Install All"** and wait for the Java indexing to finish (watch the status bar at the bottom).

### Step 3 — Environment variables

```bash
cp .env.example .env
# then edit .env with your values
```

### Step 4 — Start infrastructure

`Ctrl+Shift+P` → **Tasks: Run Task** → **🐳 Start Infrastructure (DBs + Kafka)**

Verify at http://localhost:8090 (Kafka UI should load).

### Step 5 — Run a service with the debugger

`Ctrl+Shift+D` → pick **🔐 Auth Service** → press **F5**

---

## Testing APIs

Open files in `http-tests/` — click **"Send Request"** above any block.
No Postman needed. Tests live in the repo and stay in sync with the code.

Swagger UI (once a service is running):
- http://localhost:8081/swagger-ui.html — Auth
- http://localhost:8082/swagger-ui.html — Arena
- http://localhost:8083/swagger-ui.html — Booking

---

## Common Commands

| What | How |
|---|---|
| Build all | `Ctrl+Shift+B` |
| Run tests | `Ctrl+Shift+P` → Tasks → ☕ Run All Tests |
| Start infra | `Ctrl+Shift+P` → Tasks → 🐳 Start Infrastructure |
| Stop all | `Ctrl+Shift+P` → Tasks → 🐳 Stop All Containers |
| Fresh start | `Ctrl+Shift+P` → Tasks → 🐳 Stop All + Wipe Data |
| Debug multiple | `Ctrl+Shift+D` → 🚀 All Services → F5 |

---

## Project Structure

```
arena-finder/
├── arena-finder.code-workspace   ← open THIS in VS Code
├── .vscode/
│   ├── extensions.json           ← auto-installs extensions
│   ├── launch.json               ← F5 debug configs per service
│   ├── tasks.json                ← docker/maven tasks
│   └── settings.json             ← editor + java settings
├── http-tests/                   ← API tests (no Postman)
├── pom.xml                       ← parent POM
├── docker-compose.yml
├── .env.example
└── [auth|arena|booking|notification|ai]-service/
    └── api-gateway/
```

---

## Rules (apply to every phase)

1. No logic in controllers — only in the Service layer
2. Never return JPA entities from endpoints — always use DTOs
3. Never query another service's database directly — use its REST API
4. Every Service class method needs at least one unit test
5. Every service has its own `README.md`

---

## Build Progress

- [x] Phase 1 — Skeleton, Docker Compose, VS Code setup, CI
- [x] Phase 2 — Auth Service
- [ ] Phase 3 — Arena Service
- [ ] Phase 4 — Booking Service
- [ ] Phase 5 — API Gateway
- [ ] Phase 6 — Kafka + Notifications
- [ ] Phase 7 — AI Recommender
