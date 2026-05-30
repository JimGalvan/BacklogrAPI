package com.backlogr.domain.dto.ollama;

public record OllamaChatResponse(String model, OllamaMessage message, boolean done) {}
