package omspoc.docsagentservice.cli;

import omspoc.docsagentservice.agent.ApiDocsAgent;
import omspoc.docsagentservice.config.DocsAgentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DocsAgentRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DocsAgentRunner.class);

    private final DocsAgentProperties properties;
    private final ApiDocsAgent apiDocsAgent;
    private final ConfigurableApplicationContext context;

    public DocsAgentRunner(
            DocsAgentProperties properties,
            ApiDocsAgent apiDocsAgent,
            ConfigurableApplicationContext context) {
        this.properties = properties;
        this.apiDocsAgent = apiDocsAgent;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isRunOnStartup()) {
            return;
        }
        try {
            String summary = apiDocsAgent.generateAll();
            log.info("Docs agent one-shot summary: {}", summary);
            if (properties.isExitAfterRun()) {
                int code = SpringApplication.exit(context, () -> 0);
                System.exit(code);
            }
        } catch (Exception e) {
            log.error("Docs agent one-shot failed", e);
            if (properties.isExitAfterRun()) {
                int code = SpringApplication.exit(context, () -> 1);
                System.exit(code);
            }
            throw e;
        }
    }
}
