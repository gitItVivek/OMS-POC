# fulfillment-service

Shipment creation and fulfillment lifecycle endpoints.

## Endpoint index

| Method | Path | Handler | Auth hint |
|--------|------|---------|-----------|
| `GET` | `/api/shipments/{orderId}` | `ShipmentController.getByOrderId` | — |
| `POST` | `/api/shipments/{orderId}/advance` | `ShipmentController.advance` | — |
| `POST` | `/internal/shipments/{orderId}/fulfill-to-delivered` | `ShipmentInternalController.fulfillToDelivered` | — |

## Endpoints

### `GET /api/shipments/{orderId}`

- **Controller:** `ShipmentController.getByOrderId`
- **Returns:** `ShipmentResponseDto`
- **Path params:** orderId:UUID

```bash
curl -s -X GET "$BASE_URL/api/shipments/{orderId}"
```

### `POST /api/shipments/{orderId}/advance`

- **Controller:** `ShipmentController.advance`
- **Returns:** `ShipmentResponseDto`
- **Path params:** orderId:UUID

```bash
curl -s -X POST "$BASE_URL/api/shipments/{orderId}/advance"
```

### `POST /internal/shipments/{orderId}/fulfill-to-delivered`

- **Controller:** `ShipmentInternalController.fulfillToDelivered`
- **Returns:** `ShipmentResponseDto`
- **Path params:** orderId:UUID

```bash
curl -s -X POST "$BASE_URL/internal/shipments/{orderId}/fulfill-to-delivered"
```

