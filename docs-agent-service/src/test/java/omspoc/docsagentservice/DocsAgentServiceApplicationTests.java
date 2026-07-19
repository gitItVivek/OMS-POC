package omspoc.docsagentservice;

import omspoc.docsagentservice.config.DocsAgentProperties;
import omspoc.docsagentservice.inventory.ControllerInventoryScanner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-key-not-used",
        "docs.agent.run-on-startup=false",
        "spring.ai.mcp.server.enabled=false"
})
class DocsAgentServiceApplicationTests {

    @DynamicPropertySource
    static void repoRoot(DynamicPropertyRegistry registry) {
        registry.add("docs.agent.repo-root", () -> System.getProperty("user.dir") + "/..");
    }

    @Autowired
    private ControllerInventoryScanner scanner;

    @Autowired
    private DocsAgentProperties properties;

    @Test
    void contextLoadsAndInventoryFindsControllers() {
        assertThat(properties.getServices()).isNotEmpty();
        var endpoints = scanner.scanAll();
        assertThat(endpoints).isNotEmpty();
        assertThat(endpoints.stream().map(e -> e.service()).distinct().toList())
                .contains("order-service", "integration-service", "identity-service");
    }
}
