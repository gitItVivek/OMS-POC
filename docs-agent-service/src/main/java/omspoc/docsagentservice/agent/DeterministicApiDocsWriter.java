package omspoc.docsagentservice.agent;

import omspoc.docsagentservice.config.DocsAgentProperties;
import omspoc.docsagentservice.inventory.ControllerInventoryScanner;
import omspoc.docsagentservice.inventory.EndpointModel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Inventory-first Markdown writer used when the LLM is unavailable (e.g. quota)
 * and as a reliable baseline for CI demos.
 */
@Component
public class DeterministicApiDocsWriter {

    private static final Pattern FIELD = Pattern.compile(
            "(?:private|protected)\\s+([\\w.<>,\\s\\[\\]]+?)\\s+(\\w+)\\s*;");

    private final DocsAgentProperties properties;
    private final ControllerInventoryScanner scanner;

    public DeterministicApiDocsWriter(DocsAgentProperties properties, ControllerInventoryScanner scanner) {
        this.properties = properties;
        this.scanner = scanner;
    }

    public String writeAll() {
        List<EndpointModel> all = scanner.scanAll();
        Path outDir = Path.of(properties.getRepoRoot()).toAbsolutePath().normalize()
                .resolve(properties.getOutputDir());
        try {
            Files.createDirectories(outDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create " + outDir, e);
        }

        Map<String, List<EndpointModel>> byService = all.stream()
                .collect(Collectors.groupingBy(EndpointModel::service, LinkedHashMap::new, Collectors.toList()));

        write(outDir.resolve("README.md"), buildIndex(byService));
        for (String service : properties.getServices()) {
            List<EndpointModel> endpoints = byService.getOrDefault(service, List.of());
            write(outDir.resolve(service + ".md"), buildServiceDoc(service, endpoints));
        }
        return "Deterministic docs written for " + properties.getServices().size()
                + " services (" + all.size() + " endpoints) under " + outDir;
    }

    private void write(Path file, String content) {
        try {
            Files.writeString(file, content);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write " + file, e);
        }
    }

    private String buildIndex(Map<String, List<EndpointModel>> byService) {
        StringBuilder sb = new StringBuilder();
        sb.append("# OMS-POC API Documentation\n\n");
        sb.append("Auto-generated from Spring `@RestController` sources by `docs-agent-service`.\n\n");
        sb.append("## Quick start\n\n");
        sb.append("1. **Authenticate** with identity-service (`POST /api/auth/login`) and keep the Bearer token as `TOKEN`.\n");
        sb.append("2. **Place an order** through integration-service (`POST /api/place-order`) — the customer entrypoint.\n");
        sb.append("3. Replace `BASE_URL` with the service host (local defaults vary by service port).\n\n");
        sb.append("```bash\n");
        sb.append("# Login\n");
        sb.append("curl -s -X POST \"$BASE_URL/api/auth/login\" \\\n");
        sb.append("  -H \"Content-Type: application/json\" \\\n");
        sb.append("  -d '{\"email\":\"you@example.com\",\"password\":\"secret\"}'\n\n");
        sb.append("# Place order (integration-service)\n");
        sb.append("curl -s -X POST \"$BASE_URL/api/place-order\" \\\n");
        sb.append("  -H \"Authorization: Bearer $TOKEN\" \\\n");
        sb.append("  -H \"Content-Type: application/json\" \\\n");
        sb.append("  -d '{\"items\":[{\"productId\":\"PRODUCT_UUID\",\"quantity\":1}]}'\n");
        sb.append("```\n\n");
        sb.append("## Services\n\n");
        sb.append("| Service | Endpoints | Doc |\n");
        sb.append("|---------|-----------|-----|\n");
        for (String service : properties.getServices()) {
            int count = byService.getOrDefault(service, List.of()).size();
            sb.append("| `").append(service).append("` | ").append(count)
                    .append(" | [").append(service).append(".md](").append(service).append(".md) |\n");
        }
        sb.append("\n> Endpoint lists are deterministic (controller scan). Prose may be LLM-enhanced when OpenAI is available.\n");
        return sb.toString();
    }

    private String buildServiceDoc(String service, List<EndpointModel> endpoints) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(service).append("\n\n");
        sb.append(serviceOverview(service)).append("\n\n");
        sb.append("## Endpoint index\n\n");
        sb.append("| Method | Path | Handler | Auth hint |\n");
        sb.append("|--------|------|---------|-----------|\n");
        for (EndpointModel e : endpoints) {
            sb.append("| `").append(e.httpMethod()).append("` | `").append(e.path()).append("` | `")
                    .append(e.controllerClass()).append(".").append(e.javaMethodName())
                    .append("` | ").append(e.requiresAuthHint() ? "yes" : "—").append(" |\n");
        }
        sb.append("\n## Endpoints\n\n");
        for (EndpointModel e : endpoints) {
            sb.append("### `").append(e.httpMethod()).append(" ").append(e.path()).append("`\n\n");
            sb.append("- **Controller:** `").append(e.controllerClass()).append(".")
                    .append(e.javaMethodName()).append("`\n");
            sb.append("- **Returns:** `").append(nullToDash(e.returnType())).append("`\n");
            if (e.requestBodyType() != null) {
                sb.append("- **Request body:** `").append(e.requestBodyType()).append("`\n");
                List<String> fields = readDtoFields(e.requestBodyType());
                if (!fields.isEmpty()) {
                    sb.append("- **Body fields (from DTO):** ").append(String.join(", ", fields)).append("\n");
                }
            }
            if (!e.pathParams().isEmpty()) {
                sb.append("- **Path params:** ").append(String.join(", ", e.pathParams())).append("\n");
            }
            if (!e.queryParams().isEmpty()) {
                sb.append("- **Query params:** ").append(String.join(", ", e.queryParams())).append("\n");
            }
            if (e.requiresAuthHint()) {
                sb.append("- **Auth:** Bearer token / Spring `Authentication` expected\n");
            }
            sb.append("\n```bash\n");
            sb.append(curlFor(e));
            sb.append("```\n\n");
        }
        return sb.toString();
    }

