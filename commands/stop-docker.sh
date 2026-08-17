#!/bin/bash

# Stop Docker Compose containers
cd "$(dirname "$0")/.."

echo "Stopping Docker Compose containers..."
docker-compose -f compose.yaml down

echo "Containers stopped!"
