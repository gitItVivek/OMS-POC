package omspoc.docsagentservice.inventory;

import java.util.List;

public record EndpointModel(
        String service,
        String controllerClass,
        String controllerFile,
        String javaMethodName,
        String httpMethod,
        String path,
        String requestBodyType,
        String returnType,
        List<String> pathParams,
        List<String> queryParams,
        boolean requiresAuthHint
) {
}
