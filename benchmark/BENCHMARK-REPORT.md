# OMS-POC Benchmark Report — Three Orchestration Approaches

**Dates:** 7 July 2026 (A & B) · 8 August 2026 (C)  
**Environment:** Local dev (Windows), 6 Spring Boot services + Apache Kafka 3.9 (Docker)  
**Product under test:** `7dbb2df1-6c36-43f1-83ff-298031cd275f` (quantity 1 per order)

---

## Why we implemented three endpoints

We created three benchmark endpoints to compare implementation approaches for the **same** place-order business flow under controlled load:

- `POST /api/bench/place-order/saga` (A)
- `POST /api/bench/place-order/camel` (B)
- `POST /api/bench/place-order/spring-integration` (C)

At first glance these endpoints look similar because they all accept the same request and trigger the same domain outcome (order lifecycle over Kafka). That is intentional.  
The purpose is to isolate **where orchestration responsibility lives**:

- **A:** Java saga logic is the brain; Camel is a thin event adapter.
- **B:** Camel DSL is the orchestration brain (split/choice/routes).
- **C:** Java saga logic is still the brain; Spring Integration is the event adapter.

This gives us two valid comparisons:

1. **A vs C (adapter comparison):** same saga logic, same `oms.*` topics, different event-listener technology.
2. **A/C vs B (architecture comparison):** Java orchestration vs Camel-heavy orchestration.

So the benchmark is not trying to prove "three different features."  
It is trying to quantify trade-offs (latency, tail behavior, reliability, and maintainability implications) across three technically valid orchestration styles.

---

## 1. Executive summary (meeting opener)

We built **one business flow** — place order → reserve stock → confirm order → start fulfillment → shipment event — and implemented it **three different ways** in the integration-service:

| Approach | Tagline |
|----------|---------|
| **A: Saga + thin Camel** | Business rules in **Java**; Camel only listens to Kafka and calls Java beans |
| **B: Camel-heavy pipeline** | Orchestration in **Camel routes** (Split, Choice, Kafka producers) |
| **C: Saga + Spring Integration** | Same Java saga as A; **Spring Integration** listens to Kafka instead of Camel |

Both paths use **Apache Kafka** across order, inventory, and fulfillment services. We ran **JMeter load tests** (10 concurrent users × 20 orders each) and **manual smoke tests**.

### Headline findings

| Finding | Detail |
|---------|--------|
| **All three work** | 0% HTTP errors at 200 orders each (A, B, C JMeter runs) |
| **Saga-style faster HTTP than B** | A: POST 18 ms / GET 22 ms; C: POST 28 ms / GET 25 ms; B: POST 33 ms / GET 40 ms |
| **A vs C comparable** | Same saga brain; ~10 ms POST mean gap on different run dates — not a performance decision at this scale |
| **C tail spike** | POST max 560 ms on single run — needs more runs to interpret |
| **End-to-end (manual)** | A ~262 ms, B ~212 ms elapsedMs after warmup — same ballpark |
| **Recommendation** | Java saga orchestration + thin adapter (Camel or SI); avoid Camel-heavy for core saga |

> **Important:** JMeter measures **HTTP response times** (accept + status poll after 3–5s wait), not raw Kafka-internal orchestration time alone.

---

## 2. What we tested

### Business flow (identical for both)

```
Customer places order
    → Order service creates order
    → Inventory service reserves stock
    → Order service confirms order
    → Fulfillment service ships
    → Integration marks run COMPLETED
```

### Bench API endpoints

| Step | A: Saga + thin Camel | B: Camel-heavy | C: Saga + Spring Integration |
|------|----------------------|----------------|------------------------------|
| Place order | `POST /api/bench/place-order/saga` | `POST /api/bench/place-order/camel` | `POST /api/bench/place-order/spring-integration` |
| Check status | `GET /api/bench/place-order/{orderId}/status` | Same | Same |

