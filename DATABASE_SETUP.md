# PostgreSQL Database Setup

This project uses PostgreSQL as the primary database, configured via Docker Compose.

## Quick Start

### Prerequisites
- Docker and Docker Compose installed

### Starting the Database

```bash
# Start PostgreSQL container
docker-compose up -d

# Verify the database is running
docker-compose ps
```

The database will be available at `localhost:5432` with the following credentials:
- **Database**: `sdd_inventory`
- **Username**: `inventory_user`
- **Password**: `inventory_password`

### Running the Application

```bash
# Build the project
./mvnw clean install

# Run the Spring Boot application
./mvnw spring-boot:run
```

Spring Boot will automatically apply Flyway migrations on startup.

### Stopping the Database

```bash
# Stop containers
docker-compose down

# Stop and remove all volumes (resets database)
docker-compose down -v
```

## Database Configuration

### Production (PostgreSQL)
- **File**: `src/main/resources/application.yml`
- **Default profile**: PostgreSQL via docker-compose
- **Hibernate DDL**: `validate` (only validates schema, no auto-creation)

### Testing (H2)
- **File**: `src/main/resources/application-test.yml`
- **Profile**: `test`
- **Hibernate DDL**: `create-drop` (creates schema on startup, drops on shutdown)

Run tests with:
```bash
./mvnw test
```

## Flyway Migrations

Database migrations are located in `src/main/resources/db/migration/`

Naming convention: `V{version}__{description}.sql`

Example:
```bash
V001__create_initial_schema.sql
V002__add_users_table.sql
```

## Docker Compose Services

### PostgreSQL (v16-Alpine)
- **Image**: `postgres:16-alpine`
- **Port**: `5432`
- **Volume**: `postgres_data` (persistent storage)
- **Health Check**: Enabled with 10s interval

## Environment Variables

Copy `.env.example` to `.env` to customize database credentials:

```bash
cp .env.example .env
```

Then modify as needed and run:
```bash
docker-compose up -d
```

## Troubleshooting

### Database connection refused
```bash
# Check if container is running
docker-compose ps

# View logs
docker-compose logs postgres

# Rebuild container
docker-compose down -v
docker-compose up -d
```

### Permission denied errors
```bash
# Ensure Docker is running and user has permissions
sudo usermod -aG docker $USER
```

### Reset database
```bash
# Remove all containers and volumes
docker-compose down -v

# Start fresh
docker-compose up -d
```
