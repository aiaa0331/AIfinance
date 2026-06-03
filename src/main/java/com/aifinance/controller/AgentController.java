package com.aifinance.controller;

import com.aifinance.model.AgentDefinition;
import com.aifinance.model.ChatRequest;
import com.aifinance.service.AgentCatalog;
import com.aifinance.service.AnthropicService;
import com.aifinance.service.OpenAICompatService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AgentController {

    private final AgentCatalog catalog;
    private final AnthropicService anthropicService;
    private final OpenAICompatService openAICompatService;

    public AgentController(AgentCatalog catalog, AnthropicService anthropicService,
                           OpenAICompatService openAICompatService) {
        this.catalog = catalog;
        this.anthropicService = anthropicService;
        this.openAICompatService = openAICompatService;
    }

    @GetMapping("/agents")
    public List<AgentDefinition> listAgents() {
        return catalog.findAll();
    }

    @GetMapping("/agents/{id}")
    public AgentDefinition getAgent(@PathVariable String id) {
        return catalog.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatRequest request) {
        String model = request.model() != null && !request.model().isBlank()
                ? request.model() : "claude-opus-4-8";

        var baseAgent = catalog.findById(request.agentId())
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + request.agentId()));

        // Rebuild with the requested model
        var agent = new AgentDefinition(
                baseAgent.id(), baseAgent.displayName(), baseAgent.description(),
                baseAgent.descriptionZh(), baseAgent.vertical(), baseAgent.verticalColor(),
                baseAgent.systemPrompt(), baseAgent.skills(), baseAgent.steeringExample(),
                model, baseAgent.requiredMcpServers()
        );

        Flux<String> stream = model.startsWith("claude-")
                ? anthropicService.streamChat(agent, request.message(), request.apiKey())
                : openAICompatService.streamChat(agent, request.message(), model);

        return stream.map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }

    @GetMapping("/health")
    public Map<String, Object> health(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey) {
        return Map.of(
                "status", "ok",
                "apiKeyConfigured", anthropicService.isApiKeyConfigured(apiKey),
                "agentCount", catalog.findAll().size()
        );
    }
}
