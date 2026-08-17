#!/bin/bash

# Stop both backend and frontend applications
SCRIPT_DIR="$(dirname "$0")"

echo "Stopping all applications..."
echo "========================================"

# Stop frontend
echo "1. Stopping frontend..."
"$SCRIPT_DIR/stop-frontend.sh"

# Stop backend
echo "2. Stopping backend..."
"$SCRIPT_DIR/stop-backend.sh"

echo "========================================"
echo "All applications stopped!"
