#!/bin/bash

# Start Docker Compose containers
cd "$(dirname "$0")/.."

echo "Starting Docker Compose containers..."
docker compose up -d

echo ""
echo "Containers started!"
echo "Database: postgresql://localhost:5432/sdd_inventory"
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:4200"
