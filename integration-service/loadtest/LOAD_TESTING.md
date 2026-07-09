# Load Testing the Order Saga (Spring Integration)

This document explains how the benchmark numbers in the root `README.md` ("Benchmarking — Spring Integration (Order Saga)") were produced, so the test can be reproduced or extended.

## What's being measured, and why it's two numbers

`POST /api/place-order` on `integration-service` only does synchronous work: validate the request, write a `SagaInstance` row, publish one `OrderCreateCommand` to Kafka, and return `202`. The actual order saga — `OrderSagaIntegrationFlows`, the Spring Integration Kafka message-driven flows that replaced the old Apache Camel routes — runs asynchronously afterwards, fanning out across `order-service` → `inventory-service` → `fulfillment-service` → `notification-service` over several more Kafka round trips.

So a load test that only times the `POST` response measures HTTP + DB-write + Kafka-produce latency, not the saga orchestration it's meant to benchmark. To capture the real thing, the test measures two things separately:

1. **Ingress accept latency** — the `POST /api/place-order` response time alone.
2. **End-to-end saga completion** — from placing the order to the saga reaching a terminal state (`COMPLETED`/`FAILED`), which is what actually reflects Spring Integration's orchestration performance.

## Prerequisite: a saga-status endpoint

There was no way to observe saga completion from outside, so `GET /api/saga-status/{orderId}` was added to `integration-service`:

- `PlaceOrderController.sagaStatus` → `SagaOrchestratorService.getSagaStatus`
- Reads the existing `SagaInstance` row and returns `{orderId, status, currentStep}`
- Deliberately left **unauthenticated** — it's a benchmarking/observability endpoint, not part of the product API. Secure it before any non-local use.

## Test plan structure (`place-order-benchmark.jmx`)

```
Thread Group (Place Order Load)
├─ CSV Data Set Config          → reads product_ids.csv, provides ${productId}
├─ Random Variable Config       → ${quantity} (1-10)
└─ Transaction Controller "Place Order End-to-End (Saga Completion)"
   │   (generates one parent sample per iteration, includes timer wait time)
   ├─ HTTP Request: POST /api/place-order
   │   ├─ Response Assertion: expect 202
   │   └─ JSR223 PostProcessor: extract orderId from response,
   │       init pollCount=0, terminal=false, sagaStatus=IN_PROGRESS
   └─ While Controller (loops while terminal != "true")
      ├─ Constant Timer: ${POLL_INTERVAL_MS} (default 200ms)
      ├─ HTTP Request: GET /api/saga-status/${orderId}
      └─ JSR223 PostProcessor: parse status from response;
          set terminal=true on COMPLETED/FAILED;
          on hitting ${MAX_POLLS} without a terminal status,
          mark the sample failed (polling timeout) and set terminal=true anyway
          so the thread doesn't hang forever
```

The Transaction Controller's generated sample is the one to read for "how long did this order actually take" — it spans the POST plus every poll plus the wait time between polls.

### Why a CSV of real product IDs

The first version of this test used `${__UUID()}` as the product ID on every request. `order-service` validates the product against `inventory-service` before creating the order, so every one of those random UUIDs 404'd. Each failure gets retried by the Kafka consumer with the `DefaultErrorHandler` backoff before it's given up on (~5s per bad message observed), so a run full of bad product IDs both never lets any saga complete and leaves a large backlog of poison messages on the `oms.order.create.command` topic for the next run to fight through.

Fix: `product_ids.csv` holds ~500 real, in-stock product IDs pulled from `inventory-service`'s catalog (`GET /api/products/search`, filtered to `availableQty >= 50`). The CSV Data Set Config is set to `shareMode.all` so all threads pull from the same cycling list, spreading load across many products instead of hammering one and depleting its stock mid-run.

If you regenerate this file, filter for enough stock that a full run doesn't reserve more than what's available and start seeing legitimate `StockReservationFailedEvent` compensations (those are a valid saga outcome — `FAILED` is a terminal status too — but they change what you're measuring).

## Parameters

| JMeter property | Default | Meaning |
|---|---|---|
| `host` | `localhost` | integration-service host |
| `port` | `8085` | integration-service port |
| `authToken` | `Bearer test-token` | must be a real JWT — see below |
| `threads` | `20` | concurrent virtual users |
| `rampUp` | `10` | seconds to start all threads |
| `loops` | `50` | iterations per thread (→ `threads × loops` total orders) |
| `maxPolls` | `40` | max status polls before declaring a timeout |
| `pollIntervalMs` | `200` | delay between polls |

## Getting a valid JWT

`integration-service` calls `identity-service` to resolve the `Authorization` header on every `place-order` request, so `Bearer test-token` (the `.jmx` file's placeholder default) will get every request rejected with 401. Register or log in a throwaway user first:

```bash
curl -s -X POST http://localhost:8086/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"loadtest@example.com","password":"LoadTest12345","displayName":"Load Test User"}'
# → copy the "accessToken" field (valid for 3600s by default)
```

## Running it

```bash
cd integration-service/loadtest
/opt/jmeter/bin/jmeter -n -t place-order-benchmark.jmx \
  -Jhost=localhost -Jport=8085 -JauthToken="Bearer <accessToken>" \
  -Jthreads=20 -JrampUp=10 -Jloops=50 -JmaxPolls=60 -JpollIntervalMs=200 \
  -l results.jtl -e -o report/
```

`report/statistics.json` has the per-label aggregates (mean/median/p90/p95/p99/throughput). The label to read for saga performance is `Place Order End-to-End (Saga Completion)`; `POST /api/place-order` is the ingress-only number.

## A gotcha to watch for: consumer backlog from a bad run

If a prior run (or manual testing) sent orders with invalid product IDs, those `OrderCreateCommand` messages sit in `oms.order.create.command` and `order-service`'s consumer will burn through them — retrying and failing each one — before it reaches your new, valid messages. Symptom: every saga in a fresh run stays stuck at `ORDER_CREATE_SENT` and never progresses, even though everything looks healthy.

Check for lag:

```bash
docker exec oms-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --describe --group order-service
```

If `LAG` is non-zero and stuck behind known-bad data, stop `order-service`, then skip the backlog (safe for test/dev data — this discards unprocessed messages, so never do this against real orders):

```bash
docker exec oms-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --group order-service \
  --topic oms.order.create.command --reset-offsets --to-latest --execute
```

Then restart `order-service`.

## Results

See the "Benchmarking — Spring Integration (Order Saga)" section in the repo root `README.md` for the latest numbers. Summary of the last run: 1000 orders, 0 errors, ingress accept median 15ms / p99 38ms, end-to-end saga completion median 221ms / p99 437ms.
