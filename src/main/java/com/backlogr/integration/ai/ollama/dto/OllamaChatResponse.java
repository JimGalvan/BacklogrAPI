package com.backlogr.integration.ai.ollama.dto;

public record OllamaChatResponse(String model, OllamaMessage message, boolean done) {}
