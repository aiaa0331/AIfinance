package com.aifinance.model;

public record AgencyAgent(
        String id,
        String name,
        String description,
        String emoji,
        String color,
        String vibe,
        String category,
        String categoryDisplayName,
        String systemPrompt
) {}
