package omspoc.docsagentservice.web;

import omspoc.docsagentservice.agent.ApiDocsAgent;
import omspoc.docsagentservice.inventory.ControllerInventoryScanner;
import omspoc.docsagentservice.inventory.EndpointModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docs")
public class DocsAgentController {

    private final ApiDocsAgent apiDocsAgent;
    private final ControllerInventoryScanner scanner;

    public DocsAgentController(ApiDocsAgent apiDocsAgent, ControllerInventoryScanner scanner) {
        this.apiDocsAgent = apiDocsAgent;
        this.scanner = scanner;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate() {
        String summary = apiDocsAgent.generateAll();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("summary", summary);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/inventory")
    public List<EndpointModel> inventory() {
        return scanner.scanAll();
    }
}
