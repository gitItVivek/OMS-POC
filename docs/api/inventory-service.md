# inventory-service

Product catalog and stock reservation internals used during order placement.

## Endpoint index

| Method | Path | Handler | Auth hint |
|--------|------|---------|-----------|
| `GET` | `/api/products/by-category` | `ProductController.getProductsByCategory` | — |
| `GET` | `/api/products/by-ids` | `ProductController.getProductsByIds` | — |
| `GET` | `/api/products/search` | `ProductController.searchProducts` | — |
| `GET` | `/api/products/{id}` | `ProductController.getProductById` | — |
| `POST` | `/internal/stock/release` | `StockInternalController.release` | — |
| `POST` | `/internal/stock/reserve` | `StockInternalController.reserve` | — |

## Endpoints

### `GET /api/products/by-category`

- **Controller:** `ProductController.getProductsByCategory`
- **Returns:** `ProductPageResponseDto`

```bash
curl -s -X GET "$BASE_URL/api/products/by-category"
```

### `GET /api/products/by-ids`

- **Controller:** `ProductController.getProductsByIds`
- **Returns:** `List<ProductSearchResultDto>`

```bash
curl -s -X GET "$BASE_URL/api/products/by-ids"
```

### `GET /api/products/search`

- **Controller:** `ProductController.searchProducts`
- **Returns:** `ProductPageResponseDto`

```bash
curl -s -X GET "$BASE_URL/api/products/search"
```

### `GET /api/products/{id}`

- **Controller:** `ProductController.getProductById`
- **Returns:** `ProductSearchResultDto`
- **Path params:** id:UUID

```bash
curl -s -X GET "$BASE_URL/api/products/{id}"
```

### `POST /internal/stock/release`

- **Controller:** `StockInternalController.release`
- **Returns:** `void`
- **Request body:** `ReleaseStockCommandDto`
- **Body fields (from DTO):** orderId:UUID

```bash
curl -s -X POST "$BASE_URL/internal/stock/release" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### `POST /internal/stock/reserve`

- **Controller:** `StockInternalController.reserve`
- **Returns:** `StockReservationResultDto`
- **Request body:** `ReserveStockCommandDto`
- **Body fields (from DTO):** orderId:UUID, items:List<ReserveStockItemDto>

```bash
curl -s -X POST "$BASE_URL/internal/stock/reserve" \
  -H "Content-Type: application/json" \
  -d '{}'
```