**A vs C adapter selection:** the HTTP path tags `saga_instances.event_adapter` (`CAMEL` vs `SPRING_INTEGRATION`). Both thin Camel and Spring Integration Kafka listeners stay active (different consumer groups); the orchestrator ignores events when the listener’s adapter does not match the saga row. Safe for concurrent Postman runs on all three URLs.

### Infrastructure

| Component | Role |
|-----------|------|
| **identity-service** (8086) | JWT login for bench user |
| **integration-service** (8085) | Orchestration (saga or camel pipeline) |
| **order-service** (8081) | Order create / confirm / cancel |
| **inventory-service** (8084) | Stock reserve / release |
| **fulfillment-service** (8082) | Shipment + event |
| **Apache Kafka** (9092) | Async messaging between services |
| **PostgreSQL** | `saga_instances` or `pipeline_runs` state |

### Kafka topic isolation

| Approach | Topic namespace | Why separate |
|----------|-----------------|--------------|
| A & C (saga) | `oms.order.*`, `oms.inventory.*`, `oms.fulfillment.*` | Shared saga brain — URL selects Camel vs SI adapter |
| B (camel-heavy) | `oms.camel.*` (mirrored) | Benchmark isolation — runs side-by-side with A or C |

---

## 3. How we tested

### Phase A — Manual smoke tests

1. Registered bench user: `bench@oms-poc.test`
2. Called saga endpoint, polled status until `COMPLETED`
3. Called camel endpoint, polled status until `COMPLETED`
4. Compared `elapsedMs` from integration DB

**Manual results (after warmup):**

| Run | Orchestration | elapsedMs | orderStatus |
|-----|---------------|-----------|-------------|
| 2nd saga | SAGA | 262 | CONFIRMED |
| 2nd camel | CAMEL_PIPELINE | 212 | CONFIRMED |

First saga run (~6.2 s) was **cold-start noise**, not a real architectural gap.

### Phase B — JMeter load tests

**Command used:**

```bat
benchmark\run-jmeter.bat 7dbb2df1-6c36-43f1-83ff-298031cd275f camel
benchmark\run-jmeter.bat 7dbb2df1-6c36-43f1-83ff-298031cd275f saga
benchmark\run-jmeter.bat 7dbb2df1-6c36-43f1-83ff-298031cd275f spring-integration
```

**Profile:**

| Parameter | Value |
|-----------|-------|
| Threads | 10 concurrent users |
| Loops | 20 orders per thread |
| Ramp-up | 5 seconds |
| Orders per run | 200 place-order + 200 status checks |
| Wait before status | 3–5 seconds (random timer) |
| Auth | Auto-login per thread as bench user |

**Reports generated:**

- A Saga: `benchmark/jmeter/results/report-saga-20260707_125334/index.html`
- B Camel: `benchmark/jmeter/results/report-camel-20260707_125002/index.html`
- C Spring Integration: `benchmark/jmeter/results/report-spring-integration-20260807_121627/index.html`

---

## 4. JMeter results

### A — Saga + thin Camel (7 July 2026)

| Metric | POST place-order | GET status | Total run |
|--------|------------------|------------|-----------|
| Samples | 200 | 200 | 420 |
| Errors | 0 (0%) | 0 (0%) | 0 (0%) |
| Mean latency | **18 ms** | **22 ms** | 26 ms |
| Median | 17 ms | 21 ms | 20 ms |
| Max | 50 ms | 45 ms | 220 ms |

### B — Camel-heavy (7 July 2026)

| Metric | POST place-order | GET status | Total run |
|--------|------------------|------------|-----------|
| Samples | 200 | 200 | 420 |
| Errors | 0 (0%) | 0 (0%) | 0 (0%) |
| Mean latency | **33 ms** | **40 ms** | 42 ms |
| Median | 30 ms | 38 ms | 34 ms |
| Max | 253 ms | 119 ms | 253 ms |

### C — Saga + Spring Integration (8 August 2026)

Prerequisite: call `POST /api/bench/place-order/spring-integration` (sets `event_adapter=SPRING_INTEGRATION`).

