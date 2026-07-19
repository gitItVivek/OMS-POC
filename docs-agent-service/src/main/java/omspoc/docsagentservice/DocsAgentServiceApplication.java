package omspoc.docsagentservice;

import omspoc.docsagentservice.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocsAgentServiceApplication {

    public static void main(String[] args) {
        EnvFileLoader.loadDefaultLocations();
        SpringApplication.run(DocsAgentServiceApplication.class, args);
    }
}
