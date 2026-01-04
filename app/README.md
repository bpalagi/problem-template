# ACME Orders Service

A Java Spring Boot application for managing orders.

## Interview Exercise: Performance Investigation

This application has a **performance problem** that you need to identify and fix.

### Scenario

The ACME Orders team has noticed that certain API endpoints are significantly slower than others. Users are complaining about slow response times when looking up orders by order number, especially when fetching order details with line items.

### Your Task

1. **Run the load test** to observe the performance issue
2. **Investigate** the slow endpoints to determine the root cause
3. **Fix** the performance issue
4. **Verify** your fix by re-running the load test

### Hints

- The database has 50,000 orders and ~175,000 order items pre-loaded
- Compare response times between different endpoints
- Consider what makes database queries fast or slow
- You can inspect the database schema in `src/main/resources/schema.sql`
- Use `sqlite3 ./data/orders.db` to inspect the database directly

### Success Criteria

- All tests (2-4) should have similar performance to Test 1
- The fix should be minimal and targeted

---

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Run the Application

```bash
cd app
mvn spring-boot:run -s settings-local.xml
```

The application will start on `http://localhost:8080`.

### Run the Load Test

In a separate terminal:

```bash
cd app
./loadtest.sh
```

### Run Unit Tests

```bash
mvn test -s settings-local.xml
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/orders/count` | Get total order count |
| GET | `/api/orders/{id}` | Get order by ID |
| GET | `/api/orders/number/{orderNumber}` | Get order by order number |
| GET | `/api/orders/number/{orderNumber}/details` | Get order with line items |
| POST | `/api/orders` | Create a new order |
| PUT | `/api/orders/{id}` | Update order by ID |
| PUT | `/api/orders/number/{orderNumber}` | Update order by order number |

## Project Structure

```
app/
├── data/orders.db              # Pre-seeded SQLite database
├── src/main/
│   ├── java/com/acme/orders/
│   │   ├── controller/         # REST controllers
│   │   ├── model/              # Domain models
│   │   ├── repository/         # Data access layer
│   │   └── service/            # Business logic
│   └── resources/
│       ├── application.properties
│       └── schema.sql          # Database schema
├── loadtest.sh                 # Performance test script
└── pom.xml
```
