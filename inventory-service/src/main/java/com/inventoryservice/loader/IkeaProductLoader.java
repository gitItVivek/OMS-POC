package com.inventoryservice.loader;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.inventoryservice.entity.Product;
import com.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "inventory.product-load.enabled", havingValue = "true")
public class IkeaProductLoader implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ProductRepository productRepository;

    @Value("${inventory.product-load.batch-size}")
    private int batchSize;

    @Value("${inventory.product-load.file-path}")
    private String filePath;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) {
            log.info("Products table already populated ({} rows). Skipping IKEA load.", productRepository.count());
            return;
        }

        Path path = resolveProductFilePath();
        if (!Files.exists(path)) {
            throw new IllegalStateException("IKEA product file not found at: " + path);
        }

        log.info("Loading IKEA products from {}", path);
        List<JsonNode> records = objectMapper.readValue(path.toFile(), new TypeReference<>() {
        });

        Instant now = Instant.now();
        Set<String> seenSkus = new HashSet<>();
        List<Product> batch = new ArrayList<>(batchSize);
        int skippedDuplicates = 0;
        int inserted = 0;

        for (JsonNode record : records) {
            String sku = textValue(record, "sku");
            if (sku == null || sku.isBlank()) {
                continue;
            }
            if (!seenSkus.add(sku)) {
                skippedDuplicates++;
                continue;
            }

            batch.add(mapToProduct(record, now));
            if (batch.size() >= batchSize) {
                inserted += batchInsert(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            inserted += batchInsert(batch);
        }

        log.info("IKEA product load complete. Inserted={}, skippedDuplicateSkus={}, sourceRecords={}",
                inserted, skippedDuplicates, records.size());
    }

    private Path resolveProductFilePath() {
        Path configured = Path.of(filePath);
        if (configured.isAbsolute()) {
            return configured.normalize();
        }

        Path cwd = Path.of(System.getProperty("user.dir")).normalize();
        Path fileName = configured.getFileName();

        List<Path> candidates = List.of(
                cwd.resolve(configured).normalize(),
                cwd.resolve(fileName).normalize(),
                cwd.getParent() != null ? cwd.getParent().resolve(fileName).normalize() : cwd.resolve(fileName),
                cwd.resolve("docs").resolve(fileName).normalize()
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "IKEA product file not found. Set inventory.product-load.file-path. Tried: " + candidates);
    }

    private Product mapToProduct(JsonNode record, Instant now) {
        return Product.builder()
                .id(UUID.randomUUID())
                .sku(textValue(record, "sku"))
                .title(trimToLength(textValue(record, "product_title"), 500))
                .brand(textValue(record, "brand"))
                .category(trimToLength(textValue(record, "breadcrumbs"), 1000))
                .price(parsePrice(textValue(record, "product_price")))
                .currency(defaultCurrency(textValue(record, "currency")))
                .sourceUrl(textValue(record, "product_url"))
                .availableQty(ThreadLocalRandom.current().nextInt(0, 101))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private int batchInsert(List<Product> products) {
        String sql = """
                INSERT INTO products (id, sku, title, brand, category, price, currency, source_url, available_qty, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (sku) DO NOTHING
                """;

        int[][] results = jdbcTemplate.batchUpdate(sql, products, products.size(), (ps, product) -> {
            ps.setObject(1, product.getId());
            ps.setString(2, product.getSku());
            ps.setString(3, product.getTitle());
            ps.setString(4, product.getBrand());
            ps.setString(5, product.getCategory());
            ps.setBigDecimal(6, product.getPrice());
            ps.setString(7, product.getCurrency());
            ps.setString(8, product.getSourceUrl());
            ps.setInt(9, product.getAvailableQty());
            ps.setTimestamp(10, Timestamp.from(product.getCreatedAt()));
            ps.setTimestamp(11, Timestamp.from(product.getUpdatedAt()));
        });

        int inserted = 0;
        for (int[] batchResult : results) {
            for (int result : batchResult) {
                if (result > 0) {
                    inserted++;
                }
            }
        }
        return inserted;
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asString();
    }

    private String defaultCurrency(String currency) {
        return currency != null ? currency : "USD";
    }

    private BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(rawPrice.trim()).setScale(2, RoundingMode.HALF_UP);
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