| Metric | POST place-order | GET status | Total run |
|--------|------------------|------------|-----------|
| Samples | 200 | 200 | 430* |
| Errors | 0 (0%) | 0 (0%) | 0 (0%) |
| Mean latency | **28 ms** | **25 ms** | 33 ms |
| Median | 19 ms | 23 ms | 22 ms |
| Max | 560 ms | 193 ms | 686 ms |

\*430 includes login samples from all three JMeter thread groups; only the SI group executed place-order.

### Side-by-side comparison

| Metric | A · thin Camel | B · Camel-heavy | C · Spring Integration |
|--------|----------------|-----------------|------------------------|
| POST accept (mean) | 18 ms | 33 ms | 28 ms |
| Status GET (mean) | 22 ms | 40 ms | 25 ms |
| Error rate | 0% | 0% | 0% |
| POST max | 50 ms | 253 ms | 560 ms |
| Run date | 7 Jul | 7 Jul | 8 Aug |

**Interpretation:** Saga-style paths (A & C) show lower HTTP means than Camel-heavy (B). A vs C are within ~10 ms on POST means — adapter choice is not justified by this POC latency alone. C’s higher POST max is a single-run tail; rerun before treating it as characteristic.

---

## 5. Architecture deep dive

### Approach A — Saga-heavy + thin Camel (~80% Java / ~20% Camel)

**Philosophy:** Orchestration is **domain logic** → keep it in testable Java.

| Layer | Technology | File / component |
|-------|------------|------------------|
| HTTP entry | Spring `RestController` | `BenchPlaceOrderController` |
| Orchestration | Java service | `SagaOrchestratorServiceImpl` |
| State | PostgreSQL | `saga_instances` table |
| Publish commands | Spring `KafkaTemplate` | `SagaCommandPublisher` |
| Consume events | Camel Kafka + JSON + bean | `OrderSagaRoutes` |
| Branching | Java `if/else` | e.g. stock fail → cancel saga |

**Camel EIP used:** Kafka consumer, JSON marshal/unmarshal, bean delegation.  
**Not used in routes:** Split, Choice, Aggregator.

```
HTTP → SagaOrchestratorService (Java)
         ↓ KafkaTemplate publish
       oms.order.create.command
         ↓
       order / inventory / fulfillment services
         ↓ events
       OrderSagaRoutes (thin Camel) → bean → SagaOrchestratorService.onXxx()
```

### Approach B — Camel-heavy pipeline (~80% Camel / ~20% Java beans)

**Philosophy:** Orchestration is **integration flow** → express it in Camel DSL.

| Layer | Technology | File / component |
|-------|------------|------------------|
| HTTP entry | Spring → Camel `direct:` | `BenchPlaceOrderController` → `direct:bench-place-order` |
| Orchestration | Camel routes | `PlaceOrderCamelPipelineRoutes` |
| State | PostgreSQL | `pipeline_runs` table |
| Publish commands | Camel Kafka producer | `to("kafka:oms.camel....")` |
| Consume events | Camel Kafka routes | One route per event type |
| Branching | Camel `choice` | Stock-failed compensation in DSL |
| Line items | Camel `split` + `parallelProcessing` | Validate each item |

**Camel EIP used:** Kafka, direct (entry only), split, choice, marshal/unmarshal, bean.

```
HTTP → direct:bench-place-order (in-JVM only)
         ↓
       Camel route → kafka:oms.camel.order.create.command
         ↓
       order / inventory / fulfillment (camel listeners)
         ↓ oms.camel.* events
       Camel routes chain → next kafka command
```

> `direct:` is **not** cross-service. It is a single in-memory handoff from the HTTP controller into Camel inside integration-service. Everything after that is Kafka.

### Approach C — Saga + Spring Integration (same brain as A)

**Philosophy:** Same as A — orchestration in Java; only the Kafka event adapter differs.

| Layer | Technology | File / component |
|-------|------------|------------------|
| HTTP entry | Spring `RestController` | `SpringIntegrationBenchController` |
| Orchestration | Java service | `SagaOrchestratorServiceImpl` (unchanged) |
| State | PostgreSQL | `saga_instances` (unchanged) |
| Publish commands | Spring `KafkaTemplate` | `SagaCommandPublisher` (unchanged) |
| Consume events | Spring Integration Kafka inbound | `OrderSagaIntegrationFlows` |
| Adapter | HTTP path | `/api/bench/place-order/spring-integration` → `event_adapter=SPRING_INTEGRATION` |

