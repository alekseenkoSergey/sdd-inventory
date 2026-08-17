#!/bin/bash

# Start both backend and frontend applications
SCRIPT_DIR="$(dirname "$0")"

echo "Starting all applications..."
echo "========================================"

# Start backend in background
echo "1. Starting backend..."
"$SCRIPT_DIR/start-backend.sh" &
BACKEND_PID=$!

# Give backend time to start
sleep 3

# Start frontend in background
echo "2. Starting frontend..."
"$SCRIPT_DIR/start-frontend.sh" &
FRONTEND_PID=$!

echo "========================================"
echo "All applications started!"
echo "Backend PID: $BACKEND_PID"
echo "Frontend PID: $FRONTEND_PID"
echo ""
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:4200"
