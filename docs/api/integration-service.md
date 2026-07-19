# integration-service

Orchestration edge for customers. Prefer `POST /api/place-order` as the place-order entrypoint.

## Endpoint index

| Method | Path | Handler | Auth hint |
|--------|------|---------|-----------|
| `POST` | `/api/place-order` | `PlaceOrderController.placeOrder` | — |

## Endpoints

### `POST /api/place-order`

- **Controller:** `PlaceOrderController.placeOrder`
- **Returns:** `PlaceOrderResponseDto`
- **Request body:** `PlaceOrderRequestDto`
- **Body fields (from DTO):** customerId:UUID, items:List<PlaceOrderItemDto>

```bash
curl -s -X POST "$BASE_URL/api/place-order" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

