#!/bin/bash

# Stop Docker Compose containers
cd "$(dirname "$0")/.."

echo "Stopping Docker Compose containers..."
docker compose down

echo "Containers stopped!"
