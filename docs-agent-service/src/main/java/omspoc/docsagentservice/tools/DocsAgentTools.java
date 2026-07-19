package omspoc.docsagentservice.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import omspoc.docsagentservice.config.DocsAgentProperties;
import omspoc.docsagentservice.inventory.ControllerInventoryScanner;
import omspoc.docsagentservice.inventory.EndpointModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent tools used by ChatClient. The same ToolCallbacks are exposed over MCP
 * via {@code spring.ai.mcp.server.tool-callback-converter=true}.
 */
@Component
public class DocsAgentTools {

    private final DocsAgentProperties properties;
    private final ControllerInventoryScanner scanner;
    private final ObjectMapper objectMapper;

    public DocsAgentTools(
            DocsAgentProperties properties,
            ControllerInventoryScanner scanner,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.scanner = scanner;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "List OMS microservice names that should be documented.")
    public String listServices() {
        return String.join(", ", properties.getServices());
    }

    @Tool(description = "Return deterministic REST endpoint inventory scanned from *Controller.java files. Optionally filter by service name.")
    public String getEndpointInventory(
            @ToolParam(description = "Optional service folder name, e.g. order-service. Empty means all services.")
            String service) {
        List<EndpointModel> endpoints;
        if (service == null || service.isBlank()) {
            endpoints = scanner.scanAll();
        } else {
            endpoints = scanner.scanService(service.trim());
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(endpoints);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize inventory", e);
        }
    }

    @Tool(description = "Read an existing markdown file under docs/api. Pass filename only, e.g. order-service.md or README.md.")
    public String readExistingDoc(
            @ToolParam(description = "Filename under docs/api, e.g. integration-service.md")
            String filename) {
        Path file = resolveDocFile(filename);
        if (!Files.isRegularFile(file)) {
            return "FILE_NOT_FOUND: " + filename;
        }
        try {
            return Files.readString(file);
        } catch (IOException e) {
            return "ERROR reading " + filename + ": " + e.getMessage();
        }
    }

    @Tool(description = "Write or overwrite a markdown file under docs/api. Pass filename only and full markdown content.")
    public String writeDocFile(
            @ToolParam(description = "Filename under docs/api, e.g. order-service.md or README.md")
            String filename,
            @ToolParam(description = "Full markdown document content to write")
            String content) {
        Path file = resolveDocFile(filename);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content == null ? "" : content);
            return "WROTE " + file.toAbsolutePath().normalize();
        } catch (IOException e) {
            return "ERROR writing " + filename + ": " + e.getMessage();
        }
    }

    @Tool(description = "Read a source file snippet by absolute or repo-relative path, optionally limited by startLine/endLine (1-based inclusive).")
    public String readSourceSnippet(
            @ToolParam(description = "Absolute path or path relative to repo root")
            String path,
            @ToolParam(description = "Optional 1-based start line (inclusive). Use 0 for beginning.")
            Integer startLine,
            @ToolParam(description = "Optional 1-based end line (inclusive). Use 0 for end of file.")
            Integer endLine) {
        Path file = resolveRepoPath(path);
        if (!Files.isRegularFile(file)) {
            return "FILE_NOT_FOUND: " + path;
        }
        try {
            List<String> lines = Files.readAllLines(file);
            int start = (startLine == null || startLine < 1) ? 1 : startLine;
            int end = (endLine == null || endLine < 1) ? lines.size() : Math.min(endLine, lines.size());
            if (start > lines.size()) {
                return "EMPTY_RANGE";
            }
            start = Math.min(start, lines.size());
            end = Math.max(end, start);
            StringBuilder sb = new StringBuilder();
            for (int i = start; i <= end; i++) {
                sb.append(i).append(": ").append(lines.get(i - 1)).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            return "ERROR reading source: " + e.getMessage();
        }
    }

    @Tool(description = "Summarize inventory counts per service for a quick overview.")
    public String summarizeInventory() {
        Map<String, Long> counts = scanner.scanAll().stream()
                .collect(Collectors.groupingBy(EndpointModel::service, LinkedHashMap::new, Collectors.counting()));
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(counts);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path resolveDocFile(String filename) {
        String safe = Path.of(filename).getFileName().toString();
        if (!safe.endsWith(".md")) {
            throw new IllegalArgumentException("Only .md files are allowed under docs/api");
        }
        return repoRoot().resolve(properties.getOutputDir()).resolve(safe).normalize();
    }

    private Path resolveRepoPath(String path) {
        Path p = Path.of(path);
        if (p.isAbsolute()) {
            return p.normalize();
        }
        return repoRoot().resolve(p).normalize();
    }

    private Path repoRoot() {
        return Path.of(properties.getRepoRoot()).toAbsolutePath().normalize();
    }
}
