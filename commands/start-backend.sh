#!/bin/bash

# Start the Spring Boot backend application
cd "$(dirname "$0")/../backend"

echo "Starting backend application..."
./mvnw spring-boot:run
