#!/bin/bash
# Load test script for Orders application
# This script runs performance tests against all API endpoints

set -e

BASE_URL="http://localhost:8080"

echo "========================================"
echo "  ACME Orders - Load Test Suite"
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

echo "========================================"
echo "  Running API Performance Tests"
echo "========================================"
echo ""

# Test 1: GET order by ID
echo "Test 1: GET /api/orders/{id}"
echo "  Running 10 requests..."
TOTAL_TIME=0
for i in $(seq 1 10); do
    ID=$((RANDOM % ORDER_COUNT + 1))
    START=$(date +%s%N)
    curl -s "$BASE_URL/api/orders/$ID" > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
done
AVG1=$((TOTAL_TIME / 10))
echo "  Average: ${AVG1}ms"
echo ""

# Test 2: GET order by order number
echo "Test 2: GET /api/orders/number/{orderNumber}"
echo "  Running 10 requests..."
TOTAL_TIME=0
for i in $(seq 1 10); do
    ORDER_NUM=$(printf "ORD-%08d" $((RANDOM % ORDER_COUNT + 1)))
    START=$(date +%s%N)
    curl -s "$BASE_URL/api/orders/number/$ORDER_NUM" > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
done
AVG2=$((TOTAL_TIME / 10))
echo "  Average: ${AVG2}ms"
echo ""

# Test 3: GET order with details (single order)
echo "Test 3: GET /api/orders/number/{orderNumber}/details"
echo "  Running 10 requests..."
TOTAL_TIME=0
for i in $(seq 1 10); do
    ORDER_NUM=$(printf "ORD-%08d" $((RANDOM % ORDER_COUNT + 1)))
    START=$(date +%s%N)
    curl -s "$BASE_URL/api/orders/number/$ORDER_NUM/details" > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
done
AVG3=$((TOTAL_TIME / 10))
echo "  Average: ${AVG3}ms"
echo ""

# Test 4: POST create order
echo "Test 4: POST /api/orders"
echo "  Running 5 requests..."
TOTAL_TIME=0
for i in $(seq 1 5); do
    ORDER_NUM="ORD-TEST-$(date +%s%N)"
    START=$(date +%s%N)
    curl -s -X POST "$BASE_URL/api/orders" \
        -H "Content-Type: application/json" \
        -d "{\"orderNumber\":\"$ORDER_NUM\",\"customerName\":\"Test Customer\",\"status\":\"PENDING\",\"amount\":99.99}" \
        > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
done
AVG4=$((TOTAL_TIME / 5))
echo "  Average: ${AVG4}ms"
echo ""

# Test 5: PUT update order
echo "Test 5: PUT /api/orders/{id}"
echo "  Running 5 requests..."
TOTAL_TIME=0
for i in $(seq 1 5); do
    ID=$((RANDOM % ORDER_COUNT + 1))
    START=$(date +%s%N)
    curl -s -X PUT "$BASE_URL/api/orders/$ID" \
        -H "Content-Type: application/json" \
        -d '{"customerName":"Updated Customer","status":"SHIPPED","amount":149.99}' \
        > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
done
AVG5=$((TOTAL_TIME / 5))
echo "  Average: ${AVG5}ms"
echo ""

# Test 6: GET recent orders with details
echo "Test 6: GET /api/orders/recent?limit=100"
echo "  Running 5 requests..."
TOTAL_TIME=0
TIMES=""
for i in $(seq 1 5); do
    START=$(date +%s%N)
    curl -s "$BASE_URL/api/orders/recent?limit=100" > /dev/null
    END=$(date +%s%N)
    DURATION=$(( (END - START) / 1000000 ))
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
    TIMES="$TIMES ${DURATION}ms"
done
AVG6=$((TOTAL_TIME / 5))
echo "  Individual:$TIMES"
echo "  Average: ${AVG6}ms"
echo ""

# Summary
echo "========================================"
echo "  Results Summary"
echo "========================================"
echo ""
printf "  %-45s %8s\n" "Endpoint" "Avg Time"
printf "  %-45s %8s\n" "---------------------------------------------" "--------"
printf "  %-45s %6dms\n" "GET  /api/orders/{id}" "$AVG1"
printf "  %-45s %6dms\n" "GET  /api/orders/number/{orderNumber}" "$AVG2"
printf "  %-45s %6dms\n" "GET  /api/orders/number/{orderNumber}/details" "$AVG3"
printf "  %-45s %6dms\n" "POST /api/orders" "$AVG4"
printf "  %-45s %6dms\n" "PUT  /api/orders/{id}" "$AVG5"
printf "  %-45s %6dms\n" "GET  /api/orders/recent?limit=100" "$AVG6"
echo ""
echo "  Target: All endpoints should respond in <100ms"
echo ""
