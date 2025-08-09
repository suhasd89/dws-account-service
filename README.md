# Account Transfer Challenge

A Spring Boot application for managing accounts and transferring funds between them.

## Features

- Create new accounts with unique IDs and initial balances
- Retrieve account details by ID
- Transfer funds between accounts with validation and notifications
- In-memory data storage for simplicity

## API Endpoints

### Create Account
- **POST** `/v1/accounts`
- **Request Body:**
  ```json
  {
    "accountId": "12345",
    "balance": 1000.00
  }
  ```
- **Response:** 201 Created or 400 Bad Request (if duplicate or invalid)

### Get Account
- **GET** `/v1/accounts/{accountId}`
- **Response:** Account details or 404 Not Found

### Transfer Funds
- **POST** `/v1/accounts/transfer`
- **Request Body:**
  ```json
  {
    "accountFromId": "12345",
    "accountToId": "67890",
    "amount": 100.00
  }
  ```
- **Response:** 200 OK or 400 Bad Request (if invalid)

## Swagger UI

Once application is running, you can access API documentation at: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Request & Response Flow

- Client sends HTTP request to REST endpoint.
- Controller validates input and delegates to service layer.
- Service performs business logic, updates accounts, and triggers notifications.
- Added sample postman collection for testing purpose.

## Following Production-Ready Enhancements can be done w.r.t this service

- Persistent database storage (e.g., PostgreSQL, MySQL) instead of in-memory.
- Authentication and authorization (e.g., OAuth2, JWT).
- Input validation and error handling improvements.
- API rate limiting and monitoring.
- Transaction management for fund transfers.
- Logging and audit trails.
- Containerization and orchestration (Docker, Kubernetes).
- Health checks and metrics (Actuator).
- Automated tests (unit, integration, e2e).
- API versioning and documentation.
