#!/bin/bash

# Stop the Angular frontend application
echo "Stopping frontend application..."

# Kill processes on port 4200
lsof -ti:4200 | xargs kill -9 2>/dev/null || echo "No process found on port 4200"

echo "Frontend stopped."
