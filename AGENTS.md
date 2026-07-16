You are a senior Java backend engineer. Build a production-ready Restaurant Service for a food delivery platform.

## 1. Technology stack

Use the following technologies:

* Java 21
* Spring Boot 3.4+
* Spring Web
* Spring Data JPA
* PostgreSQL
* Flyway
* Spring Security
* JWT resource server
* Spring Validation
* Spring Kafka
* OpenAPI/Swagger
* MapStruct
* Lombok
* Resilience4j
* Micrometer
* Spring Boot Actuator
* JUnit 5
* Mockito
* Testcontainers
* Maven
* Docker
* AWS ECS/Fargate-ready configuration

Use clean, maintainable, production-quality Java code.

Base package:

```text
com.fooddelivery.restaurant
```

Use this module structure:

```text
restaurant-service
├── controller
├── service
├── repository
├── entity
├── dto
│   ├── request
│   └── response
├── mapper
├── exception
├── security
├── event
│   ├── producer
│   └── model
├── config
├── specification
├── validation
└── util
```

Use constructor injection only.

Do not use field injection.

---

# 2. Service responsibility

The Restaurant Service must own:

* Restaurant profile
* Restaurant address
* Restaurant business hours
* Restaurant open or closed status
* Cuisine types
* Menu categories
* Menu items
* Menu item prices
* Menu item availability
* Restaurant delivery settings
* Restaurant order-validation API
* Restaurant search
* Menu management by restaurant owners
* Restaurant status updates
* Publishing restaurant and menu change events

The Restaurant Service must not own:

* Customer orders
* Payments
* Courier assignments
* Customer profiles
* Notifications

Other services must never access the Restaurant Service database directly.

---

# 3. User roles

Support these JWT roles:

```text
CUSTOMER
RESTAURANT_OWNER
RESTAURANT_STAFF
ADMIN
SERVICE
```

Authorization rules:

* Customers can browse restaurants and menus.
* Restaurant owners can manage only restaurants they own.
* Restaurant staff can manage assigned restaurants.
* Admin users can manage all restaurants.
* Internal service APIs require the `SERVICE` role.
* Public browse endpoints require authentication but allow the `CUSTOMER` role.
* Health endpoints should remain accessible for infrastructure monitoring.

Get the authenticated user ID from the JWT `sub` claim.

Get roles from the JWT roles or authorities claim.

---

# 4. Core entities

Create the following PostgreSQL-backed JPA entities.

## Restaurant

Fields:

```text
id UUID primary key
ownerId UUID not null
name varchar(150) not null
description varchar(1000)
phone varchar(30)
email varchar(150)
status enum
acceptingOrders boolean
minimumOrderAmount decimal(10,2)
deliveryFee decimal(10,2)
estimatedPreparationMinutes integer
averageRating decimal(3,2)
totalRatings integer
currency varchar(3)
timezone varchar(50)
version bigint
createdAt timestamp with time zone
updatedAt timestamp with time zone
```

Restaurant status enum:

```text
DRAFT
PENDING_APPROVAL
ACTIVE
INACTIVE
SUSPENDED
```

Use optimistic locking with:

```java
@Version
private Long version;
```

## RestaurantAddress

Fields:

```text
id UUID
restaurantId UUID
addressLine1
addressLine2
city
province
postalCode
country
latitude decimal(9,6)
longitude decimal(9,6)
createdAt
updatedAt
```

## Cuisine

Fields:

```text
id UUID
name varchar(100) unique
description
active boolean
```

Create a many-to-many relationship between Restaurant and Cuisine using a join table.

Avoid exposing JPA entities directly in APIs.

## BusinessHour

Fields:

```text
id UUID
restaurantId UUID
dayOfWeek enum
openTime time
closeTime time
closed boolean
```

Ensure one business-hours record per restaurant and day.

Support overnight hours if close time is earlier than open time.

## MenuCategory

Fields:

```text
id UUID
restaurantId UUID
name varchar(100)
description
displayOrder integer
active boolean
version bigint
createdAt
updatedAt
```

## MenuItem

Fields:

```text
id UUID
restaurantId UUID
categoryId UUID
name varchar(150)
description varchar(1000)
price decimal(10,2)
currency varchar(3)
imageUrl varchar(500)
available boolean
vegetarian boolean
vegan boolean
glutenFree boolean
spicyLevel integer
preparationMinutes integer
displayOrder integer
version bigint
createdAt
updatedAt
```

