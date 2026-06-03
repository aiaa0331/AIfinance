package com.aifinance.model;

public record HookEntry(String event, String matcher, String scriptName, String description) {}
