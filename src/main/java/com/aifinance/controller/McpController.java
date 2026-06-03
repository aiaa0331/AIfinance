package com.aifinance.controller;

import com.aifinance.model.McpServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private static final Path CLAUDE_JSON = Path.of(System.getProperty("user.home"), ".claude.json");
    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public List<McpServer> listServers() {
        if (!Files.exists(CLAUDE_JSON)) return List.of();
        try {
            JsonNode root = mapper.readTree(CLAUDE_JSON.toFile());
            JsonNode mcps = root.path("mcpServers");
            if (mcps.isMissingNode()) return List.of();

            List<McpServer> result = new ArrayList<>();
            mcps.fields().forEachRemaining(e -> {
                String name = e.getKey();
                JsonNode cfg = e.getValue();
                String type = cfg.has("url") ? "http" : "stdio";
                String command = cfg.has("command") ? cfg.get("command").asText()
                        : cfg.has("url") ? cfg.get("url").asText() : "";
                List<String> args = new ArrayList<>();
                if (cfg.has("args")) cfg.get("args").forEach(a -> args.add(a.asText()));
                boolean running = checkRunning(name, command);
                result.add(new McpServer(name, type, command, args, running));
            });
            result.sort(Comparator.comparing(McpServer::name));
            return result;
        } catch (IOException e) {
            return List.of();
        }
    }

    private boolean checkRunning(String name, String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("pgrep", "-f", name);
            return pb.start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
