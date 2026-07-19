# OMS-POC API Documentation

Auto-generated from Spring `@RestController` sources by `docs-agent-service`.

## Quick start

1. **Authenticate** with identity-service (`POST /api/auth/login`) and keep the Bearer token as `TOKEN`.
2. **Place an order** through integration-service (`POST /api/place-order`) — the customer entrypoint.
3. Replace `BASE_URL` with the service host (local defaults vary by service port).

```bash
# Login
curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"secret"}'

# Place order (integration-service)
curl -s -X POST "$BASE_URL/api/place-order" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":"PRODUCT_UUID","quantity":1}]}'
```

## Services

| Service | Endpoints | Doc |
|---------|-----------|-----|
| `identity-service` | 4 | [identity-service.md](identity-service.md) |
| `order-service` | 8 | [order-service.md](order-service.md) |
| `inventory-service` | 6 | [inventory-service.md](inventory-service.md) |
| `fulfillment-service` | 3 | [fulfillment-service.md](fulfillment-service.md) |
| `notification-service` | 1 | [notification-service.md](notification-service.md) |
| `integration-service` | 1 | [integration-service.md](integration-service.md) |

> Endpoint lists are deterministic (controller scan). Prose may be LLM-enhanced when OpenAI is available.
