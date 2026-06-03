package com.aifinance.controller;

import com.aifinance.model.AgencyAgent;
import com.aifinance.model.AgencyChatRequest;
import com.aifinance.service.AgencyAgentService;
import com.aifinance.service.AnthropicService;
import com.aifinance.model.AgentDefinition;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agency")
public class AgencyAgentController {

    private final AgencyAgentService agencyAgentService;
    private final AnthropicService anthropicService;

    public AgencyAgentController(AgencyAgentService agencyAgentService,
                                  AnthropicService anthropicService) {
        this.agencyAgentService = agencyAgentService;
        this.anthropicService = anthropicService;
    }

    @GetMapping("/agents")
    public List<AgencyAgent> listAgents() {
        return agencyAgentService.findAll();
    }

    @GetMapping("/agents/{category}/{name}")
    public AgencyAgent getAgent(@PathVariable String category, @PathVariable String name) {
        String id = category + "/" + name;
        return agencyAgentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody AgencyChatRequest request) {
        AgencyAgent agent = agencyAgentService.findById(request.agencyAgentId())
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + request.agencyAgentId()));

        // Wrap into AgentDefinition-compatible call via system prompt
        var agentDef = new AgentDefinition(
                agent.id(), agent.name(), agent.description(), agent.category(),
                agent.color(), agent.systemPrompt(), List.of(), "", "claude-sonnet-4-6", List.of()
        );

        return anthropicService.streamChat(agentDef, request.message(), request.apiKey())
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<AgencyAgent> all = agencyAgentService.findAll();
        Map<String, Long> byCategory = new java.util.LinkedHashMap<>();
        all.forEach(a -> byCategory.merge(a.categoryDisplayName(), 1L, Long::sum));
        return Map.of("total", all.size(), "byCategory", byCategory);
    }
}
