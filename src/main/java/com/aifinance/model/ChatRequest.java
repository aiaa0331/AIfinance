package com.aifinance.model;

public record ChatRequest(String agentId, String message, String apiKey, String model) {}