**Where SI lives:** only `integration-service`. Domain services still use `@KafkaListener`.

```
HTTP → SagaOrchestratorService (Java) — identical to A
         ↓ KafkaTemplate publish — identical to A
       oms.* commands / events — identical to A
       OrderSagaIntegrationFlows (SI) → bean → SagaOrchestratorService.onXxx()
```

---

## 6. Saga-heavy vs Camel-heavy vs Spring Integration — when to use which

### Saga-heavy + thin Camel — advantages

| Advantage | Why it matters |
|-----------|----------------|
| **Readable business logic** | Compensation and steps are plain Java — easy for OMS domain developers |
| **Unit testable** | `SagaOrchestratorServiceImpl` tests without spinning Camel routes |
| **Easier debugging** | Stack traces point to Java methods, not route IDs |
| **Team onboarding** | Most Java/Spring teams already know this style |
| **Production saga patterns** | Matches common microservice saga implementations |
| **Thin Camel is the right 20%** | Camel excels at protocol adapters (Kafka in → bean → Kafka out) |

### Saga-heavy — disadvantages

| Disadvantage | Notes |
|--------------|-------|
| More Java code for each new step | You write a method + publisher call per saga step |
| Camel routes are "boring" | By design — they're thin listeners |

### Camel-heavy — advantages

| Advantage | Why it matters |
|-----------|----------------|
| **Visual flow in DSL** | Good if team is Camel-native |
| **EIP building blocks** | Split, Choice, multicast, etc. are first-class |
| **Fast integration prototyping** | Quick to wire Kafka → transform → Kafka |

### Camel-heavy — disadvantages

| Disadvantage | Notes |
|--------------|-------|
| **Harder to unit test** | Route logic spread across DSL + beans |
| **Compensation in routes** | `choice` blocks become complex over time |
| **Debugging** | Follow message through multiple routes |
| **Domain logic in integration layer** | Risk of business rules leaking into routes |
| **No performance win here** | Our benchmark showed saga equal or slightly better |

### Verdict for OMS-POC

| Criterion | Recommendation |
|-----------|----------------|
| Long-term maintainability | **Java saga** + thin adapter (Camel or SI) |
| Kafka event adapter | **Camel (A) or Spring Integration (C)** — team preference; ~10 ms POST mean gap in benchmark |
| Camel-heavy for core saga | **Not recommended** — slower HTTP means, more DSL complexity |
| Performance at 10 users / 200 orders | **No meaningful difference** A vs C — do not pick adapter on ms alone |
| Where to put SI/Camel | **integration-service only** — not domain microservices |

---

## 7. What JMeter did *not* measure

| Not measured | Why |
|--------------|-----|
| Pure Kafka hop latency | No per-topic timers in test plan |
| DB write cost in isolation | Bundled into HTTP + async flow |
| Production-scale load | 10 users on a dev laptop |
| Notification side-effects | Optional service, not in bench path |
| Stock exhaustion failures | Product had sufficient stock |

Treat these numbers as **relative comparison on the same machine**, not SLA guarantees.

---

## 8. Files and artifacts

### JMeter reports used (all three runs — source of benchmark numbers)

| Run | Report folder | Raw data | Key file for numbers |
|-----|---------------|----------|----------------------|
| **A · Saga + thin Camel** | `benchmark/jmeter/results/report-saga-20260707_125334/` | `saga-20260707_125334.jtl` | `statistics.json` |
| **B · Camel-heavy** | `benchmark/jmeter/results/report-camel-20260707_125002/` | `camel-20260707_125002.jtl` | `statistics.json` |
| **C · Spring Integration** | `benchmark/jmeter/results/report-spring-integration-20260807_121627/` | `spring-integration-20260807_121627.jtl` | `statistics.json` |