    private String serviceOverview(String service) {
        return switch (service) {
            case "identity-service" -> "Owns JWT authentication and user identity. Other services call `/api/auth/me` to resolve the caller.";
            case "integration-service" -> "Orchestration edge for customers. Prefer `POST /api/place-order` as the place-order entrypoint.";
            case "order-service" -> "Order domain API: create/read orders, search, and dashboard analytics.";
            case "inventory-service" -> "Product catalog and stock reservation internals used during order placement.";
            case "fulfillment-service" -> "Shipment creation and fulfillment lifecycle endpoints.";
            case "notification-service" -> "Notification dispatch internals for order lifecycle events.";
            default -> "OMS microservice REST API.";
        };
    }

    private String curlFor(EndpointModel e) {
        StringBuilder c = new StringBuilder();
        c.append("curl -s -X ").append(e.httpMethod()).append(" \"$BASE_URL").append(e.path()).append("\"");
        if (e.requiresAuthHint() || "integration-service".equals(e.service())
                || ("order-service".equals(e.service()) && "POST".equals(e.httpMethod()))) {
            c.append(" \\\n  -H \"Authorization: Bearer $TOKEN\"");
        }
        if (e.requestBodyType() != null && List.of("POST", "PUT", "PATCH").contains(e.httpMethod())) {
            c.append(" \\\n  -H \"Content-Type: application/json\"");
            c.append(" \\\n  -d '{}'");
        }
        c.append('\n');
        return c.toString();
    }

    private List<String> readDtoFields(String simpleType) {
        if (simpleType == null || simpleType.isBlank()) {
            return List.of();
        }
        Path repo = Path.of(properties.getRepoRoot()).toAbsolutePath().normalize();
        String fileName = simpleType.contains("<")
                ? simpleType.substring(0, simpleType.indexOf('<')).trim() + ".java"
                : simpleType + ".java";
        try (var walk = Files.walk(repo)) {
            Path match = walk.filter(p -> p.getFileName().toString().equals(fileName))
                    .filter(p -> {
                        String s = p.toString().replace('\\', '/');
                        return s.contains("/src/main/");
                    })
                    .findFirst()
                    .orElse(null);
            if (match == null) {
                return List.of();
            }
            String src = Files.readString(match);
            List<String> fields = new ArrayList<>();
            Matcher m = FIELD.matcher(src);
            while (m.find()) {
                fields.add(m.group(2) + ":" + m.group(1).replaceAll("\\s+", " ").trim());
            }
            return fields;
        } catch (IOException e) {
            return List.of();
        }
    }

    private static String nullToDash(String v) {
        return v == null || v.isBlank() ? "—" : v;
    }
}
