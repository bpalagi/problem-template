# ACME Orders Service

A Java Spring Boot application for managing orders.

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

API documentation is available at: http://localhost:8080/swagger-ui.html

### Run Unit Tests

```bash
mvn test -s settings-local.xml
```

### Run the Load Test

In a separate terminal:

```bash
cd app
./loadtest.sh
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/orders/count` | Get total order count |
| GET | `/api/orders/{id}` | Get order by ID |
| GET | `/api/orders/number/{orderNumber}` | Get order by order number |
| GET | `/api/orders/number/{orderNumber}/details` | Get order with line items |
| GET | `/api/orders/recent?limit=N` | Get recent orders with line items |
| POST | `/api/orders` | Create a new order |
| PUT | `/api/orders/{id}` | Update order by ID |
| PUT | `/api/orders/number/{orderNumber}` | Update order by order number |
| DELETE | `/api/orders/{id}` | Delete an order |

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
