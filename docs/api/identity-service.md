# identity-service

Owns JWT authentication and user identity. Other services call `/api/auth/me` to resolve the caller.

## Endpoint index

| Method | Path | Handler | Auth hint |
|--------|------|---------|-----------|
| `POST` | `/api/auth/login` | `AuthController.login` | — |
| `POST` | `/api/auth/logout` | `AuthController.logout` | yes |
| `GET` | `/api/auth/me` | `AuthController.currentUser` | yes |
| `POST` | `/api/auth/register` | `AuthController.register` | — |

## Endpoints

### `POST /api/auth/login`

- **Controller:** `AuthController.login`
- **Returns:** `ResponseEntity<AuthResponse>`
- **Request body:** `LoginEmailRequest`

```bash
curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### `POST /api/auth/logout`

- **Controller:** `AuthController.logout`
- **Returns:** `ResponseEntity<Void>`
- **Auth:** Bearer token / Spring `Authentication` expected

```bash
curl -s -X POST "$BASE_URL/api/auth/logout" \
  -H "Authorization: Bearer $TOKEN"
```

### `GET /api/auth/me`

- **Controller:** `AuthController.currentUser`
- **Returns:** `ResponseEntity<UserSummaryResponse>`
- **Auth:** Bearer token / Spring `Authentication` expected

```bash
curl -s -X GET "$BASE_URL/api/auth/me" \
  -H "Authorization: Bearer $TOKEN"
```

### `POST /api/auth/register`

- **Controller:** `AuthController.register`
- **Returns:** `ResponseEntity<AuthResponse>`
- **Request body:** `RegisterEmailRequest`

```bash
curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{}'
```

