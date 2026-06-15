# Inkwell

Inkwell is a simple Blogging site built with Java SprintBoot and Angular.

---

## Technology Stack

| Layer      | Technology                                      |
|------------|-------------------------------------------------|
| Frontend   | Angular 17, Tailwind CSS, marked (Markdown)     |
| Backend    | Java 21, Spring Boot 3.3, Spring Security (JWT) |
| Database   | PostgreSQL 16 (Flyway migrations)               |
| Auth       | JWT (jjwt 0.12), BCrypt password hashing        |
| Build      | Maven 3.9, Angular CLI 17, npm                  |
| Container  | Docker, Docker Compose                          |
| Web Server | Nginx (frontend), embedded Tomcat (backend)     |
| Testing    | JUnit 5, Mockito, Spring Security Test, H2      |

---

## API Documentation

See [API.md](API.md) for the full reference including request/response examples and the
endpoint summary table.

## Database Schema

```sql
users
  id            BIGSERIAL PK
  username      VARCHAR(50) UNIQUE NOT NULL
  email         VARCHAR(255) UNIQUE NOT NULL
  password_hash VARCHAR(255) NOT NULL
  created_at    TIMESTAMP NOT NULL
  updated_at    TIMESTAMP NOT NULL

blogs
  id             BIGSERIAL PK
  title          VARCHAR(255) NOT NULL
  content        TEXT NOT NULL
  author_id      BIGINT FK -> users(id) ON DELETE CASCADE
  views          BIGINT DEFAULT 0
  likes          BIGINT DEFAULT 0
  search_vector  TSVECTOR GENERATED (title A, content B)
  created_at     TIMESTAMP NOT NULL
  updated_at     TIMESTAMP NOT NULL

comments
  id          BIGSERIAL PK
  content     TEXT NOT NULL
  blog_id     BIGINT FK -> blogs(id) ON DELETE CASCADE
  author_id   BIGINT FK -> users(id) ON DELETE CASCADE
  created_at  TIMESTAMP NOT NULL
  updated_at  TIMESTAMP NOT NULL

blog_likes
  blog_id     BIGINT FK -> blogs(id) ON DELETE CASCADE
  user_id     BIGINT FK -> users(id) ON DELETE CASCADE
  created_at  TIMESTAMP NOT NULL
  PRIMARY KEY (blog_id, user_id)
```

Indexes: `idx_blogs_author_id`, `idx_blogs_created_at`, `idx_comments_blog_id`,
`idx_comments_author_id`, `idx_blogs_search_vector` (GIN)

---

### Running Tests

```bash
cd backend
mvn test
```

---

## Deployment Details

### Prerequisites

- Docker and Docker Compose installed
- Ports 4200, 8080, 5433 available

### Development Deployment (hot reload)

```bash
docker-compose -f docker-compose.dev.yml up
```

- Backend: Spring Boot DevTools with Maven live reload
- Frontend: Angular dev server with proxy to backend
- Database: same PostgreSQL container

### Environment Variables

| Variable            | Default                           | Description          |
|---------------------|-----------------------------------|----------------------|
| DB_URL              | jdbc:postgresql://db:5432/inkwell | PostgreSQL JDBC URL  |
| DB_USERNAME         | inkwell                           | Database user        |
| DB_PASSWORD         | inkwell                           | Database password    |
| JWT_SECRET          | dev-secret-key-not-for-production-must-be-at-least-256-bits-long-ok | HS256 signing key    |
| JWT_EXPIRATION_MS   | 86400000                          | Token TTL (ms)       |
| CORS_ALLOWED_ORIGINS| http://localhost:4200             | Allowed CORS origins |
| SERVER_PORT         | 8080                              | Backend port         |

Change the `JWT_SECRET`.

### Docker Images

```
app  -- maven:3.9-eclipse-temurin-21 (build) -> eclipse-temurin:21-jre-jammy (run)
web  -- node:20-alpine (build) -> nginx:alpine (run)
db   -- postgres:16-alpine
```