**Open in browser during demo:**

```
benchmark\jmeter\results\report-saga-20260707_125334\index.html
benchmark\jmeter\results\report-camel-20260707_125002\index.html
benchmark\jmeter\results\report-spring-integration-20260807_121627\index.html
```

Inside each report, show:
- **Dashboard** (`index.html`) — error %, throughput, response times
- **Response Times** (`content/pages/ResponseTimes.html`) — per-request breakdown
- **Statistics** (`statistics.json`) — exact mean/median numbers used in this report

### Documentation and tooling

| Artifact | Path |
|----------|------|
| This report | `benchmark/BENCHMARK-REPORT.md` |
| HTML presentation | `benchmark/benchmark-presentation.html` |
| How-to / runbook | `docs/benchmark-guide.md` |
| JMeter plan | `benchmark/jmeter/oms-place-order-benchmark.jmx` |
| Runner (Windows) | `benchmark/run-jmeter.bat` |

---

## 9. Files to show while explaining (show-and-tell map)

Use this as a **walkthrough checklist** in a meeting or demo.

### Step 1 — Set the scene (both approaches)

| Show | File | What to say |
|------|------|-------------|
| Bench API entry | `integration-service/.../controller/BenchPlaceOrderController.java` | Two endpoints: `/saga` and `/camel`; same status URL for both |
| Benchmark guide | `docs/benchmark-guide.md` | How we ran manual + JMeter tests |
| This report | `benchmark/BENCHMARK-REPORT.md` | Written findings |
| HTML deck | `benchmark/benchmark-presentation.html` | Slide walkthrough |

### Step 2 — Saga-heavy + thin Camel implementation

| Show | File | What to say |
|------|------|-------------|
| **Orchestration (Java)** | `integration-service/.../service/impl/SagaOrchestratorServiceImpl.java` | `startPlaceOrder`, `onOrderCreated`, `onStockReserved`, compensation in Java |
| **Thin Camel listeners** | `integration-service/.../camel/OrderSagaRoutes.java` | Kafka in → JSON unmarshal → bean method — no Split/Choice |
| **Publish commands** | `integration-service/.../kafka/SagaCommandPublisher.java` | Spring `KafkaTemplate` publishes to `oms.*` topics |
| **Topic names** | `integration-service/.../kafka/OmsKafkaTopics.java` | `oms.order.*`, `oms.inventory.*`, `oms.fulfillment.*` |
| **DB state** | Flyway + entity `SagaInstance` / table `saga_instances` | Saga step + status persisted in PostgreSQL |
| **Status API** | `integration-service/.../bench/BenchOrderStatusService.java` | Returns `orchestration: SAGA`, `elapsedMs` from `saga_instances` |
| **Order service listener** | `order-service/.../kafka/OrderCommandListener.java` | Consumes `oms.order.create.command` (saga path) |
| **Inventory listener** | `inventory-service/.../kafka/InventoryCommandListener.java` | Consumes reserve/release on saga topics |
| **Fulfillment listener** | `fulfillment-service/.../kafka/FulfillmentCommandListener.java` | Starts fulfillment, publishes shipment event |

### Step 3 — Camel-heavy pipeline implementation

| Show | File | What to say |
|------|------|-------------|
| **Camel routes (orchestration)** | `integration-service/.../camel/PlaceOrderCamelPipelineRoutes.java` | Split, Choice, Kafka producers — orchestration in DSL |
| **Processor beans** | `integration-service/.../bench/PlaceOrderPipelineProcessor.java` | Beans called from routes; DB `pipeline_runs` |
| **Topic names** | `integration-service/.../kafka/OmsCamelPipelineKafkaTopics.java` | Mirrored `oms.camel.*` namespace |
| **HTTP → Camel entry** | `BenchPlaceOrderController.java` → `direct:bench-place-order` | One in-JVM `direct:` hop, then all Kafka |
| **DB state** | Entity `PipelineRun` / table `pipeline_runs` | Pipeline status + `elapsedMs` |
| **Order service listener** | `order-service/.../kafka/OrderCamelPipelineCommandListener.java` | Same commands, `oms.camel.order.*` topics |
| **Inventory listener** | `inventory-service/.../kafka/InventoryCamelPipelineCommandListener.java` | Camel pipeline stock commands |
| **Fulfillment listener** | `fulfillment-service/.../kafka/FulfillmentCamelPipelineCommandListener.java` | Camel pipeline fulfillment |

