package com.backlogr.domain.dto.ollama;

import java.util.List;

public record OllamaChatRequest(String model, List<OllamaMessage> messages, boolean stream) {}
