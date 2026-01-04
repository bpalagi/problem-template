#!/bin/bash
# Load test script for Orders application
# This script runs performance tests against the API endpoints
# to help identify missing database indexes

set -e

BASE_URL="http://localhost:8080"

echo "========================================"
echo "  ACME Orders - Load Test"
echo "========================================"
echo ""

# Check if server is running
if ! curl -s "$BASE_URL/api/orders/count" > /dev/null 2>&1; then
    echo "ERROR: Server is not running at $BASE_URL"
    echo "Please start the application first with:"
    echo "  mvn spring-boot:run -s settings-local.xml"
    exit 1
fi

# Get current order count
ORDER_COUNT=$(curl -s "$BASE_URL/api/orders/count")
echo "Database has $ORDER_COUNT orders"
echo ""

if [ "$ORDER_COUNT" -lt 1000 ]; then
    echo "WARNING: Database has too few orders for meaningful load test."
    echo "The pre-seeded database should have 50,000 orders."
    echo "Please ensure app/data/orders.db exists and is not corrupted."
    exit 1
fi

# Run load tests
echo "========================================"
echo "  Running Performance Tests"
echo "========================================"
echo ""

# Test 1: GET by ID (should be fast - uses primary key)
echo "Test 1: GET /api/orders/{id} (by primary key)"
echo "  Running 20 requests..."
TOTAL_TIME=0
for i in $(seq 1 20); do
    ID=$((RANDOM % ORDER_COUNT + 1))
    START=$(date +%s%N)
    curl -s "$BASE_URL/api/orders/$ID" > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
done
AVG=$((TOTAL_TIME / 20))
echo "  Average response time: ${AVG}ms"
echo ""

# Test 2: GET by order number (slow - no index!)
echo "Test 2: GET /api/orders/number/{orderNumber} (NO INDEX)"
echo "  Running 20 requests..."
TOTAL_TIME=0
for i in $(seq 1 20); do
    ORDER_NUM=$(printf "ORD-%08d" $((RANDOM % ORDER_COUNT + 1)))
    START=$(date +%s%N)
    curl -s "$BASE_URL/api/orders/number/$ORDER_NUM" > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
done
AVG=$((TOTAL_TIME / 20))
echo "  Average response time: ${AVG}ms"
echo ""

# Test 3: GET order with items (very slow - two table scans!)
echo "Test 3: GET /api/orders/number/{orderNumber}/details (NO INDEX - scans 2 tables!)"
echo "  Running 20 requests..."
TOTAL_TIME=0
TIMES=""
for i in $(seq 1 20); do
    ORDER_NUM=$(printf "ORD-%08d" $((RANDOM % ORDER_COUNT + 1)))
    START=$(date +%s%N)
    curl -s "$BASE_URL/api/orders/number/$ORDER_NUM/details" > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
    TIMES="$TIMES ${DURATION}ms"
done
AVG=$((TOTAL_TIME / 20))
echo "  Individual times:$TIMES"
echo "  Average response time: ${AVG}ms"
echo ""

# Test 4: PUT by order number (slow - no index!)
echo "Test 4: PUT /api/orders/number/{orderNumber} (NO INDEX)"
echo "  Running 10 requests..."
TOTAL_TIME=0
TIMES=""
for i in $(seq 1 10); do
    ORDER_NUM=$(printf "ORD-%08d" $((RANDOM % ORDER_COUNT + 1)))
    START=$(date +%s%N)
    curl -s -X PUT "$BASE_URL/api/orders/number/$ORDER_NUM" \
        -H "Content-Type: application/json" \
        -d '{"customerName":"Updated Customer","status":"SHIPPED","amount":149.99}' \
        > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
    TIMES="$TIMES ${DURATION}ms"
done
AVG=$((TOTAL_TIME / 10))
echo "  Individual times:$TIMES"
echo "  Average response time: ${AVG}ms"
echo ""

# Summary
echo "========================================"
echo "  Summary"
echo "========================================"
echo ""
echo "Expected results:"
echo "  - Test 1 (by ID): Should be FAST (~10-20ms)"
echo "  - Test 2 (by order_number): Should be SLOW (~20-50ms)"
echo "  - Test 3 (details with items): Should be VERY SLOW (~30-80ms)"
echo "  - Test 4 (update by order_number): Should be SLOW (~20-50ms)"
echo ""
echo "If Tests 2-4 are significantly slower than Test 1, the database"
echo "is missing indexes on the order_number column."
echo ""
echo "The candidate should identify this issue and add appropriate indexes."
echo ""
