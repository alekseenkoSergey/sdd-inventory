#!/bin/bash

# Start the Spring Boot backend application
cd "$(dirname "$0")/../backend"

echo "Starting backend application..."
echo "Note: Ensure Docker Compose services are running (use ./commands/start-docker.sh)"
./mvnw spring-boot:run
