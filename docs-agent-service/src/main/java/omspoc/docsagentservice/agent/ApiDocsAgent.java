package omspoc.docsagentservice.agent;

import omspoc.docsagentservice.config.DocsAgentProperties;
import omspoc.docsagentservice.tools.DocsAgentTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ApiDocsAgent {

    private static final Logger log = LoggerFactory.getLogger(ApiDocsAgent.class);

    private static final String SYSTEM_PROMPT = """
            You are the OMS-POC API documentation agent.
            Your job is to regenerate compelling, accurate Markdown API docs under docs/api.

            Hard rules:
            1. Always call getEndpointInventory (and summarizeInventory if useful) before writing docs.
            2. Document ONLY endpoints returned by the inventory tools. Never invent endpoints, paths, or fields.
            3. Prefer updating existing docs: call readExistingDoc first when a file exists.
            4. You MUST call writeDocFile for README.md and for every service listed by listServices.
            5. Use placeholders BASE_URL and TOKEN in curl examples. Never invent real hosts/secrets.
            6. For request bodies, infer field names only from DTO/source via readSourceSnippet when needed.
            7. Write readable docs: short overview, endpoint table, then per-endpoint sections with method, path,
               auth notes, sample JSON, and curl.
            8. Highlight that integration-service POST /api/place-order is the customer entry for placing orders,
               and identity-service owns JWT auth (/api/auth/*).
            """;

    private final ChatClient chatClient;
    private final DocsAgentProperties properties;
    private final DeterministicApiDocsWriter deterministicWriter;

    public ApiDocsAgent(
            ChatClient.Builder chatClientBuilder,
            DocsAgentTools tools,
            DocsAgentProperties properties,
            DeterministicApiDocsWriter deterministicWriter) {
        this.properties = properties;
        this.deterministicWriter = deterministicWriter;
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(tools)
                .build();
    }

    public String generateAll() {
        String services = String.join(", ", properties.getServices());
        String userPrompt = """
                Regenerate the full OMS API documentation set now.
                Services: %s
                Output directory: %s
                Write README.md as an index with auth + place-order quickstart, then one markdown file per service
                named exactly {service}.md (e.g. order-service.md).
                When finished, reply with a short summary of files written and endpoint counts.
                """.formatted(services, properties.getOutputDir());

        log.info("Starting API docs generation for services: {}", services);
        try {
            String result = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();
            log.info("API docs generation finished via Spring AI / OpenAI");
            return result == null ? "" : result;
        } catch (Exception llmFailure) {
            log.warn("LLM docs generation failed ({}). Falling back to deterministic inventory docs.",
                    llmFailure.getMessage());
            return deterministicWriter.writeAll();
        }
    }
}
