#!/bin/bash

# Database Validation Script for SSO Authentication Feature
# Validates that the users table exists with correct schema and constraints

set -e

echo "=========================================="
echo "SSO Authentication Database Validation"
echo "=========================================="
echo ""

DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="sdd_inventory"
DB_USER="inventory_user"
DB_PASS="inventory_password"

# Set PostgreSQL password
export PGPASSWORD="$DB_PASS"

echo "1. Checking PostgreSQL connection..."
psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" > /dev/null
echo "   ✓ Connected to PostgreSQL"
echo ""

echo "2. Checking users table exists..."
TABLE_EXISTS=$(psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -c "
  SELECT EXISTS(
    SELECT 1 FROM information_schema.tables
    WHERE table_name = 'users'
  );"
)

if [ "$TABLE_EXISTS" = "t" ]; then
  echo "   ✓ Users table exists"
else
  echo "   ✗ Users table NOT found"
  exit 1
fi
echo ""

echo "3. Validating table schema..."
psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "\d users" | head -15
echo ""

echo "4. Checking required columns..."
COLUMNS=$(psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -c "
  SELECT string_agg(column_name, ', ' ORDER BY column_name)
  FROM information_schema.columns
  WHERE table_name = 'users';"
)

REQUIRED=("id" "provider" "provider_user_id" "email" "display_name" "avatar_url" "created_at" "updated_at")

for col in "${REQUIRED[@]}"; do
  if [[ "$COLUMNS" == *"$col"* ]]; then
    echo "   ✓ Column '$col' exists"
  else
    echo "   ✗ Column '$col' NOT found"
    exit 1
  fi
done
echo ""

echo "5. Checking unique constraint on (provider, provider_user_id)..."
CONSTRAINT=$(psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -c "
  SELECT constraint_name
  FROM information_schema.table_constraints
  WHERE table_name = 'users' AND constraint_type = 'UNIQUE'
  AND constraint_name LIKE '%provider%provider_user%';"
)

if [ -n "$CONSTRAINT" ]; then
  echo "   ✓ Unique constraint exists: $CONSTRAINT"
else
  echo "   ✗ Unique constraint NOT found"
  exit 1
fi
echo ""

echo "6. Checking indexes..."
INDEXES=$(psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "
  SELECT indexname FROM pg_indexes WHERE tablename = 'users';" | tail -n +3
)

if [ -z "$INDEXES" ]; then
  echo "   ⚠ No indexes found (might be expected)"
else
  echo "   ✓ Indexes found:"
  echo "$INDEXES" | sed 's/^/     /'
fi
echo ""

echo "7. Checking current user count..."
USER_COUNT=$(psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM users;")
echo "   Total users: $USER_COUNT"
echo ""

if [ "$USER_COUNT" -gt 0 ]; then
  echo "8. Showing existing users..."
  psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "
    SELECT id, provider, provider_user_id, email, display_name, created_at
    FROM users
    ORDER BY id;"
  echo ""
fi

echo "=========================================="
echo "✓ Database validation complete!"
echo "=========================================="
echo ""
echo "Ready for testing. Run quickstart scenarios:"
echo "  1. First-Time Login"
echo "  2. Returning User (deduplication test)"
echo "  3. Session Persistence"
echo "  4. Logout"
echo "  5. Session Expiry"
echo "  6. OAuth Failure Handling"
echo "  7. Logging & Audit Trail"