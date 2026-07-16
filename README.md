# Restaurant Service

Production-ready Restaurant Service for a food delivery platform built with Java 21 and Spring Boot 3.4.1.

## Table of Contents

- [Service Responsibility](#service-responsibility)
- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Event Publishing](#event-publishing)
- [Testing](#testing)
- [Deployment](#deployment)
- [MVP Limitations](#mvp-limitations)

---

## Service Responsibility

The Restaurant Service owns:

- Restaurant profiles, addresses, and business hours
- Restaurant status and availability management
- Cuisine types and menu management
- Menu categories, items, prices, and availability
- Delivery settings and order validation
- Restaurant search and filtering
- Event publishing for restaurant and menu changes

The Restaurant Service does **not** own:

- Customer orders or payments
- Courier assignments
- Customer profiles
- Notifications

---

## Architecture Overview

### Domain Model

```
Restaurant (1) ─── (1) RestaurantAddress
     │
     ├─── (n) BusinessHour
     ├─── (n) MenuCategory ─── (n) MenuItem
     ├─── (1) DeliverySetting
     └─── (n) Cuisine (many-to-many)
```

### Design Patterns

- **Transactional Outbox Pattern**: Database changes and event creation happen atomically; a separate scheduled process publishes events to Kafka
- **Repository Pattern**: Spring Data JPA repositories with custom queries
- **DTO Pattern**: MapStruct mappers convert entities to DTOs; entities never exposed in APIs
- **Specification Pattern**: Dynamic restaurant search queries using JPA Specifications

### Database Schema

- PostgreSQL with schema: `restaurant_service`
- UUID primary keys throughout
- Optimistic locking on Restaurant, MenuCategory, and MenuItem
- Flyway for database migrations
- Indexes on frequently queried columns

---

## Technology Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.1 |
| Web | Spring Web, Spring MVC |
| Data | Spring Data JPA, Hibernate |
| Database | PostgreSQL 15 |
| Migration | Flyway |
| Security | Spring Security, JWT Resource Server |
| Validation | Spring Validation, Hibernate Validator |
| Messaging | Spring Kafka |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Mapping | MapStruct 1.6.3 |
| Utilities | Lombok |
| Resilience | Resilience4j |
| Monitoring | Micrometer, Spring Boot Actuator |
| Testing | JUnit 5, Mockito, Testcontainers |
| Build | Maven 3.9+ |
| Container | Docker, AWS ECS/Fargate |

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose

### Environment Variables

Required environment variables for local development (or use `application-local.yml`):

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=restaurant_service
DB_USERNAME=restaurant_user
DB_PASSWORD=restaurant_pass

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# JWT (for production, point to your auth server)
JWT_ISSUER_URI=http://localhost:8081/realms/food-delivery

# Redis (optional for caching)
REDIS_HOST=localhost
REDIS_PORT=6379

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### Local Setup

1. **Start infrastructure services**:
   ```bash
   docker compose up -d postgres kafka redis
   ```

2. **Verify services are running**:
   ```bash
   docker compose ps
   ```

3. **Run database migrations** (automatic on application startup):
   Flyway migrations are applied automatically when the application starts.

4. **Build the application**:
   ```bash
   mvn clean verify
   ```

5. **Run the application**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

6. **Access Swagger UI**:
   ```
   http://localhost:8080/swagger-ui.html
   ```

7. **Access Actuator endpoints**:
   ```
   http://localhost:8080/actuator/health
   http://localhost:8080/actuator/prometheus
   ```

### Quick Start with Docker

Build and run using Docker:

```bash
# Build Docker image
docker build -t restaurant-service:latest .

# Run with docker compose
docker compose up
```

---

## API Documentation

### Public Browse APIs

#### Search Restaurants

```bash
GET /api/v1/restaurants?city=Toronto&acceptingOrders=true&page=0&size=20&sort=averageRating,desc
```

Query parameters:
- `city`, `postalCode`, `cuisine`, `name`: Filtering
- `acceptingOrders`: Boolean filter
- `latitude`, `longitude`, `radiusKm`: Geo-based filtering (future)
- `page`, `size`, `sort`: Pagination and sorting

#### Get Restaurant Details

```bash
GET /api/v1/restaurants/{restaurantId}
```

Returns restaurant details, address, cuisines, business hours, delivery settings, and current open/closed status.

#### Get Restaurant Menu

```bash
GET /api/v1/restaurants/{restaurantId}/menu
```

Returns active categories and available menu items grouped by category.

### Restaurant Management APIs

#### Create Restaurant

```bash
POST /api/v1/restaurants
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "name": "Toronto Spice Kitchen",
  "description": "Indian cuisine and street food",
  "phone": "+14165551234",
  "email": "owner@example.com",
  "currency": "CAD",
  "timezone": "America/Toronto",
  "estimatedPreparationMinutes": 25,
  "address": {
    "addressLine1": "100 King Street West",
    "city": "Toronto",
    "province": "ON",
    "postalCode": "M5X 1A9",
    "country": "CA",
    "latitude": 43.6487,
    "longitude": -79.3817
  },
  "cuisineIds": ["<uuid>", "<uuid>"]
}
```

**Roles**: `RESTAURANT_OWNER`, `ADMIN`

#### Update Restaurant Status

```bash
PATCH /api/v1/restaurants/{restaurantId}/status
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "status": "ACTIVE"
}
```

**Roles**: `ADMIN` (for ACTIVE/SUSPENDED), `RESTAURANT_OWNER` (for DRAFT→PENDING_APPROVAL)

#### Update Accepting Orders Status

```bash
PATCH /api/v1/restaurants/{restaurantId}/accepting-orders
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "acceptingOrders": true
}
```

**Roles**: `RESTAURANT_OWNER`, `ADMIN`

#### Update Business Hours

```bash
PUT /api/v1/restaurants/{restaurantId}/business-hours
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "businessHours": [
    {
      "dayOfWeek": "MONDAY",
      "openTime": "09:00",
      "closeTime": "22:00",
      "closed": false
    },
    {
      "dayOfWeek": "SUNDAY",
      "closed": true
    }
  ]
}
```

### Menu Management APIs

#### Create Menu Category

```bash
POST /api/v1/restaurants/{restaurantId}/menu-categories
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "name": "Main Course",
  "description": "Traditional main dishes",
  "displayOrder": 1,
  "active": true
}
```

#### Create Menu Item

```bash
POST /api/v1/restaurants/{restaurantId}/menu-items
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "categoryId": "<uuid>",
  "name": "Butter Chicken",
  "description": "Chicken cooked in tomato and cream sauce",
  "price": 17.99,
  "currency": "CAD",
  "imageUrl": "https://example.com/butter-chicken.jpg",
  "available": true,
  "vegetarian": false,
  "vegan": false,
  "glutenFree": true,
  "spicyLevel": 2,
  "preparationMinutes": 20,
  "displayOrder": 1
}
```

#### Update Menu Item Availability

```bash
PATCH /api/v1/restaurants/{restaurantId}/menu-items/{menuItemId}/availability
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "available": false
}
```

### Internal Service APIs

#### Validate Order

```bash
POST /api/v1/internal/restaurants/{restaurantId}/validate-order
Authorization: Bearer <SERVICE_JWT>
Content-Type: application/json

{
  "orderType": "DELIVERY",
  "items": [
    {
      "menuItemId": "<uuid>",
      "quantity": 2,
      "expectedUnitPrice": 17.99
    }
  ]
}
```

**Role**: `SERVICE`

Response includes validation result, current prices, availability, and computed subtotal.

### Delivery Settings APIs

#### Update Delivery Settings

```bash
PUT /api/v1/restaurants/{restaurantId}/delivery-settings
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "deliveryRadiusKm": 8.0,
  "minimumOrderAmount": 15.00,
  "deliveryFee": 3.99,
  "freeDeliveryThreshold": 50.00,
  "estimatedDeliveryMinutes": 35,
  "deliveryEnabled": true,
  "pickupEnabled": true
}
```

---

## Event Publishing

### Kafka Topics

- **Topic**: `restaurant-events`

### Event Types

```
RESTAURANT_CREATED
RESTAURANT_UPDATED
RESTAURANT_ACTIVATED
RESTAURANT_DEACTIVATED
RESTAURANT_ACCEPTING_ORDERS_CHANGED
MENU_CATEGORY_CREATED
MENU_CATEGORY_UPDATED
MENU_CATEGORY_DEACTIVATED
MENU_ITEM_CREATED
MENU_ITEM_UPDATED
MENU_ITEM_AVAILABILITY_CHANGED
DELIVERY_SETTINGS_UPDATED
```

### Event Envelope Format

```json
{
  "eventId": "uuid",
  "eventType": "MENU_ITEM_UPDATED",
  "aggregateType": "RESTAURANT",
  "aggregateId": "uuid",
  "occurredAt": "2026-07-15T18:00:00Z",
  "version": 1,
  "correlationId": "uuid",
  "payload": {
    "restaurantId": "uuid",
    "menuItemId": "uuid",
    "name": "Butter Chicken",
    "price": 17.99,
    "available": true
  }
}
```

### Transactional Outbox Pattern

1. Application transaction updates restaurant data and inserts `outbox_event` row atomically
2. Scheduled `OutboxEventPublisherService` reads PENDING events
3. Events are published to Kafka
4. Successfully published events are marked PUBLISHED
5. Failed events are retried with exponential backoff
6. After maximum retries, events are marked FAILED and logged

---

## Testing

### Run All Tests

```bash
mvn clean verify
```

### Run Unit Tests Only

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify -Dtest=*IntegrationTest
```

### Test Categories

- **Unit Tests**: Service layer, business logic, mappers
- **Repository Tests**: JPA queries, specifications, Testcontainers PostgreSQL
- **Controller Tests**: MockMvc, Spring Security, validation
- **Kafka Tests**: Outbox publishing, event payloads

---

## Deployment

### Build Docker Image

```bash
docker build -t restaurant-service:latest .
```

### Tag for ECR

```bash
docker tag restaurant-service:latest <aws-account-id>.dkr.ecr.ca-central-1.amazonaws.com/restaurant-service:latest
```

### Push to ECR

```bash
aws ecr get-login-password --region ca-central-1 | docker login --username AWS --password-stdin <aws-account-id>.dkr.ecr.ca-central-1.amazonaws.com
docker push <aws-account-id>.dkr.ecr.ca-central-1.amazonaws.com/restaurant-service:latest
```

### ECS Deployment

The service is designed to run on AWS ECS with Fargate.

**Task Definition**: See `ecs-task-definition.json`

**CI/CD**: GitHub Actions workflow at `.github/workflows/restaurant-service.yml`

**Environment Variables**: Provided via AWS Secrets Manager

**Health Check**: `/actuator/health`

**Port**: 8080

---

## MVP Limitations

The following features are accepted limitations for the MVP and can be added later:

- ✅ Radius-based delivery validation is simplified (no geographic polygon zones)
- ✅ No Elasticsearch full-text search (using PostgreSQL ILIKE)
- ✅ No restaurant recommendation engine
- ✅ No complex menu customization or add-ons initially
- ✅ No multilingual menu support initially
- ✅ No holiday-specific business hours initially
- ✅ No image upload service (image URLs are stored)
- ✅ Ratings are read-only (updated through events from Review Service)
- ✅ No restaurant staff assignment table (can be added later)

The architecture supports adding these features without major refactoring.

---

## Maven Commands

```bash
# Clean and build
mvn clean package

# Run tests
mvn test

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Skip tests
mvn package -DskipTests

# Generate code coverage
mvn clean verify jacoco:report
```

---

## Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Actuator**: http://localhost:8080/actuator
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus
- **Kafka UI**: http://localhost:8090 (via Docker Compose)

---

## License

Proprietary - Food Delivery Platform

---

## Contact

For questions or support, contact the Platform Engineering team.
