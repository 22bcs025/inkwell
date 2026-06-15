# Inkwell API Documentation

## Base URL

```
http://localhost:8080/api
```

## Authentication

All protected endpoints require:

```
Authorization: Bearer <jwt_token>
```

Tokens are returned on login and registration and expire after 24 hours (configurable via
`JWT_EXPIRATION_MS`). Unauthenticated requests to protected endpoints return `401`. Requests
to another user's resource return `403`.

---

## Auth Endpoints

### POST /auth/register

Register a new user.

Request:
```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "password123"
}
```

Response `201 Created`:
```json
{
  "token": "eyJhbGci...",
  "userId": 1,
  "username": "alice"
}
```

Errors: `409 Conflict` (duplicate username/email), `400 Bad Request` (validation)

---

### POST /auth/login

Request:
```json
{
  "username": "alice",
  "password": "password123"
}
```

Response `200 OK`:
```json
{
  "token": "eyJhbGci...",
  "userId": 1,
  "username": "alice"
}
```

Errors: `401 Unauthorized` (bad credentials)

---

## Blog Endpoints

### GET /blogs

List all blogs, paginated. Public.

| Query Param | Default        | Description                     |
|-------------|----------------|---------------------------------|
| page        | 0              | Page number (zero-indexed)      |
| size        | 10             | Results per page                |
| sort        | createdAt,desc | Sort field and direction        |
| q           | (none)         | Full-text search term           |

Response `200 OK`:
```json
{
  "content": [
    {
      "id": 1,
      "title": "Hello World",
      "content": "# Hello\n\nThis is my first post.",
      "authorId": 1,
      "authorUsername": "alice",
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z",
      "commentCount": 3,
      "views": 42,
      "likes": 7
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

### GET /blogs/top-weekly

Blogs ranked by engagement score (`views + likes * 3`). Public.

Query params: `page`, `size`

Response: same shape as `GET /blogs`

---

### GET /blogs/{id}

Get a single blog. Increments view count on each call. Public.

Response `200 OK`: single `BlogResponse` (same fields as above)

Errors: `404 Not Found`

---

### POST /blogs

Create a blog post. **Requires auth.**

Request:
```json
{
  "title": "My Post",
  "content": "# Heading\n\nSome content."
}
```

Response `201 Created`: `BlogResponse`

Errors: `400 Bad Request` (validation), `401 Unauthorized`

---

### PUT /blogs/{id}

Update a blog post. **Requires auth. Owner only.**

Request: same shape as POST

Response `200 OK`: `BlogResponse`

Errors: `403 Forbidden`, `404 Not Found`

---

### DELETE /blogs/{id}

Delete a blog post. **Requires auth. Owner only.**

Response `204 No Content`

Errors: `403 Forbidden`, `404 Not Found`

---

### POST /blogs/{id}/like

Like a post. **Requires auth.**

Response `200 OK`: updated `BlogResponse`

Errors: `409 Conflict` (already liked), `404 Not Found`

---

### DELETE /blogs/{id}/like

Unlike a post. **Requires auth.**

Response `200 OK`: updated `BlogResponse`

Errors: `404 Not Found` (not previously liked)

---

### GET /blogs/{id}/like

Check like status for the current user. **Requires auth.**

Response `200 OK`:
```json
{ "liked": true }
```

---

## Comment Endpoints

### GET /blogs/{blogId}/comments

List comments for a blog, paginated. Public.

| Query Param | Default    |
|-------------|------------|
| page        | 0          |
| size        | 20         |

Response `200 OK`:
```json
{
  "content": [
    {
      "id": 1,
      "content": "Great post!",
      "blogId": 1,
      "authorId": 2,
      "authorUsername": "bob",
      "createdAt": "2024-01-02T08:00:00Z",
      "updatedAt": "2024-01-02T08:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

### POST /blogs/{blogId}/comments

Add a comment. **Requires auth.**

Request:
```json
{ "content": "Great post!" }
```

Response `201 Created`: `CommentResponse`

Errors: `404 Not Found` (blog not found)

---

### PUT /comments/{id}

Edit a comment. **Requires auth. Owner only.**

Request:
```json
{ "content": "Updated comment." }
```

Response `200 OK`: `CommentResponse`

Errors: `403 Forbidden`, `404 Not Found`

---

### DELETE /comments/{id}

Delete a comment. **Requires auth. Owner only.**

Response `204 No Content`

Errors: `403 Forbidden`, `404 Not Found`

---

## User Endpoints

### GET /users/{id}

Get a user profile. Public.

Response `200 OK`:
```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "createdAt": "2024-01-01T09:00:00Z"
}
```

Errors: `404 Not Found`

---

### PUT /users/{id}

Update account. **Requires auth. Owner only.** All fields optional.

Request:
```json
{
  "username": "alice2",
  "email": "alice2@example.com",
  "password": "newpassword"
}
```

Response `200 OK`: `UserResponse`

Errors: `403 Forbidden`, `409 Conflict` (duplicate username/email)

---

### DELETE /users/{id}

Delete account. **Requires auth. Owner only.** Cascades to all posts and comments.

Response `204 No Content`

Errors: `403 Forbidden`, `404 Not Found`

---

## Error Response Shape

All errors:
```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 404,
  "error": "Blog not found: 99"
}
```

Validation errors add a `fields` map:
```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 400,
  "error": "Validation failed",
  "fields": {
    "username": "Username must be between 3 and 50 characters",
    "email": "Email must be valid"
  }
}
```

## Endpoint Summary

| Method | Path                        | Auth | Description              |
|--------|-----------------------------|------|--------------------------|
| POST   | /auth/register              | No   | Register                 |
| POST   | /auth/login                 | No   | Login                    |
| GET    | /blogs                      | No   | List / search blogs      |
| GET    | /blogs/top-weekly           | No   | Top blogs by engagement  |
| GET    | /blogs/{id}                 | No   | Get blog                 |
| POST   | /blogs                      | Yes  | Create blog              |
| PUT    | /blogs/{id}                 | Yes  | Update blog              |
| DELETE | /blogs/{id}                 | Yes  | Delete blog              |
| GET    | /blogs/{id}/like            | Yes  | Like status              |
| POST   | /blogs/{id}/like            | Yes  | Like blog                |
| DELETE | /blogs/{id}/like            | Yes  | Unlike blog              |
| GET    | /blogs/{blogId}/comments    | No   | List comments            |
| POST   | /blogs/{blogId}/comments    | Yes  | Add comment              |
| PUT    | /comments/{id}              | Yes  | Edit comment             |
| DELETE | /comments/{id}              | Yes  | Delete comment           |
| GET    | /users/{id}                 | No   | Get user                 |
| PUT    | /users/{id}                 | Yes  | Update user              |
| DELETE | /users/{id}                 | Yes  | Delete user              |
