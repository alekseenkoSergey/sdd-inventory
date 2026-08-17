# Project Commands

This directory contains convenient scripts to start and stop the frontend, backend, and Docker containers for the SDD Inventory application.

## Available Commands

### Docker Compose Commands

**Start Docker containers:**
```bash
./start-docker.sh
```
Starts PostgreSQL database, backend service, and frontend service in Docker containers.
- Database: `postgresql://localhost:5432/sdd_inventory` (credentials: inventory_user / inventory_password)
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:4200`

**Stop Docker containers:**
```bash
./stop-docker.sh
```
Stops and removes all Docker containers.

---

### Individual Services

**Start Backend (requires Docker):**
```bash
./start-backend.sh
```
Starts the Spring Boot backend on port 8080 using Maven. Requires Docker containers to be running first (use `start-docker.sh`).

**Stop Backend:**
```bash
./stop-backend.sh
```
Stops the backend by killing the process on port 8080.

**Start Frontend:**
```bash
./start-frontend.sh
```
Starts the Angular frontend on port 4200 using `npm start`. Does not require Docker.

**Stop Frontend:**
```bash
./stop-frontend.sh
```
Stops the frontend by killing the process on port 4200.

---

### Combined Commands

**Start everything:**
```bash
./start-all.sh
```
Starts both frontend and backend services.

**Stop everything:**
```bash
./stop-all.sh
```
Stops both frontend and backend services.

---

## Usage Examples

### Local Development (without Docker)
```bash
# Terminal 1: Start Frontend
./start-frontend.sh

# Terminal 2: Start Docker only for database
./start-docker.sh

# Terminal 3: Start Backend
./start-backend.sh

# Or combine frontend and backend in one terminal:
./start-all.sh
```

### Using Docker Compose
```bash
# Start all services in containers
./start-docker.sh

# Access the application at http://localhost:4200

# Stop when done
./stop-docker.sh
```

---

## Notes

- All scripts are relative path aware and can be run from any directory
- The backend requires a database to be running (either via Docker or locally)
- The frontend can run independently without Docker
- Ports used:
  - Frontend: **4200**
  - Backend: **8080**
  - Database: **5432**

## Troubleshooting

**Port already in use:**
```bash
# Kill process on specific port
# For port 4200:
lsof -ti:4200 | xargs kill -9

# For port 8080:
lsof -ti:8080 | xargs kill -9

# For port 5432:
lsof -ti:5432 | xargs kill -9
```

**Docker containers not stopping:**
```bash
./stop-docker.sh
# Or manually:
docker compose down --remove-orphans
```
