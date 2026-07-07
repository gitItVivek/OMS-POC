# OMS-POC
Proof of Concept demonstrating enterprise integration patterns using Spring Integration and Apache Camel in an Order Management System (OMS) microservices architecture.

## Benchmarking — Spring Integration (Order Saga)

Full methodology, test plan structure, and troubleshooting notes: [`integration-service/loadtest/LOAD_TESTING.md`](integration-service/loadtest/LOAD_TESTING.md).

Load test of the order saga on `integration-service`, which is orchestrated via Spring Integration Kafka message-driven flows (`OrderSagaIntegrationFlows`) instead of the previous Apache Camel routes. The saga fans out across `order-service` → `inventory-service` → `fulfillment-service` → `notification-service` over several Kafka round trips, so two things are measured separately:

1. **Ingress accept latency** — `POST /api/place-order`: validate request, write a `SagaInstance` row, publish one Kafka command, return `202`. This is synchronous HTTP work only; the Spring Integration flows haven't run yet when this returns.
2. **End-to-end saga completion** — the full async chain, measured by placing an order and polling the new `GET /api/saga-status/{orderId}` endpoint (`SagaOrchestratorServiceImpl.getSagaStatus`) until the saga reaches `COMPLETED`/`FAILED`. This is the number that actually reflects the Spring Integration orchestration performance.

**Tool:** Apache JMeter 5.6.3 (non-GUI mode)
**Test plan:** `integration-service/loadtest/place-order-benchmark.jmx` (POST wrapped in a Transaction Controller with a While Controller polling loop; product IDs drawn from `integration-service/loadtest/product_ids.csv`, a sample of 500 real in-stock catalog products, to avoid 404s from random UUIDs)
**Load profile:** 20 threads, 10s ramp-up, 50 loops/thread (1000 orders total), polling every 200ms up to 60 attempts
**Target:** `localhost:8085`, Kafka + Postgres running locally, all 6 services up

| Metric | Ingress accept (`POST /api/place-order`) | End-to-end saga completion |
|---|---|---|
| Requests | 1000 | 1000 |
| Errors | 0 (0.00%) | 0 (0.00%) |
| Throughput | 48.9 req/s* | 48.3 sagas/s |
| Avg | 15.48 ms | 227.88 ms |
| Median (p50) | 15 ms | 221 ms |
| p90 | 26 ms | 235 ms |
| p95 | 28 ms | 244 ms |
| p99 | 38 ms | 437 ms |
| Min / Max | 4 ms / 54 ms | 206 ms / 551 ms |

\* Lower than an earlier ingress-only run (99.5 req/s) because threads now block on the polling loop for the full saga duration before starting their next iteration — this is expected once the test measures completion, not just acceptance.

All 1000 orders reached `COMPLETED` with 0 failed/timed-out sagas. Run it yourself with:

```bash
/opt/jmeter/bin/jmeter -n -t integration-service/loadtest/place-order-benchmark.jmx \
  -Jhost=localhost -Jport=8085 -JauthToken="Bearer <jwt>" \
  -Jthreads=20 -JrampUp=10 -Jloops=50 -JmaxPolls=60 -JpollIntervalMs=200 \
  -l results.jtl -e -o report/
```

A valid JWT (obtained via `POST /api/auth/register` or `/api/auth/login` on identity-service) is required — `integration-service` validates the `Authorization` header against identity-service before accepting the order. Also note: `GET /api/saga-status/{orderId}` is currently unauthenticated (added purely for benchmarking/observability) — it should be secured before any non-local use.
