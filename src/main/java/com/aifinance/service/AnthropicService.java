package com.aifinance.service;

import com.aifinance.model.AgentDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class AnthropicService {

    private static final String ANTHROPIC_API_BASE = "https://api.anthropic.com";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    @Value("${anthropic.api-key:}")
    private String defaultApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AnthropicService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl(ANTHROPIC_API_BASE)
                .build();
        this.objectMapper = objectMapper;
    }

    public Flux<String> streamChat(AgentDefinition agent, String userMessage, String apiKey) {
        String key = (apiKey != null && !apiKey.isBlank()) ? apiKey : defaultApiKey;
        if (key == null || key.isBlank()) {
            return Flux.just("data: {\"error\":\"API key not configured\"}\n\n");
        }

        Map<String, Object> requestBody = Map.of(
                "model", "claude-opus-4-8",
                "max_tokens", 4096,
                "system", agent.systemPrompt(),
                "messages", List.of(Map.of("role", "user", "content", userMessage)),
                "stream", true
        );

        String bodyJson;
        try {
            bodyJson = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            return Flux.error(new RuntimeException("Failed to serialize request", e));
        }

        return webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", key)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bodyJson)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data: ") && !line.equals("data: [DONE]"))
                .map(line -> line.substring(6))
                .flatMap(json -> {
                    try {
                        var node = objectMapper.readTree(json);
                        String type = node.path("type").asText();
                        if ("content_block_delta".equals(type)) {
                            String text = node.path("delta").path("text").asText("");
                            if (!text.isEmpty()) {
                                return Flux.just(text);
                            }
                        } else if ("message_stop".equals(type)) {
                            return Flux.just("\n\n[DONE]");
                        } else if ("error".equals(type)) {
                            String errMsg = node.path("error").path("message").asText("Unknown error");
                            return Flux.just("[ERROR] " + errMsg);
                        }
                        return Flux.empty();
                    } catch (Exception e) {
                        return Flux.empty();
                    }
                })
                .onErrorResume(e -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("401")) {
                        return Flux.just("[ERROR] Invalid API key");
                    }
                    return Flux.just("[ERROR] " + (msg != null ? msg : "Connection failed"));
                });
    }

    public boolean isApiKeyConfigured(String requestApiKey) {
        String key = (requestApiKey != null && !requestApiKey.isBlank()) ? requestApiKey : defaultApiKey;
        return key != null && !key.isBlank();
    }
}
