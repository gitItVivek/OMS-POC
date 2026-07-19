package omspoc.docsagentservice.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads KEY=VALUE pairs from a local .env into process environment properties
 * when the key is not already set. Never logs secret values.
 */
public final class EnvFileLoader {

    private EnvFileLoader() {
    }

    public static void loadDefaultLocations() {
        List<Path> candidates = List.of(
                Path.of(".env"),
                Path.of("docs-agent-service", ".env"),
                Path.of("..", "docs-agent-service", ".env")
        );
        for (Path path : candidates) {
            if (Files.isRegularFile(path)) {
                load(path);
                return;
            }
        }
    }

    public static void load(Path path) {
        try {
            for (String raw : Files.readAllLines(path)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
            // Optional local convenience file; CI injects secrets via env.
        }
    }
}
