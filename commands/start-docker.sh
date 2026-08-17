#!/bin/bash

# Start Docker Compose containers
cd "$(dirname "$0")/.."

echo "Starting Docker Compose containers..."
docker-compose -f compose.yaml up -d

echo ""
echo "Containers started!"
echo "Database: postgresql://localhost:5432/sdd_inventory"