Validate spicy level between 0 and 5.

## DeliverySetting

Fields:

```text
id UUID
restaurantId UUID unique
deliveryRadiusKm decimal(5,2)
minimumOrderAmount decimal(10,2)
deliveryFee decimal(10,2)
freeDeliveryThreshold decimal(10,2)
estimatedDeliveryMinutes integer
deliveryEnabled boolean
pickupEnabled boolean
createdAt
updatedAt
```

---

# 5. Database design

Create Flyway migrations.

Create:

```text
V1__create_restaurant_schema.sql
V2__insert_default_cuisines.sql
```

Use PostgreSQL UUID columns.

Create appropriate foreign keys and indexes.

Required indexes:

```text
restaurant(owner_id)
restaurant(status)
restaurant(accepting_orders)
restaurant_address(city)
restaurant_address(postal_code)
restaurant_cuisine(restaurant_id, cuisine_id)
business_hour(restaurant_id, day_of_week)
menu_category(restaurant_id, active)
menu_item(restaurant_id, available)
menu_item(category_id, available)
menu_item(name)
```

Create unique constraints where appropriate.

Use snake_case database column names.

Use separate database schema:

```text
restaurant_service
```

Configure Hibernate with:

```text
ddl-auto: validate
```

Flyway must own database schema creation.

---

# 6. Public APIs

Implement REST endpoints under:

```text
/api/v1
```

## Search restaurants

```http
GET /api/v1/restaurants
```

Query parameters:

```text
city
postalCode
cuisine
name
acceptingOrders
latitude
longitude
radiusKm
page
size
sort
```

Return paginated restaurant summaries.

Use Spring Data JPA Specifications for filtering.

Default page size:

```text
20
```

Maximum page size:

```text
100
```

Support sorting by:

```text
name
averageRating
deliveryFee
estimatedPreparationMinutes
```

## Get restaurant

```http
GET /api/v1/restaurants/{restaurantId}
```

Return:

* Restaurant details
* Address
* Cuisines
* Business hours
* Delivery settings
* Current computed open or closed state

## Get restaurant menu

```http
GET /api/v1/restaurants/{restaurantId}/menu
```

Return only active categories and available items for customers.

Response should be grouped by menu category.

## Get menu item

```http
GET /api/v1/restaurants/{restaurantId}/menu-items/{menuItemId}
```

Return menu item details.

---

# 7. Restaurant-owner APIs

## Create restaurant

```http
POST /api/v1/restaurants
```

Allowed roles:

```text
RESTAURANT_OWNER
ADMIN
```

Example request:

```json
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
    "addressLine2": null,
    "city": "Toronto",
    "province": "ON",
    "postalCode": "M5X 1A9",
    "country": "CA",
    "latitude": 43.6487,
    "longitude": -79.3817
  },
  "cuisineIds": []
}
```

New restaurants should start with status:

```text
DRAFT
```

## Update restaurant

```http
PUT /api/v1/restaurants/{restaurantId}
```

Support complete update.

## Partial update

```http
PATCH /api/v1/restaurants/{restaurantId}
```

Support selected restaurant fields.

Do not allow owners to directly set:

```text
averageRating
totalRatings
ownerId
```

## Update restaurant status

```http
PATCH /api/v1/restaurants/{restaurantId}/status
```

Example:

```json
{
  "status": "ACTIVE"
}
```

Only Admin can approve or suspend restaurants.

Owners can move a restaurant from:

```text
DRAFT -> PENDING_APPROVAL
```

## Update accepting-orders status

```http
PATCH /api/v1/restaurants/{restaurantId}/accepting-orders
```

Request:

```json
{
  "acceptingOrders": true
}
```

A restaurant can accept orders only when its status is ACTIVE.

## Delete restaurant

```http
DELETE /api/v1/restaurants/{restaurantId}
```

Implement soft deletion or status transition to INACTIVE.

Do not physically delete production restaurant data.

---

# 8. Business-hours APIs

Implement:

```http
GET    /api/v1/restaurants/{restaurantId}/business-hours
PUT    /api/v1/restaurants/{restaurantId}/business-hours
```

Example request:

