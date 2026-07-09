# Benchmark Comparison — Three Coexisting Approaches

> **HTML deck:** [`benchmark/benchmark-presentation.html`](benchmark-presentation.html) — meeting slides (slides 1–3 = what we built, implemented, found).

## Why this comparison exists

This comparison is not "three different products." It is one OMS place-order flow implemented in three orchestration styles so we can measure trade-offs clearly.

We introduced three endpoints to answer two different questions:

1. **Adapter question (A vs C):**  
   If saga logic stays in Java, does changing only the Kafka event adapter (Camel -> Spring Integration) materially change behavior or latency?

2. **Architecture question (A/C vs B):**  
   What changes when orchestration moves from Java saga methods into Camel DSL route orchestration?

Because these are different questions, we keep:

- **A and C** on the same saga brain and same `oms.*` topics (URL tags `event_adapter`; both listeners stay active).
- **B** on isolated `oms.camel.*` topics so it can coexist without interference.

This document focuses on presenting the results in that exact framing: adapter swap vs architecture swap.

This compares **three live benchmark paths** in `integration-service`:

| # | Approach | Status | Bench endpoint | Kafka topics |
|---|---|---|---|---|
| A | **Saga + thin Camel** | Live | `POST /api/bench/place-order/saga` | `oms.*` |
| B | **Camel-heavy pipeline** | Live | `POST /api/bench/place-order/camel` | `oms.camel.*` |
| C | **Saga + Spring Integration** | Live | `POST /api/bench/place-order/spring-integration` | `oms.*` |

**A and C** share `SagaOrchestratorServiceImpl`, `saga_instances`, and `oms.*` topics. Only the Kafka event adapter differs (Camel `OrderSagaRoutes` vs Spring Integration `OrderSagaIntegrationFlows`).

**Domain services** use plain `@KafkaListener` — no Camel, no Spring Integration.

## Adapter selection (A vs C)

| Endpoint | Stored `event_adapter` | Kafka listener that advances the saga |
|----------|------------------------|----------------------------------------|
| `POST /api/bench/place-order/saga` | `CAMEL` | Thin Camel `OrderSagaRoutes` |
| `POST /api/bench/place-order/spring-integration` | `SPRING_INTEGRATION` | Spring Integration `OrderSagaIntegrationFlows` |

Both listeners stay active (different consumer groups). The orchestrator no-ops when the listener’s adapter does not match the saga row — safe for concurrent Postman runs. **B** uses `oms.camel.*` and is independent.

## Benchmark methodology (matched profile)

Plan: [`benchmark/jmeter/oms-place-order-benchmark.jmx`](jmeter/oms-place-order-benchmark.jmx)

| Parameter | Value |
|---|---|
| Concurrency | 10 threads |
| Orders | 20 loops/thread = 200 |
| Ramp-up | 5s |
| Quantity | 1 per order |
| Wait | 3–5s random, then one status GET |
| Product | `7dbb2df1-6c36-43f1-83ff-298031cd275f` |

## Results (JMeter HTTP metrics)

### A — Saga + thin Camel (7 Jul 2026)

Report: [`report-saga-20260707_125334/`](jmeter/results/report-saga-20260707_125334/)

| Metric | POST `/saga` | GET status |
|---|---|---|
| Mean | 18 ms | 22 ms |
| Median | 17 ms | 21 ms |
| Max | 50 ms | 45 ms |
| Errors | 0% | 0% |

Manual `elapsedMs` (post-warmup): ~262 ms

### B — Camel-heavy (7 Jul 2026)

Report: [`report-camel-20260707_125002/`](jmeter/results/report-camel-20260707_125002/)

| Metric | POST `/camel` | GET status |
|---|---|---|
| Mean | 33 ms | 40 ms |
| Median | 30 ms | 38 ms |
| Max | 253 ms | 119 ms |
| Errors | 0% | 0% |

Manual `elapsedMs` (post-warmup): ~212 ms

### C — Saga + Spring Integration (8 Aug 2026)

Report: [`report-spring-integration-20260807_121627/`](jmeter/results/report-spring-integration-20260807_121627/)

| Metric | POST `/spring-integration` | GET status |
|---|---|---|
| Mean | 28 ms | 25 ms |
| Median | 19 ms | 23 ms |
| Max | 560 ms | 193 ms |
| Errors | 0% | 0% |

Run command:

```bat
benchmark\run-jmeter.bat 7dbb2df1-6c36-43f1-83ff-298031cd275f spring-integration
```

(Via `POST /api/bench/place-order/spring-integration` — no property restart required.)

### Side-by-side (POST + GET means)

| | A · thin Camel | B · Camel-heavy | C · Spring Integration |
|---|---|---|---|
| POST mean | 18 ms | 33 ms | 28 ms |
| GET mean | 22 ms | 40 ms | 25 ms |
| Errors | 0% | 0% | 0% |
| Run date | 7 Jul | 7 Jul | 8 Aug |

## Interpretation (unbiased)

1. **All three work** at 10 users / 200 orders — 0% HTTP errors.
2. **Saga-style (A & C)** shows lower HTTP means than **Camel-heavy (B)** in these runs.
3. **A vs C** — adapter swap only; means within ~10 ms on POST (18 vs 28). Not enough to declare a performance winner on this hardware. Pick adapter by team skills and Spring-native preference.
4. **C POST max (560 ms)** is a tail spike on a single run — not evidence that SI is inherently slower.
5. **JMeter measures HTTP** (accept + status poll), not isolated Kafka orchestration latency.
6. **Different run dates** — treat absolute milliseconds as directional, not certified regression data.

## Where Spring Integration lives

- **Only `integration-service`** — `OrderSagaIntegrationFlows.java`, `SpringIntegrationBenchController.java`
- **Not** in order, inventory, fulfillment, or notification services
- Same placement rationale as thin Camel: orchestration crosses service boundaries

## Sources

- [`benchmark/BENCHMARK-REPORT.md`](BENCHMARK-REPORT.md)
- [`docs/benchmark-guide.md`](../docs/benchmark-guide.md)
- [`docs/demo-guide.md`](../docs/demo-guide.md) — Part 5
