package omspoc.docsagentservice.mcp;

import org.springframework.context.annotation.Configuration;

/**
 * MCP exposure is provided by {@code spring-ai-starter-mcp-server-webmvc} with
 * {@code spring.ai.mcp.server.protocol=STREAMABLE}.
 * <p>
 * Tool methods live on {@link omspoc.docsagentservice.tools.DocsAgentTools}
 * ({@code @Tool}). The {@link org.springframework.ai.tool.ToolCallbackProvider}
 * bean in {@link omspoc.docsagentservice.config.DocsAgentConfiguration} is
 * converted into MCP tools when {@code tool-callback-converter=true}.
 */
@Configuration
public class DocsMcpConfiguration {
}
