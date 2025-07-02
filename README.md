# Medical Logistics Order Management System

A Domain-Driven Design (DDD) implementation of a medical logistics order management system built with Spring Boot, designed for high I/O operations in a cloud-native environment.

## Architecture Overview

This project implements a medical logistics order system following Domain-Driven Design principles with a focus on:
- **Clean Architecture**: Clear separation between domain, application, and infrastructure layers
- **Test-Driven Development**: High test coverage with business logic driven by tests
- **Cloud-Native Design**: Built for containerization and horizontal scaling
- **I/O Optimization**: Designed to handle high-throughput operations

```
            API Request                                          
                │                                                
                ▼                                                
       ┌─────────────────┐                                       
       │   Controller    │  (Interface Layer)                    
       └────────┬────────┘                                       
                │                                                
                ▼                                                
       ┌─────────────────┐                                       
       │   Application   │  (Orchestrates flow)                  
       │    Service      │                                       
       │                 ├──── uses ───> Domain (Pure logic)     
       │                 │                                       
       │                 ├──── uses ───> Repository (Persistence)
       └─────────────────┘                                       
```
## Project Structure

```
src/main/java/com/medical/logistics/
├── domain/          # Business logic (Order, OrderItem, OrderStatus)
├── application/     # Use cases (OrderApplicationService, Commands)
├── infrastructure/  # Technical details (Repository, Config)
└── interfaces/      # REST API (Controllers, DTOs)

Key files:
├── Dockerfile                                    #  Docker build
├── run-docker.sh                                # Docker automation script
├── Medical-Logistics-API.postman_collection.json # API test collection
└── .github/workflows/ci.yml               # CI/CD pipeline
```


## Features

### Core Functionality
- **Order Management**: Create, approve, and cancel medical supply orders
- **State Validation**: Orders can only transition from PENDING → APPROVED/CANCELLED
- **Item Validation**: Each order must contain at least one item with valid quantity

### Technical Features
- **RESTful API** with comprehensive error handling
- **Domain-Driven Design** with clear bounded contexts
- **Command Pattern** for operation clarity and future event sourcing
- **In-Memory Repository** with interface for easy database integration
- **Comprehensive Logging** for debugging and monitoring
- **Docker Support** with optimized multi-stage builds
- **CI/CD Pipeline** with GitHub Actions
- **API Testing** with Postman collection included

## Tech Stack

- **Java 21** - Latest LTS version for optimal performance
- **Spring Boot 3.5.3** - Modern microservice framework
- **Maven** - Dependency management and build tool
- **Docker** - Containerization for consistent deployments
- **JUnit 5** - Comprehensive testing framework
- **Lombok** - Reduces boilerplate code (used in DTOs and services)
- **SLF4J + Logback** - Structured logging


### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker (optional, for containerized deployment)

## Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/manjush/medical-logistics-api.git
   cd medical-logistics-api
   ```

2. **Run with Maven**
   ```bash
   # Run tests
   mvn clean test

   # Run the application
   mvn spring-boot:run
   ```

3. **Run with Docker (Recommended)**
   ```bash
   # Make the script executable
   chmod +x run-docker.sh

   # Build and run with automatic setup
   ./run-docker.sh
   ```

   The script will:
    - Build the Maven project
    - Create an optimized Docker image
    - Remove old images to save space
    - Start the container with health checks
    - Display real-time logs during startup

### Docker Script Options

```bash
# Full build and run (default)
./run-docker.sh

# Skip Maven build (use existing JAR)
./run-docker.sh --skip-build

# Run without showing logs
./run-docker.sh --no-logs
```

### Verify Installation

Once running, verify the application:
```bash
# Health check
curl http://localhost:8080/actuator/health

# API test
curl -X GET http://localhost:8080/api/orders
```

## API Documentation

### Base URL
```
http://localhost:8080
```

### Endpoints

#### 1. Create Order
```http
POST /api/orders
Content-Type: application/json

{
  "items": [
    {
      "name": "Surgical Masks",
      "quantity": 100
    },
    {
      "name": "Disposable Gloves",
      "quantity": 200
    }
  ]
}
```

**Response (201 Created)**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "status": "PENDING",
  "items": [
     {
        "name": "Surgical Masks",
        "quantity": 100
     },
     {
        "name": "Disposable Gloves",
        "quantity": 200
     }
  ],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 2. Get All Orders
```http
GET /api/orders
```

#### 3. Get Order by ID
```http
GET /api/orders/{orderId}
```

#### 4. Approve Order
```http
PUT /api/orders/{orderId}/approve
```
*Note: Only works for orders in PENDING status*

#### 5. Cancel Order
```http
PUT /api/orders/{orderId}/cancel
```
*Note: Only works for orders in PENDING status*

### Error Responses

All errors follow a consistent format:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Invalid Order State",
  "message": "Cannot approve order in CANCELLED status",
  "details": {}
}
```

### Postman Collection

A comprehensive Postman collection is included for easy API testing:

1. Import `Medical-Logistics-API.postman_collection.json` into Postman
2. The collection includes:
    - All API endpoints with example requests
    - Automated tests for each endpoint
    - Error scenario testing
    - Complete order lifecycle examples

##  Testing

### Running Tests

```bash
# Run all tests
mvn clean test

```


### Test Structure

- **Domain Tests**: Business logic validation
- **Application Tests**: Use case testing with mocks
- **Integration Tests**: Full API endpoint testing
- **Repository Tests**: Data persistence verification

### TDD Approach

This project was built using Test-Driven Development:
1. Tests were written first to define behavior
2. Implementation followed to make tests pass
3. Code was refactored while maintaining green tests



## System Design

### I/O Heavy Considerations

The system is designed to handle high I/O operations through:

1. **Repository Pattern**: Allows easy switching between storage implementations
2. **Stateless Services**: Enables horizontal scaling
3. **Command Pattern**: Prepares for async processing and event sourcing
4. **Efficient Data Structures**: Uses `ConcurrentHashMap` for thread-safe operations



## Deployment

### Docker Deployment

The application includes a multi-stage Dockerfile for optimal image size:

```bash
# Build and run manually
docker build -t medical-logistics-api .
docker run -p 8080:8080 medical-logistics-api

# Or use the provided script
./run-docker.sh
```


### CI/CD Pipeline

GitHub Actions workflow included for:
- Runs all tests on every push
- Caches dependencies for faster builds
- Docker image building

## Monitoring

### Health Checks

```bash
# Basic health
GET /actuator/health

```
## Future Enhancements

With more time, the following would be added:
- [ ] PostgreSQL persistence layer
- [ ] Redis caching for performance
- [ ] Authentication/Authorization with Spring Security
- [ ] OpenAPI/Swagger documentation
- [ ] Performance metrics dashboard
- [ ] Async processing with Spring WebFlux



