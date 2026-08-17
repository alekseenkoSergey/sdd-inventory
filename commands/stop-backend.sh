#!/bin/bash

# Stop the Spring Boot backend application
echo "Stopping backend application..."

# Kill processes on port 8080
lsof -ti:8080 | xargs kill -9 2>/dev/null || echo "No process found on port 8080"

echo "Backend stopped."
