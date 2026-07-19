package omspoc.docsagentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "docs.agent")
public class DocsAgentProperties {

    private String repoRoot = ".";
    private String outputDir = "docs/api";
    private List<String> services = new ArrayList<>();
    private boolean runOnStartup = false;
    private boolean exitAfterRun = true;

    public String getRepoRoot() {
        return repoRoot;
    }

    public void setRepoRoot(String repoRoot) {
        this.repoRoot = repoRoot;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public boolean isRunOnStartup() {
        return runOnStartup;
    }

    public void setRunOnStartup(boolean runOnStartup) {
        this.runOnStartup = runOnStartup;
    }

    public boolean isExitAfterRun() {
        return exitAfterRun;
    }

    public void setExitAfterRun(boolean exitAfterRun) {
        this.exitAfterRun = exitAfterRun;
    }
}
