package com.aifinance.model;

import java.util.List;

public record McpServer(String name, String type, String command, List<String> args, boolean running) {}