### Step 3b — Saga + Spring Integration (approach C)

| What to show | File | Talking point |
|--------------|------|---------------|
| **SI Kafka flows** | `integration-service/.../integration/OrderSagaIntegrationFlows.java` | Same 5 events as `OrderSagaRoutes` — SI inbound adapter → JSON → saga bean |
| **Bench HTTP** | `integration-service/.../controller/SpringIntegrationBenchController.java` | `POST /api/bench/place-order/spring-integration` |
| **URL adapter** | `POST .../spring-integration` sets `event_adapter` | Both Camel + SI listeners stay active; orchestrator filters by ownership |

### Step 4 — Side-by-side difference (one table to draw on whiteboard)

| Concern | Saga + thin Camel | Camel-heavy |
|---------|-------------------|-------------|
| **Who decides next step?** | `SagaOrchestratorServiceImpl` (Java) | `PlaceOrderCamelPipelineRoutes` (DSL) |
| **Who publishes Kafka commands?** | `SagaCommandPublisher` (Spring) | Camel `to("kafka:...")` |
| **Who consumes Kafka events?** | `OrderSagaRoutes` → bean | Camel routes per event |
| **Compensation (stock fail)** | Java in `onStockReservationFailed` | Camel `choice` in route |
| **Line item validation** | Java `validateItems()` | Camel `split` + `parallelProcessing` |
| **Kafka topics** | `oms.order.*` etc. | `oms.camel.*` |
| **State table** | `saga_instances` | `pipeline_runs` |
| **Bench endpoint** | `POST .../saga` | `POST .../camel` |

### Step 5 — Show JMeter results (all three report folders)

| Show | Path | Highlight |
|------|------|-----------|
| **A · Saga report** | `report-saga-20260707_125334/index.html` | 0% errors; POST **18 ms**; GET **22 ms** |
| **B · Camel report** | `report-camel-20260707_125002/index.html` | 0% errors; POST **33 ms**; GET **40 ms** |
| **C · SI report** | `report-spring-integration-20260807_121627/index.html` | 0% errors; POST **28 ms**; GET **25 ms** |

### Step 6 — JMeter test plan (optional technical deep-dive)

| Show | File | What to say |
|------|------|-------------|
| Test plan | `benchmark/jmeter/oms-place-order-benchmark.jmx` | Login once per thread, POST + wait + GET status |
| Runner | `benchmark/run-jmeter.bat` | `run-jmeter.bat <product-uuid> saga\|camel\|spring-integration\|both` |

---

## 10. Suggested talking points for stakeholders

1. **We proved both orchestration styles work** over Kafka with zero errors under moderate load.
2. **Performance is a tie** — choose architecture for clarity and team skills, not milliseconds on a POC laptop.
3. **Saga + thin Camel** keeps order logic where domain experts expect it: in Java, testable and readable.
4. **Camel-heavy** is valuable for integration-centric flows, but for core OMS orchestration it adds complexity without a measurable win here.
5. **Separate Kafka topic namespaces** (`oms.*` vs `oms.camel.*`) let us benchmark fairly without cross-talk.

---

## 11. One-slide summary

```
Same business flow. Same Kafka. Same services. Same 0% errors.

Saga + thin Camel  →  Java orchestrates, Camel listens (recommended)
Camel-heavy        →  Camel orchestrates, beans assist

HTTP latency (JMeter): Saga slightly faster (18/22 ms vs 33/40 ms)
Throughput:            Tie (~2.4 samples/sec)
Manual elapsedMs:      Tie (~200–260 ms after warmup)

Winner for OMS-POC: Saga-heavy + thin Camel (maintainability, not speed)
```
