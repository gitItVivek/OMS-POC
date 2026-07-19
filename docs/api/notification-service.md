# notification-service

Notification dispatch internals for order lifecycle events.

## Endpoint index

| Method | Path | Handler | Auth hint |
|--------|------|---------|-----------|
| `POST` | `/internal/notifications` | `NotificationInternalController.send` | — |

## Endpoints

### `POST /internal/notifications`

- **Controller:** `NotificationInternalController.send`
- **Returns:** `void`
- **Request body:** `SendNotificationRequestDto`
- **Body fields (from DTO):** orderId:UUID, message:String

```bash
curl -s -X POST "$BASE_URL/internal/notifications" \
  -H "Content-Type: application/json" \
  -d '{}'
```

