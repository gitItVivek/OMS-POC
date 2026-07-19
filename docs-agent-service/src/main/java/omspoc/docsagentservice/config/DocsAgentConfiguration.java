package omspoc.docsagentservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import omspoc.docsagentservice.tools.DocsAgentTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DocsAgentProperties.class)
public class DocsAgentConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    /**
     * Registers Spring AI tool callbacks so MCP server auto-config can expose them
     * via spring.ai.mcp.server.tool-callback-converter=true.
     */
    @Bean
    public ToolCallbackProvider docsAgentToolCallbackProvider(DocsAgentTools docsAgentTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(docsAgentTools)
                .build();
    }
}