```json
{
  "businessHours": [
    {
      "dayOfWeek": "MONDAY",
      "openTime": "09:00",
      "closeTime": "22:00",
      "closed": false
    },
    {
      "dayOfWeek": "TUESDAY",
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

Implement a domain service:

```java
RestaurantAvailabilityService
```

Method:

```java
boolean isRestaurantOpen(
    Restaurant restaurant,
    List<BusinessHour> hours,
    ZonedDateTime currentTime
);
```

The calculation must use the restaurant timezone.

Restaurant order eligibility must require:

* Restaurant status is ACTIVE
* acceptingOrders is true
* Restaurant is currently open
* Delivery or pickup is enabled depending on order type

---

# 9. Menu-category APIs

Implement:

```http
POST   /api/v1/restaurants/{restaurantId}/menu-categories
GET    /api/v1/restaurants/{restaurantId}/menu-categories
PUT    /api/v1/restaurants/{restaurantId}/menu-categories/{categoryId}
PATCH  /api/v1/restaurants/{restaurantId}/menu-categories/{categoryId}/availability
DELETE /api/v1/restaurants/{restaurantId}/menu-categories/{categoryId}
```

Delete should deactivate the category rather than physically delete it.

When a category becomes inactive, customer menu responses must hide it and all its items.

---

# 10. Menu-item APIs

Implement:

```http
POST   /api/v1/restaurants/{restaurantId}/menu-items
GET    /api/v1/restaurants/{restaurantId}/menu-items
GET    /api/v1/restaurants/{restaurantId}/menu-items/{menuItemId}
PUT    /api/v1/restaurants/{restaurantId}/menu-items/{menuItemId}
PATCH  /api/v1/restaurants/{restaurantId}/menu-items/{menuItemId}/availability
DELETE /api/v1/restaurants/{restaurantId}/menu-items/{menuItemId}
```

Example create request:

```json
{
  "categoryId": "uuid",
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

Validation rules:

* Price must be greater than zero.
* Name must not be blank.
* Category must belong to the same restaurant.
* Currency must match the restaurant currency.
* Owner or staff must have access to the restaurant.
* Restaurant ID in URL must match the menu item restaurant ID.
* Do not allow cross-restaurant category assignment.

---

# 11. Internal order-validation API

Create an internal API used by Order Service:

```http
POST /api/v1/internal/restaurants/{restaurantId}/validate-order
```

Allowed role:

```text
SERVICE
```

Example request:

```json
{
  "orderType": "DELIVERY",
  "items": [
    {
      "menuItemId": "uuid",
      "quantity": 2,
      "expectedUnitPrice": 17.99
    },
    {
      "menuItemId": "uuid",
      "quantity": 1,
      "expectedUnitPrice": 5.99
    }
  ]
}
```

Response:

```json
{
  "restaurantId": "uuid",
  "valid": true,
  "restaurantOpen": true,
  "acceptingOrders": true,
  "currency": "CAD",
  "minimumOrderAmount": 15.00,
  "deliveryFee": 3.99,
  "estimatedPreparationMinutes": 25,
  "subtotal": 41.97,
  "validationErrors": [],
  "items": [
    {
      "menuItemId": "uuid",
      "name": "Butter Chicken",
      "requestedQuantity": 2,
      "available": true,
      "currentUnitPrice": 17.99,
      "priceChanged": false,
      "lineTotal": 35.98
    }
  ]
}
```

Validate:

* Restaurant exists.
* Restaurant is ACTIVE.
* Restaurant is accepting orders.
* Restaurant is currently open.
* Requested order type is enabled.
* Every menu item exists.
* Every menu item belongs to the restaurant.
* Every menu item is available.
* Every item category is active.
* Quantity is between 1 and 20.
* Current price matches expected price.
* Currency is consistent.
* Subtotal meets minimum-order requirement.

Do not trust the price supplied by Order Service.

Always calculate subtotal using current Restaurant Service prices.

Use BigDecimal for all monetary values.

Use explicit rounding rules where required.

Do not use `double` for monetary calculations.

Load all requested menu items in one database query to prevent N+1 queries.

Return validation errors instead of failing on the first invalid item.

Example validation errors:

```text
RESTAURANT_NOT_ACTIVE
RESTAURANT_CLOSED
RESTAURANT_NOT_ACCEPTING_ORDERS
DELIVERY_NOT_ENABLED
MENU_ITEM_NOT_FOUND
MENU_ITEM_UNAVAILABLE
MENU_CATEGORY_INACTIVE
PRICE_CHANGED
MINIMUM_ORDER_NOT_MET
INVALID_QUANTITY
```

---

# 12. Delivery settings APIs

Implement:

```http
GET /api/v1/restaurants/{restaurantId}/delivery-settings
PUT /api/v1/restaurants/{restaurantId}/delivery-settings
```

Example:

```json
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

Validation:

* At least one of deliveryEnabled or pickupEnabled must be true.
* Delivery radius must be positive when delivery is enabled.
* Monetary amounts cannot be negative.
* Free-delivery threshold must be greater than or equal to minimum-order amount.

---

# 13. Domain events

Publish events to Kafka.

Topic:

```text
restaurant-events
```

Use event envelope:

```json
{
  "eventId": "uuid",
  "eventType": "MENU_ITEM_UPDATED",
  "aggregateType": "RESTAURANT",
  "aggregateId": "uuid",
  "occurredAt": "2026-07-15T18:00:00Z",
  "version": 1,
  "correlationId": "uuid",
  "payload": {}
}
```

Publish these event types:

```text
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

Implement the transactional outbox pattern.

Create table:

```text
outbox_event
```

Fields:

```text
id UUID
aggregateType
aggregateId
eventType
payload JSONB
status
retryCount
createdAt
publishedAt
lastError
```

Use statuses:

```text
PENDING
PUBLISHED
FAILED
```

Application transaction must:

1. Update restaurant data.
2. Insert outbox event.
3. Commit both atomically.

Create an outbox publisher scheduled process that:

* Reads PENDING events in batches.
* Uses pessimistic locking or skip-locked.
* Publishes to Kafka.
* Marks events PUBLISHED.
* Retries temporary failures.
* Marks events FAILED after configurable maximum attempts.
* Logs event ID, aggregate ID and correlation ID.

Do not publish Kafka events directly inside the main database transaction.

---

# 14. Error handling

Implement a global exception handler using:

```java
@RestControllerAdvice
```

Create exceptions:

```text
RestaurantNotFoundException
MenuItemNotFoundException
MenuCategoryNotFoundException
AccessDeniedException
RestaurantNotAvailableException
DuplicateResourceException
InvalidOrderValidationException
OptimisticLockConflictException
BusinessRuleViolationException
```

Use a consistent error response:

```json
{
  "timestamp": "2026-07-15T18:00:00Z",
  "status": 404,
  "error": "NOT_FOUND",
  "code": "RESTAURANT_NOT_FOUND",
  "message": "Restaurant was not found",
  "path": "/api/v1/restaurants/uuid",
  "correlationId": "uuid",
  "validationErrors": []
}
```

Map validation failures to HTTP 400.

Map ownership and authorization failures to HTTP 403.

Map optimistic-lock failures to HTTP 409.

Map missing resources to HTTP 404.

Never expose stack traces or database details in API responses.

---

# 15. Idempotency

Support an `Idempotency-Key` header on:

```http
POST /api/v1/restaurants
POST /api/v1/restaurants/{restaurantId}/menu-items
POST /api/v1/restaurants/{restaurantId}/menu-categories
```

Store idempotency records in PostgreSQL.

Fields:

```text
idempotencyKey
userId
requestHash
responseStatus
responseBody
createdAt
expiresAt
```

When the same idempotency key and same request are received:

* Return the previous response.

When the same key is used with a different request:

* Return HTTP 409.

---

# 16. Caching

Use Spring Cache abstraction.

Cache:

```text
restaurant details
customer-facing restaurant menu
cuisine list
```

Use cache names:

```text
restaurant-details
restaurant-menu
cuisines
```

Provide a simple in-memory cache for local development.

Configure code so Redis can be enabled through configuration for deployed environments.

Evict relevant caches when:

* Restaurant is updated.
* Business hours change.
* Menu category changes.
* Menu item changes.
* Availability changes.
* Delivery settings change.

Do not cache internal order-validation responses.

---

# 17. Observability

Add:

* Spring Boot Actuator
* Micrometer metrics
* Structured JSON logging
* Correlation ID support
* Trace ID propagation
* Health checks
* Readiness checks
* Liveness checks

Expose:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
/actuator/prometheus
```

Create custom metrics:

```text
restaurant_created_total
restaurant_search_total
restaurant_menu_view_total
restaurant_order_validation_total
restaurant_order_validation_failure_total
menu_item_update_total
outbox_publish_success_total
outbox_publish_failure_total
```

Add timers for:

```text
restaurant search latency
menu retrieval latency
order validation latency
```

Never log JWTs, passwords or sensitive personal information.

Include the following fields in structured logs:

```text
timestamp
level
service
correlationId
traceId
userId
restaurantId
eventId
message
```

---

# 18. Configuration

Create configuration profiles:

```text
local
test
dev
prod
```

Use environment variables for:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
JWT_ISSUER_URI
REDIS_HOST
REDIS_PORT
AWS_REGION
```

Never hardcode secrets.

Create:

```text
application.yml
application-local.yml
application-test.yml
application-dev.yml
application-prod.yml
```

Local profile may use Docker Compose.

Production profile must be suitable for AWS ECS and AWS Secrets Manager-provided environment variables.

Configure graceful shutdown.

Configure HikariCP connection pool with reasonable defaults.

Configure Kafka producer idempotence and acknowledgements.

---

# 19. Testing

Create unit tests and integration tests.

## Unit tests

Test:

* Restaurant creation
* Owner authorization
* Restaurant status transitions
* Restaurant open-hours calculation
* Overnight business hours
* Menu item creation
* Menu item availability updates
* Category ownership validation
* Price calculation
* Order validation
* Minimum-order validation
* Delivery and pickup eligibility
* Cache eviction
* Outbox event creation

Use JUnit 5 and Mockito.

## Repository integration tests

Use PostgreSQL Testcontainers.

Test:

* Restaurant Specifications
* Case-insensitive restaurant search
* Cuisine filtering
* Menu-item batch retrieval
* Unique constraints
* Optimistic locking
* Flyway migrations

Do not use H2 for PostgreSQL integration tests.

## Controller integration tests

Use:

```text
MockMvc or WebTestClient
Spring Security test support
Testcontainers
```

Test:

* Authentication
* Authorization
* Validation
* Pagination
* Error responses
* Owner access restrictions
* Admin actions
* Internal SERVICE-role endpoints

## Kafka integration tests

Use Kafka Testcontainers or Embedded Kafka.

Test outbox publishing and event payload structure.

Aim for meaningful coverage rather than testing generated getters and setters.

---

# 20. API documentation

Configure OpenAPI.

Add:

```text
API descriptions
request examples
response examples
validation errors
security requirements
HTTP status codes
```

Swagger UI should be available in local and dev environments.

Disable or restrict Swagger UI in production.

Generate an OpenAPI document that can be imported into Postman.

---

# 21. Docker and local infrastructure

Create a multi-stage Dockerfile.

Build stage:

```text
Maven with Java 21
```

Runtime stage:

```text
Eclipse Temurin Java 21 JRE
```

Requirements:

* Run as a non-root user.
* Expose application port 8080.
* Use JVM container-aware memory settings.
* Include a health check where appropriate.
* Keep the final image small.
* Do not copy Maven credentials into the runtime image.

Create `docker-compose.yml` containing:

* PostgreSQL
* Kafka
* Kafka UI
* Redis
* Restaurant Service

Use persistent Docker volumes.

Add startup health checks.

---

# 22. CI workflow

Create a GitHub Actions workflow:

```text
.github/workflows/restaurant-service.yml
```

Trigger:

```text
push to main when restaurant-service/** changes
pull requests affecting restaurant-service/**
manual workflow dispatch
```

Pipeline steps:

1. Checkout repository.
2. Set up Java 21.
3. Cache Maven dependencies.
4. Run formatting or style checks.
5. Run unit tests.
6. Run integration tests.
7. Run Maven package.
8. Build Docker image.
9. Run vulnerability scan.
10. Authenticate with AWS using GitHub OIDC.
11. Log in to Amazon ECR.
12. Push image using commit SHA tag.
13. Render ECS task definition.
14. Deploy to ECS.
15. Wait for ECS service stability.

Use these environment variables:

```text
AWS_REGION=ca-central-1
ECR_REPOSITORY=restaurant-service
ECS_CLUSTER=food-delivery-dev-cluster
ECS_SERVICE=restaurant-service
CONTAINER_NAME=restaurant-service
TASK_DEFINITION_FILE=restaurant-service/ecs-task-definition.json
```

Do not use long-lived AWS access keys if GitHub OIDC is available.

---

# 23. ECS task definition

Create:

```text
ecs-task-definition.json
```

Configure:

* AWS Fargate
* Port 8080
* CloudWatch log driver
* CPU and memory suitable for a development environment
* Environment variables
* Secrets from AWS Secrets Manager
* Health check
* Execution role
* Task role
* Read-only root filesystem where practical
* Non-root user

Do not place plaintext database passwords in the task definition.

---

# 24. Code-quality rules

Follow these rules:

* Use Java records for immutable request and response DTOs where appropriate.
* Use BigDecimal for monetary values.
* Use UUID identifiers.
* Use OffsetDateTime or Instant for persisted timestamps.
* Use ZonedDateTime for timezone-based business logic.
* Use enums instead of arbitrary strings.
* Do not expose entities from controllers.
* Keep controllers thin.
* Put business rules in services or domain services.
* Mark transaction boundaries explicitly.
* Avoid bidirectional JPA relationships unless necessary.
* Avoid eager fetching of large collections.
* Prevent N+1 queries.
* Use projections for restaurant search result summaries.
* Use pagination for list endpoints.
* Use mapper components for entity-to-DTO conversion.
* Validate ownership in the service layer.
* Use database constraints in addition to application validation.
* Add JavaDoc only where behavior is not obvious.
* Avoid unnecessary abstractions.
* Do not create generic base repositories or generic base services.
* Avoid returning null collections.
* Do not catch generic Exception unless rethrowing with context.
* Add meaningful log statements but avoid excessive logging.

---

# 25. Required output order

Generate the implementation in phases.

## Phase 1

First generate:

1. `pom.xml`
2. Package structure
3. Main application class
4. Configuration files
5. JPA entities
6. Enums
7. Flyway migrations
8. Repositories
9. Basic Docker Compose

Ensure Phase 1 compiles.

## Phase 2

Generate:

1. Request DTOs
2. Response DTOs
3. Mappers
4. Services
5. Business-hours calculation
6. Ownership checks
7. Restaurant and menu controllers
8. Validation
9. Global exception handling

Ensure Phase 2 compiles and tests pass.

## Phase 3

Generate:

1. Internal order-validation endpoint
2. Batch menu-item loading
3. Price and subtotal calculation
4. Delivery-setting validation
5. Restaurant availability validation
6. Validation error collection

Add tests for the complete order-validation flow.

## Phase 4

Generate:

1. Kafka configuration
2. Outbox entity and repository
3. Event models
4. Outbox event creation
5. Scheduled publisher
6. Retry handling
7. Kafka integration tests

## Phase 5

Generate:

1. Spring Security configuration
2. JWT role mapping
3. Controller authorization
4. Owner and staff authorization
5. Security tests
6. OpenAPI configuration

## Phase 6

Generate:

1. Caching
2. Actuator
3. Metrics
4. Correlation ID filter
5. Structured logging
6. Dockerfile
7. ECS task definition
8. GitHub Actions workflow
9. README

After every phase:

* Run or logically verify `mvn clean verify`.
* Fix compile errors before continuing.
* Show the files created or changed.
* Explain important implementation decisions briefly.
* Do not leave placeholder methods.
* Do not use pseudocode.
* Do not generate incomplete classes.
* Do not skip tests for critical business rules.

---

# 26. README requirements

Create a complete README containing:

* Service responsibility
* Architecture overview
* Local setup
* Required environment variables
* Docker Compose instructions
* Database migration instructions
* API endpoint summary
* Swagger URL
* Kafka topics
* Event schemas
* Testing commands
* Maven commands
* Docker build commands
* ECS deployment overview
* Sample curl requests
* Known MVP limitations

Include this local startup sequence:

```bash
docker compose up -d postgres kafka redis
mvn clean verify
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Include sample curl commands for:

* Create restaurant
* Add business hours
* Add menu category
* Add menu item
* Activate restaurant
* Enable accepting orders
* Browse restaurants
* Retrieve menu
* Validate an order

---

# 27. MVP limitations

Document these accepted MVP limitations:

* No advanced geographic polygon delivery zones.
* Radius-based delivery validation can be added later.
* No full-text Elasticsearch search.
* No restaurant recommendation engine.
* No complex menu customization or add-ons initially.
* No multilingual menu support initially.
* No holiday-specific business hours initially.
* No image-upload service; image URL is stored.
* Ratings are read-only and will later be updated through events.
* Restaurant staff assignment can use a simple access table.

Design the code so these features can be added later without rewriting the core service.

Start with Phase 1 now. Generate complete files, not snippets, and make sure the project compiles before moving to the next phase.
