package omspoc.docsagentservice.inventory;

import omspoc.docsagentservice.config.DocsAgentProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class ControllerInventoryScanner {

    private static final Pattern CLASS_REQUEST_MAPPING = Pattern.compile(
            "@RequestMapping\\s*(?:\\(\\s*(?:value\\s*=\\s*|path\\s*=\\s*)?\"([^\"]*)\"\\s*\\))?");
    private static final Pattern METHOD_MAPPING = Pattern.compile(
            "@(Get|Post|Put|Patch|Delete)Mapping\\s*(?:\\(\\s*(?:value\\s*=\\s*|path\\s*=\\s*)?\"([^\"]*)\"[^)]*\\))?");
    private static final Pattern METHOD_REQUEST_MAPPING = Pattern.compile(
            "@RequestMapping\\s*\\([^)]*method\\s*=\\s*RequestMethod\\.(GET|POST|PUT|PATCH|DELETE)[^)]*(?:value\\s*=\\s*|path\\s*=\\s*)?\"([^\"]*)\"[^)]*\\)");
    private static final Pattern METHOD_SIGNATURE = Pattern.compile(
            "(?:public|protected)\\s+([\\w.<>,\\s\\[\\]]+?)\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern PATH_VAR = Pattern.compile("@PathVariable(?:\\([^)]*\\))?\\s+(?:final\\s+)?([\\w.]+)\\s+(\\w+)");
    private static final Pattern QUERY_PARAM = Pattern.compile(
            "@RequestParam(?:\\(\\s*(?:value\\s*=\\s*|name\\s*=\\s*)?\"([^\"]+)\"[^)]*\\))?\\s+(?:final\\s+)?([\\w.<>,\\s]+?)\\s+(\\w+)");
    private static final Pattern REQUEST_BODY = Pattern.compile(
            "@RequestBody(?:\\([^)]*\\))?\\s+(?:final\\s+)?([\\w.]+)\\s+(\\w+)");
    private static final Pattern AUTH_HINT = Pattern.compile(
            "\\bAuthentication\\b|HttpHeaders\\.AUTHORIZATION|@RequestHeader\\s*\\([^)]*AUTHORIZATION");

    private final DocsAgentProperties properties;

    public ControllerInventoryScanner(DocsAgentProperties properties) {
        this.properties = properties;
    }

    public List<EndpointModel> scanAll() {
        List<EndpointModel> endpoints = new ArrayList<>();
        Path repoRoot = Path.of(properties.getRepoRoot()).toAbsolutePath().normalize();
        for (String service : properties.getServices()) {
            endpoints.addAll(scanService(repoRoot, service));
        }
        endpoints.sort(Comparator
                .comparing(EndpointModel::service)
                .thenComparing(EndpointModel::path)
                .thenComparing(EndpointModel::httpMethod));
        return endpoints;
    }

    public List<EndpointModel> scanService(String service) {
        Path repoRoot = Path.of(properties.getRepoRoot()).toAbsolutePath().normalize();
        return scanService(repoRoot, service);
    }

    private List<EndpointModel> scanService(Path repoRoot, String service) {
        Path javaRoot = repoRoot.resolve(service).resolve("src/main/java");
        if (!Files.isDirectory(javaRoot)) {
            return List.of();
        }
        List<EndpointModel> endpoints = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(javaRoot)) {
            walk.filter(p -> p.getFileName().toString().endsWith("Controller.java"))
                    .sorted()
                    .forEach(file -> endpoints.addAll(parseController(service, file)));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan controllers under " + javaRoot, e);
        }
        return endpoints;
    }

    private List<EndpointModel> parseController(String service, Path file) {
        String source;
        try {
            source = Files.readString(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + file, e);
        }
        if (!source.contains("@RestController")) {
            return List.of();
        }

        String classBasePath = extractClassBasePath(source);
        String controllerClass = file.getFileName().toString().replace(".java", "");
        String controllerFile = file.toAbsolutePath().normalize().toString();

        List<EndpointModel> endpoints = new ArrayList<>();
        Matcher mappingMatcher = METHOD_MAPPING.matcher(source);
        while (mappingMatcher.find()) {
            String httpMethod = mappingMatcher.group(1).toUpperCase();
            String methodPath = Optional.ofNullable(mappingMatcher.group(2)).orElse("");
            int annotationEnd = mappingMatcher.end();
            Optional<MethodMeta> meta = findNextMethod(source, annotationEnd);
            if (meta.isEmpty()) {
                continue;
            }
            MethodMeta m = meta.get();
            String fullPath = joinPaths(classBasePath, methodPath);
            endpoints.add(new EndpointModel(
                    service,
                    controllerClass,
                    controllerFile,
                    m.name(),
                    httpMethod,
                    fullPath,
                    m.requestBodyType(),
                    m.returnType(),
                    m.pathParams(),
                    m.queryParams(),
                    m.requiresAuth()
            ));
        }

        Matcher rmMatcher = METHOD_REQUEST_MAPPING.matcher(source);
        while (rmMatcher.find()) {
            String httpMethod = rmMatcher.group(1).toUpperCase();
            String methodPath = Optional.ofNullable(rmMatcher.group(2)).orElse("");
            int annotationEnd = rmMatcher.end();
            Optional<MethodMeta> meta = findNextMethod(source, annotationEnd);
            if (meta.isEmpty()) {
                continue;
            }
            MethodMeta m = meta.get();
            endpoints.add(new EndpointModel(
                    service,
                    controllerClass,
                    controllerFile,
                    m.name(),
                    httpMethod,
                    joinPaths(classBasePath, methodPath),
                    m.requestBodyType(),
                    m.returnType(),
                    m.pathParams(),
                    m.queryParams(),
                    m.requiresAuth()
            ));
        }
        return endpoints;
    }

    private String extractClassBasePath(String source) {
        int classIdx = source.indexOf("class ");
        String header = classIdx > 0 ? source.substring(0, classIdx) : source;
        Matcher matcher = CLASS_REQUEST_MAPPING.matcher(header);
        String last = "";
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                last = matcher.group(1);
            }
        }
        return last;
    }

    private Optional<MethodMeta> findNextMethod(String source, int from) {
        Matcher matcher = METHOD_SIGNATURE.matcher(source);
        if (!matcher.find(from)) {
            return Optional.empty();
        }
        // Prefer a signature that appears soon after the mapping annotation
        if (matcher.start() - from > 400) {
            return Optional.empty();
        }
        String returnType = simplifyType(matcher.group(1).trim());
        String name = matcher.group(2);
        String params = matcher.group(3);
        List<String> pathParams = new ArrayList<>();
        Matcher pv = PATH_VAR.matcher(params);
        while (pv.find()) {
            pathParams.add(pv.group(2) + ":" + simplifyType(pv.group(1)));
        }
        List<String> queryParams = new ArrayList<>();
        Matcher qp = QUERY_PARAM.matcher(params);
        while (qp.find()) {
            String qpName = qp.group(1) != null ? qp.group(1) : qp.group(3);
            queryParams.add(qpName + ":" + simplifyType(qp.group(2).trim()));
        }
        String bodyType = null;
        Matcher body = REQUEST_BODY.matcher(params);
        if (body.find()) {
            bodyType = simplifyType(body.group(1));
        }
        boolean auth = AUTH_HINT.matcher(params).find();
        return Optional.of(new MethodMeta(name, returnType, bodyType, pathParams, queryParams, auth));
    }

    private static String joinPaths(String base, String methodPath) {
        String a = base == null ? "" : base.trim();
        String b = methodPath == null ? "" : methodPath.trim();
        if (a.isEmpty()) {
            return b.isEmpty() ? "/" : (b.startsWith("/") ? b : "/" + b);
        }
        if (b.isEmpty()) {
            return a.startsWith("/") ? a : "/" + a;
        }
        String left = a.endsWith("/") ? a.substring(0, a.length() - 1) : a;
        String right = b.startsWith("/") ? b : "/" + b;
        String joined = left + right;
        return joined.startsWith("/") ? joined : "/" + joined;
    }

    private static String simplifyType(String type) {
        String t = type.replaceAll("\\s+", " ").trim();
        int lastDot = t.lastIndexOf('.');
        if (lastDot >= 0 && !t.contains("<")) {
            return t.substring(lastDot + 1);
        }
        return t.replace("java.util.", "").replace("java.lang.", "");
    }

    private record MethodMeta(
            String name,
            String returnType,
            String requestBodyType,
            List<String> pathParams,
            List<String> queryParams,
            boolean requiresAuth
    ) {
    }
}
