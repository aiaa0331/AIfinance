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
public class OpenAICompatService {

    @Value("${ai.router.base-url:http://localhost:9000/v1}")
    private String baseUrl;

    @Value("${ai.router.api-key:sk-local}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public OpenAICompatService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    public Flux<String> streamChat(AgentDefinition agent, String userMessage, String model) {
        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        String resolvedModel = (model != null && !model.isBlank()) ? model : "deepseek-v4-pro";

        Map<String, Object> body = Map.of(
                "model", resolvedModel,
                "stream", true,
                "max_tokens", 4096,
                "messages", List.of(
                        Map.of("role", "system", "content", agent.systemPrompt()),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        String bodyJson;
        try {
            bodyJson = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            return Flux.error(new RuntimeException("Failed to serialize request", e));
        }

        return client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bodyJson)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data:") && !line.contains("[DONE]"))
                .map(line -> line.substring(5).trim())
                .flatMap(json -> {
                    try {
                        var node = objectMapper.readTree(json);
                        String text = node.path("choices").path(0)
                                .path("delta").path("content").asText("");
                        if (!text.isEmpty()) return Flux.just(text);
                        return Flux.empty();
                    } catch (Exception e) {
                        return Flux.empty();
                    }
                })
                .onErrorResume(e -> Flux.just("[ERROR] " + e.getMessage()));
    }
}
