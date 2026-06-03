package com.aifinance.model;

import java.util.List;

public record AgentDefinition(
        String id,
        String displayName,
        String description,
        String vertical,
        String verticalColor,
        String systemPrompt,
        List<String> skills,
        String steeringExample,
        String model,
        List<String> requiredMcpServers
) {}
