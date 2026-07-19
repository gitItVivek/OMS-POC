# order-service

Order domain API: create/read orders, search, and dashboard analytics.

## Endpoint index

| Method | Path | Handler | Auth hint |
|--------|------|---------|-----------|
| `GET` | `/api/dashboard` | `OrderController.dashboard` | — |
| `GET` | `/api/search` | `OrderController.search` | — |
| `POST` | `/internal/orders` | `OrderInternalController.createOrder` | — |
| `POST` | `/internal/orders/{orderId}/cancel` | `OrderInternalController.cancelOrder` | — |
| `POST` | `/internal/orders/{orderId}/confirm` | `OrderInternalController.confirmOrder` | — |
| `POST` | `/orders` | `OrderController.createOrder` | — |
| `GET` | `/orders/{orderId}` | `OrderController.getOrder` | — |
| `GET` | `/users/{userId}/orders` | `OrderController.getOrdersForUser` | — |

## Endpoints

### `GET /api/dashboard`

- **Controller:** `OrderController.dashboard`
- **Returns:** `DashboardResponseDto`

```bash
curl -s -X GET "$BASE_URL/api/dashboard"
```

### `GET /api/search`

- **Controller:** `OrderController.search`
- **Returns:** `SearchResponseDto`

```bash
curl -s -X GET "$BASE_URL/api/search"
```

### `POST /internal/orders`

- **Controller:** `OrderInternalController.createOrder`
- **Returns:** `OrderResponseDto`
- **Request body:** `InternalCreateOrderRequestDto`
- **Body fields (from DTO):** customerId:UUID, items:List<OrderItemRequestDto>

```bash
curl -s -X POST "$BASE_URL/internal/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### `POST /internal/orders/{orderId}/cancel`

- **Controller:** `OrderInternalController.cancelOrder`
- **Returns:** `OrderResponseDto`
- **Path params:** orderId:UUID

```bash
curl -s -X POST "$BASE_URL/internal/orders/{orderId}/cancel" \
  -H "Authorization: Bearer $TOKEN"
```

### `POST /internal/orders/{orderId}/confirm`

- **Controller:** `OrderInternalController.confirmOrder`
- **Returns:** `OrderResponseDto`
- **Path params:** orderId:UUID

```bash
curl -s -X POST "$BASE_URL/internal/orders/{orderId}/confirm" \
  -H "Authorization: Bearer $TOKEN"
```

### `POST /orders`

- **Controller:** `OrderController.createOrder`
- **Returns:** `OrderResponseDto`
- **Request body:** `CreateOrderRequestDto`
- **Body fields (from DTO):** items:List<OrderItemRequestDto>

```bash
curl -s -X POST "$BASE_URL/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### `GET /orders/{orderId}`

- **Controller:** `OrderController.getOrder`
- **Returns:** `OrderResponseDto`
- **Path params:** orderId:UUID

```bash
curl -s -X GET "$BASE_URL/orders/{orderId}"
```

### `GET /users/{userId}/orders`

- **Controller:** `OrderController.getOrdersForUser`
- **Returns:** `List<OrderResponseDto>`
- **Path params:** userId:UUID

```bash
curl -s -X GET "$BASE_URL/users/{userId}/orders"
```

