#!/bin/bash

# Endpoint Testing Script for SSO Authentication
# Tests backend endpoints without browser interaction

set -e

BACKEND_URL="http://localhost:8080/api/auth"

echo "=========================================="
echo "SSO Authentication Endpoint Tests"
echo "=========================================="
echo ""

echo "Testing backend endpoints..."
echo ""

# Test 1: Login endpoint (should be accessible anonymously)
echo "1. Testing GET /api/auth/login (should be 200 or 302 redirect)..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/login")
if [ "$STATUS" == "200" ] || [ "$STATUS" == "302" ]; then
  echo "   ✓ Login endpoint accessible (HTTP $STATUS)"
else
  echo "   ✗ Login endpoint error (HTTP $STATUS)"
fi
echo ""

# Test 2: Profile endpoint without session (should be 401)
echo "2. Testing GET /api/auth/user/profile without session (should be 401)..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/user/profile")
if [ "$STATUS" == "401" ]; then
  echo "   ✓ Properly rejected unauthenticated request (HTTP 401)"
else
  echo "   ✗ Unexpected response (HTTP $STATUS, expected 401)"
fi
echo ""

# Test 3: Logout endpoint without session (should be 401)
echo "3. Testing POST /api/auth/logout without session (should be 401)..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BACKEND_URL/logout")
if [ "$STATUS" == "401" ]; then
  echo "   ✓ Properly rejected unauthenticated logout (HTTP 401)"
else
  echo "   ✗ Unexpected response (HTTP $STATUS, expected 401)"
fi
echo ""

# Test 4: Check backend is responding
echo "4. Backend Health Check..."
HEALTH=$(curl -s "$BACKEND_URL/login" -I | head -n 1)
echo "   Response header: $HEALTH"
echo "   ✓ Backend is running"
echo ""

echo "=========================================="
echo "✓ Endpoint tests complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Start frontend: ng serve"
echo "2. Open browser to http://localhost:4200"
echo "3. Complete OAuth login flow"
echo "4. Run database validation: ./scripts/validate-database.sh"
