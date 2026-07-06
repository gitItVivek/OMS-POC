# Parallel Stock Lookup with Spring Integration

## The problem this solves

When a customer places an order with multiple items, `order-service` has to ask
`inventory-service` "is this product in stock?" for every single item before it
can save the order. The old code did this one item at a time, in a plain `for`
loop:

```java
for (OrderItemRequestDto itemRequest : request.getItems()) {
    ProductSummaryDto product = inventoryServiceClient.getProductById(itemRequest.getProductId());
    // ... check stock, add up total
}
```

If an order has 5 items and each stock check takes 100ms, that's 500ms spent
waiting, one call after another, even though none of the calls depend on each
other. This change makes all the stock checks happen **at the same time**
instead, using Spring Integration.

## What is Spring Integration, in one sentence

It's a library for describing a sequence of processing steps ("split this
list, do X to each piece in parallel, then gather the results back together")
as configuration instead of hand-written loops and thread-pool code. It runs
inside the existing app — no new servers, no new infrastructure.

## The new flow

```
List<OrderItemRequestDto>
        │
        ▼
   [ split ]              -- breaks the list into one message per item
        │
        ▼
[ executor channel ]      -- hands each item to a thread pool (parallelism happens here)
        │
        ▼
   [ handle ]             -- calls inventory-service for that one item
        │
        ▼
  [ aggregate ]           -- waits for all items, gathers results back into one list
        │
        ▼
List<OrderItemStockResult>
```

Everything above the line still calls the exact same
`InventoryServiceClient.getProductById(...)` REST call as before — only *how
many at once* changed.

## The code

### 1. Dependency (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-integration</artifactId>
</dependency>
```

### 2. A small result DTO (`dto/OrderItemStockResult.java`)

Carries the original request item alongside the product it resolved to, so
each result is self-contained (order of results doesn't matter):

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemStockResult {
    private OrderItemRequestDto itemRequest;
    private ProductSummaryDto product;
}
```

### 3. The gateway — the "front door" to the flow (`integration/StockLookupGateway.java`)

A gateway is just an interface. Spring Integration generates the
implementation at runtime; calling it from your service code looks and feels
like calling a normal method, even though a whole parallel pipeline runs
underneath.

```java
@MessagingGateway(errorChannel = "stockLookupErrorChannel")
public interface StockLookupGateway {

    @Gateway(
            requestChannel = "stockLookupRequestChannel",
            replyChannel = "stockLookupReplyChannel",
            replyTimeout = 10000)
    List<OrderItemStockResult> lookupStock(List<OrderItemRequestDto> items);
}
```

- `requestChannel` — where the input list is dropped off.
- `replyChannel` — where the final combined result shows up.
- `errorChannel` — if any parallel lookup throws (e.g. inventory-service is
  down, or the product doesn't exist), the exception is routed here and
  re-thrown to the caller, instead of silently getting lost on a background
  thread.
- `replyTimeout` — safety net: if something never comes back, fail after 10s
  instead of hanging forever.

### 4. The flow definition (`config/StockLookupIntegrationConfig.java`)

This is where the diagram above becomes code:

```java
@Configuration
public class StockLookupIntegrationConfig {

    @Bean
    public Executor stockLookupExecutor() {
        return Executors.newFixedThreadPool(8);
    }

    @Bean
    public IntegrationFlow stockLookupFlow(InventoryServiceClient inventoryServiceClient, Executor stockLookupExecutor) {
        return IntegrationFlow.from("stockLookupRequestChannel")
                .split()                                            // one message per order item
                .channel(c -> c.executor(stockLookupExecutor))      // dispatch onto the thread pool
                .handle(OrderItemRequestDto.class, (itemRequest, headers) -> OrderItemStockResult.builder()
                        .itemRequest(itemRequest)
                        .product(inventoryServiceClient.getProductById(itemRequest.getProductId()))
                        .build())
                .aggregate()                                         // wait for all items, recombine into a list
                .channel("stockLookupReplyChannel")
                .get();
    }

    @Bean
    public IntegrationFlow stockLookupErrorFlow() {
        return IntegrationFlow.from("stockLookupErrorChannel")
                .<MessagingException>handle((exception, headers) -> {
                    Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
                    if (cause instanceof RuntimeException runtimeException) {
                        throw runtimeException;
                    }
                    throw new IllegalStateException("Stock lookup failed", cause);
                })
                .get();
    }
}
```

`split()` and `aggregate()` automatically tag each item with a correlation ID
and a sequence number/size, so the aggregator knows when it has received all
the pieces belonging to one order — you don't manage that bookkeeping
yourself.

### 5. Using it (`service/impl/OrderServiceImpl.java`)

The service method barely changed — the sequential loop over
`inventoryServiceClient` became one call to the gateway, followed by the same
validation loop as before, now iterating over the already-resolved results:

```java
List<OrderItemStockResult> stockResults = stockLookupGateway.lookupStock(request.getItems());

for (OrderItemStockResult stockResult : stockResults) {
    OrderItemRequestDto itemRequest = stockResult.getItemRequest();
    ProductSummaryDto product = stockResult.getProduct();
    if (product.getAvailableQty() == null || product.getAvailableQty() < itemRequest.getQuantity()) {
        throw new InsufficientStockException(...);
    }
    // ... totals, build OrderItem, same as before
}
```

The `InventoryServiceClient` bean is no longer injected directly into
`OrderServiceImpl` — it's only used inside the flow now.

## Scope

This only touches `order-service`. It calls the same external
`inventory-service` REST endpoint as before, the same number of times per
order — just concurrently instead of sequentially. No other service, queue, or
shared infrastructure was introduced.

## Things to know before relying on this in production

- **Thread pool size (`newFixedThreadPool(8)`)** is a starting point — size it
  to inventory-service's actual capacity, not arbitrarily.
- **One failing item fails the whole order.** If any single product lookup
  throws, the whole `lookupStock(...)` call throws and the order isn't
  created — same all-or-nothing behavior as the original loop.
- **`replyTimeout = 10000`** means a slow inventory-service response fails the
  order creation after 10 seconds rather than hanging indefinitely — tune this
  to a realistic upper bound for your environment.
