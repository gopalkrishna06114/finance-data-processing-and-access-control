# Finance Data Processing and Access Control Backend

A production-grade RESTful backend system built with **Spring Boot 4.x** that handles financial data management with role-based access control (RBAC), JWT authentication, and dashboard analytics. This project was designed and built as a backend engineering assessment to demonstrate API design, data modeling, business logic, and access control implementation.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Architecture & Design Approach](#architecture--design-approach)
- [Folder Structure](#folder-structure)
- [Database Design](#database-design)
- [Security Implementation](#security-implementation)
- [Role-Based Access Control](#role-based-access-control)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Testing the API](#testing-the-api)
- [Assumptions Made](#assumptions-made)
- [Author](#author)

---

## Project Overview

This is the backend for a **Finance Dashboard System** where different users interact with financial records based on their assigned role. The system supports:

- User registration and JWT-based authentication
- Role-based access control with three roles: `ADMIN`, `ANALYST`, `VIEWER`
- Full CRUD operations on financial records (income and expense tracking)
- Dashboard summary APIs for analytics (totals, trends, category breakdowns)
- Soft delete for financial records
- Pagination and filtering on record listing
- Input validation and structured error handling
- Swagger UI for interactive API documentation

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 25 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security 7 + JWT (jjwt 0.13.0) |
| Database | MySQL 8 |
| ORM | Spring Data JPA + Hibernate 7 |
| Validation | Spring Boot Validation (Jakarta) |
| API Docs | Springdoc OpenAPI 2.3.0 (Swagger UI) |
| Build Tool | Maven |
| Utilities | Lombok |

---

## Architecture & Design Approach

### Layered Architecture

The project follows a clean **layered architecture** pattern which separates concerns clearly:

```
Request → Controller → Service → Repository → Database
                ↕
            DTOs (Request/Response)
                ↕
         Security (JWT Filter)
                ↕
      Exception Handler (Global)
```

Every layer has a single responsibility:

- **Controller** — handles HTTP requests and responses only, no business logic
- **Service** — contains all business logic and data transformation
- **Repository** — handles all database operations via JPA
- **Entity** — represents the database tables
- **DTO** — carries data between layers without exposing entities directly
- **Security** — intercepts every request and validates JWT before it reaches the controller
- **Exception Handler** — catches all exceptions globally and returns clean structured responses

### Design Decisions

**Why DTOs instead of exposing Entities directly?**
Entities contain sensitive fields like `password` and internal audit fields. DTOs let us control exactly what data is sent to the client.

**Why Soft Delete?**
Financial records should never be permanently deleted for audit trail purposes. The `deleted` boolean flag hides records from all queries while keeping them in the database.

**Why STATELESS sessions?**
JWT is self-contained — the server does not need to store session state. Every request carries the token and the server simply validates it. This makes the API scalable and stateless.

**Why `@EnableMethodSecurity` with `@PreAuthorize`?**
Instead of configuring role restrictions in `SecurityConfig` (which becomes hard to maintain), each endpoint declares its own access rule directly using `@PreAuthorize`. This makes access control visible and self-documenting.

---

## Folder Structure

```
src/main/java/com/example/finance_data_processing_and_access_control/
│
├── config/                          # Application configuration
│   ├── SecurityConfig.java          # Spring Security + JWT filter chain setup
│   ├── OpenApiConfig.java           # Swagger UI configuration
│
├── controller/                      # REST API layer — handles HTTP only
│   ├── AuthController.java          # /api/auth/** — register, login
│   ├── UserController.java          # /api/users/** — user management
│   ├── FinancialRecordController.java # /api/records/** — CRUD + filters
│   └── DashboardController.java     # /api/dashboard/** — analytics
│
├── dto/
│   ├── request/                     # Incoming request bodies
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── UpdateUserRequest.java
│   │   └── FinancialRecordRequest.java
│   └── response/                    # Outgoing response bodies
│       ├── ApiResponse.java         # Generic wrapper for all responses
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── FinancialRecordResponse.java
│       └── DashboardSummaryResponse.java
│
├── entity/                          # JPA entities — mapped to DB tables
│   ├── User.java                    # users table
│   └── FinancialRecord.java         # financial_records table
│
├── enums/                           # Java enums for type safety
│   ├── Role.java                    # VIEWER, ANALYST, ADMIN
│   └── TransactionType.java         # INCOME, EXPENSE
│
├── exception/                       # Error handling
│   ├── GlobalExceptionHandler.java  # Catches all exceptions app-wide
│   ├── ResourceNotFoundException.java
│   ├── AccessDeniedException.java
│   └── ErrorResponse.java           # Structured error response body
│
├── repository/                      # Data access layer — JPA interfaces
│   ├── UserRepository.java
│   └── FinancialRecordRepository.java
│
├── security/                        # JWT + Spring Security components
│   ├── JwtUtils.java                # Token generation and validation
│   ├── JwtAuthenticationFilter.java # Intercepts every request
│   └── CustomUserDetailsService.java # Loads user from DB for Spring Security
│
├── service/                         # Business logic layer
│   ├── AuthService.java             # Register + login logic
│   ├── UserService.java             # User management logic
│   ├── FinancialRecordService.java  # Record CRUD + soft delete
│   └── DashboardService.java        # Aggregations + analytics
│
└── FinanceDataProcessingAndAccessControlApplication.java
```

---

## Database Design

The database uses **2 tables** — intentionally minimal to keep queries simple and efficient.

### `users` table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-incremented primary key |
| name | VARCHAR(100) | Full name of the user |
| email | VARCHAR(100) | Unique email — used for login |
| password | VARCHAR | BCrypt hashed password |
| role | ENUM | VIEWER, ANALYST, or ADMIN |
| active | BOOLEAN | Account status — default true |
| created_at | DATETIME | Auto-set on creation |
| updated_at | DATETIME | Auto-updated on every save |

### `financial_records` table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-incremented primary key |
| amount | DECIMAL(15,2) | Transaction amount |
| type | ENUM | INCOME or EXPENSE |
| category | VARCHAR(100) | Category label (e.g. Salary, Rent) |
| date | DATE | Date of the transaction |
| notes | VARCHAR(500) | Optional description |
| deleted | BOOLEAN | Soft delete flag — default false |
| created_by | BIGINT (FK) | References users.id |
| created_at | DATETIME | Auto-set on creation |
| updated_at | DATETIME | Auto-updated on every save |

### Relationships

```
users (1) ──────────── (many) financial_records
       └── created_by FK
```

One user can create many financial records. Each record tracks who created it.

### Why only 2 tables?

- Roles are stored as an enum column on the `users` table — no separate roles/permissions tables needed because role behavior is enforced entirely in code via `@PreAuthorize`
- Categories are plain `VARCHAR` on records instead of a separate `categories` table — keeps filtering simple and allows flexible category naming
- No separate audit log table — `created_at`, `updated_at`, and `created_by` on each record cover the audit trail

---

## Security Implementation

Security is implemented in multiple layers:

### Layer 1 — JWT Token Generation (`JwtUtils.java`)

When a user logs in successfully, a JWT token is generated containing:
- **Subject** — the user's email
- **Issued At** — current timestamp
- **Expiration** — 24 hours from issue
- **Signature** — HMAC-SHA256 signed with a secret key from `application.properties`

### Layer 2 — JWT Filter (`JwtAuthenticationFilter.java`)

Every incoming request passes through this filter **before** reaching any controller:

```
Request arrives
     ↓
Extract "Authorization" header
     ↓
Does it start with "Bearer "?
     ↓ Yes
Extract token → extract email from token
     ↓
Load user from database
     ↓
Is token valid and not expired?
     ↓ Yes
Set authentication in SecurityContext
     ↓
Request proceeds to controller
```

If the token is missing, invalid, or expired — the request is rejected with `401 Unauthorized`.

### Layer 3 — Method-Level Security (`@PreAuthorize`)

Each controller endpoint declares its own access rule:

```java
@PreAuthorize("hasRole('ADMIN')")                          // ADMIN only
@PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")    // ADMIN + ANALYST
@PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')") // ALL roles
```

If a user with insufficient role tries to access an endpoint — they get `403 Forbidden`.

### Layer 4 — Password Hashing

All passwords are hashed using **BCrypt** before storing in the database. Plain text passwords are never stored.

### Layer 5 — Exception Handling for Security

`GlobalExceptionHandler` catches:
- `AuthorizationDeniedException` → `403 Forbidden`
- `AccessDeniedException` → `403 Forbidden`
- `BadCredentialsException` → `401 Unauthorized`

---

## Role-Based Access Control

Three roles are defined with different levels of access:

### VIEWER
- Can **read** financial records
- Can view the **dashboard summary**, net balance, total income/expenses, recent activity
- **Cannot** create, update, or delete records
- **Cannot** access category totals or monthly trends
- **Cannot** manage users

### ANALYST
- Everything a VIEWER can do
- Can **create** and **update** financial records
- Can access **category totals** and **monthly trends**
- **Cannot** delete records
- **Cannot** manage users

### ADMIN
- Full access to everything
- Can create, update, and **delete** financial records
- Can manage users (view, update role, activate/deactivate, delete)
- Access to all dashboard and analytics endpoints

### Access Control Matrix

| Endpoint | VIEWER | ANALYST | ADMIN |
|----------|--------|---------|-------|
| POST `/api/auth/register` | ✅ | ✅ | ✅ |
| POST `/api/auth/login` | ✅ | ✅ | ✅ |
| GET `/api/records` | ✅ | ✅ | ✅ |
| GET `/api/records/{id}` | ✅ | ✅ | ✅ |
| POST `/api/records` | ❌ | ✅ | ✅ |
| PUT `/api/records/{id}` | ❌ | ✅ | ✅ |
| DELETE `/api/records/{id}` | ❌ | ❌ | ✅ |
| GET `/api/dashboard/summary` | ✅ | ✅ | ✅ |
| GET `/api/dashboard/total-income` | ✅ | ✅ | ✅ |
| GET `/api/dashboard/total-expenses` | ✅ | ✅ | ✅ |
| GET `/api/dashboard/net-balance` | ✅ | ✅ | ✅ |
| GET `/api/dashboard/recent-activity` | ✅ | ✅ | ✅ |
| GET `/api/dashboard/category-totals` | ❌ | ✅ | ✅ |
| GET `/api/dashboard/monthly-trend` | ❌ | ✅ | ✅ |
| GET `/api/users` | ❌ | ❌ | ✅ |
| GET `/api/users/{id}` | ❌ | ❌ | ✅ |
| PUT `/api/users/{id}` | ❌ | ❌ | ✅ |
| DELETE `/api/users/{id}` | ❌ | ❌ | ✅ |
| PATCH `/api/users/{id}/activate` | ❌ | ❌ | ✅ |
| PATCH `/api/users/{id}/deactivate` | ❌ | ❌ | ✅ |

---

## API Endpoints

### Authentication — `/api/auth`

#### POST `/api/auth/register`
Register a new user. No token required.

**Request Body:**
```json
{
    "name": "Gopal Krishna",
    "email": "gopal@finance.com",
    "password": "gopal123",
    "role": "ADMIN"
}
```

**Response:**
```json
{
    "success": true,
    "message": "User registered successfully",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "userId": 1,
        "name": "Gopal Krishna",
        "email": "gopal@finance.com",
        "role": "ADMIN"
    }
}
```

#### POST `/api/auth/login`
Login with email and password.

**Request Body:**
```json
{
    "email": "gopal@finance.com",
    "password": "gopal123"
}
```

---

### Financial Records — `/api/records`

#### POST `/api/records` — ADMIN, ANALYST
**Request Body:**
```json
{
    "amount": 5000.00,
    "type": "INCOME",
    "category": "Salary",
    "date": "2026-04-03",
    "notes": "Monthly salary"
}
```

#### GET `/api/records` — ALL roles
Supports filters and pagination via query parameters:

| Parameter | Type | Example |
|-----------|------|---------|
| type | INCOME / EXPENSE | `?type=INCOME` |
| category | String | `?category=Salary` |
| startDate | YYYY-MM-DD | `?startDate=2026-01-01` |
| endDate | YYYY-MM-DD | `?endDate=2026-04-03` |
| page | Integer | `?page=0` |
| size | Integer | `?size=10` |

#### GET `/api/records/{id}` — ALL roles
#### PUT `/api/records/{id}` — ADMIN, ANALYST
#### DELETE `/api/records/{id}` — ADMIN only (soft delete)

---

### Dashboard — `/api/dashboard`

| Endpoint | Access | Description |
|----------|--------|-------------|
| GET `/api/dashboard/summary` | ALL | Full summary with all analytics |
| GET `/api/dashboard/total-income` | ALL | Total income amount |
| GET `/api/dashboard/total-expenses` | ALL | Total expenses amount |
| GET `/api/dashboard/net-balance` | ALL | Net balance (income - expenses) |
| GET `/api/dashboard/recent-activity` | ALL | Latest transactions `?limit=10` |
| GET `/api/dashboard/category-totals` | ADMIN, ANALYST | Totals grouped by category |
| GET `/api/dashboard/monthly-trend` | ADMIN, ANALYST | Monthly breakdown `?months=6` |

---

### User Management — `/api/users` (ADMIN only)

| Endpoint | Description |
|----------|-------------|
| GET `/api/users` | Get all users |
| GET `/api/users/{id}` | Get user by ID |
| PUT `/api/users/{id}` | Update name, role, or status |
| DELETE `/api/users/{id}` | Delete user |
| PATCH `/api/users/{id}/activate` | Activate user account |
| PATCH `/api/users/{id}/deactivate` | Deactivate user account |

---

## Getting Started

### Prerequisites

- Java 25
- Maven
- MySQL 8.x
- Git

### Step 1 — Clone the repository

```bash
git clone https://github.com/your_username/finance-data-processing-and-access-control.git
cd finance-data-processing-and-access-control
```

### Step 2 — Configure the database

Create a MySQL database (or let the app create it automatically):

```sql
CREATE DATABASE finance_db;
```

Copy the example properties file and fill in your values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/finance_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# JWT
app.jwt.secret=your_base64_encoded_secret_key
app.jwt.expiration-ms=86400000

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Step 3 — Generate a JWT Secret Key

The JWT secret must be a Base64 encoded string. You can generate one using:

```bash
echo -n "your-super-secret-key-minimum-32-characters" | base64
```

Paste the output as the value of `app.jwt.secret`.

### Step 4 — Run the application

```bash
mvn spring-boot:run
```

Or run directly from IntelliJ by clicking the green **Run** button.

The application will start on `http://localhost:8080`

### Step 5 — Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### Step 6 — Register your first user

```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "name": "Admin User",
    "email": "admin@finance.com",
    "password": "admin123",
    "role": "ADMIN"
}
```

### Step 7 — Login and get token

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "admin@finance.com",
    "password": "admin123"
}
```

Copy the token from the response and use it as `Bearer <token>` in the `Authorization` header for all protected endpoints.

---

## Testing the API

### Using Swagger UI

1. Open `http://localhost:8080/swagger-ui.html`
2. Use `POST /api/auth/login` to get a token
3. Click the **Authorize** 🔒 button at the top
4. Enter `Bearer your_token_here`
5. All endpoints are now unlocked for testing

### Using Postman

1. Create a new collection called `Finance API`
2. Set collection-level Authorization to `Bearer Token` with value `{{token}}`
3. In the Login request **Tests** tab add:
```javascript
const response = pm.response.json();
pm.collectionVariables.set("token", response.data.token);
```
4. Now the token is automatically saved and used for all requests

### Testing RBAC

Register three users with different roles and test access:

```json
{ "email": "admin@test.com",   "password": "pass123", "role": "ADMIN"   }
{ "email": "analyst@test.com", "password": "pass123", "role": "ANALYST" }
{ "email": "viewer@test.com",  "password": "pass123", "role": "VIEWER"  }
```

- Login as VIEWER → try `POST /api/records` → should get `403 Forbidden`
- Login as ANALYST → try `DELETE /api/records/1` → should get `403 Forbidden`
- Login as ANALYST → try `GET /api/users` → should get `403 Forbidden`
- Login as ADMIN → all endpoints should return `200 OK`

---

## Assumptions Made

1. **Registration is public** — Any user can register. The `role` field is accepted at registration time. In a real production system, only admins would be able to assign roles.

2. **Categories are free-form strings** — No predefined list of categories. Users can enter any category name they want (Salary, Rent, Food, etc.).

3. **Soft delete only for records** — Financial records use soft delete (hidden but not removed). Users are hard deleted when removed by an admin.

4. **Single token per login** — No refresh token mechanism. Tokens expire after 24 hours and the user must log in again.

5. **No pagination on users** — User listing returns all users at once. Financial records use pagination since they can grow large.

6. **Created by tracking** — Every financial record tracks which user created it using the `created_by` foreign key linked to the authenticated user from the JWT token.

---

## Author

**Gopal Krishna**

- Project: Finance Data Processing and Access Control Backend
- Stack: Java 25 + Spring Boot 4.0.5 + MySQL + JWT
- Built as a backend engineering assessment

---

## License

This project is built for assessment purposes.
