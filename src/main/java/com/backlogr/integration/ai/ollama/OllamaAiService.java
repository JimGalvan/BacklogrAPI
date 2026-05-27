package com.backlogr.integration.ai.ollama;

import com.backlogr.enums.AiModelProvider;
import com.backlogr.integration.ai.AiMessage;
import com.backlogr.integration.ai.AiService;
import com.backlogr.integration.ai.ollama.client.OllamaHttpClient;
import com.backlogr.integration.ai.ollama.dto.OllamaChatRequest;
import com.backlogr.integration.ai.ollama.dto.OllamaMessage;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class OllamaAiService implements AiService {

    @ConfigProperty(name = "ollama.model", defaultValue = "llama3.2")
    String defaultModel;

    @Inject
    @RestClient
    OllamaHttpClient ollamaHttpClient;

    @Override
    public boolean supports(AiModelProvider provider) {
        return provider == AiModelProvider.OLLAMA;
    }

    @Override
    public AiModelProvider getProvider() {
        return AiModelProvider.OLLAMA;
    }

    @Override
    @Retry(maxRetries = 2, delay = 1000)
    @Timeout(value = 90, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 0.6)
    @Fallback(fallbackMethod = "fallbackAsk")
    public Uni<String> ask(List<AiMessage> messages) {
        return ollamaHttpClient
                .chat(buildRequest(messages, false))
                .map(response -> response.message().content());
    }

    @Override
    public Multi<String> stream(List<AiMessage> messages) {
        return ollamaHttpClient
                .streamChat(buildRequest(messages, true))
                .filter(chunk -> !chunk.done() && chunk.message() != null)
                .map(chunk -> chunk.message().content());
    }

    public Uni<String> fallbackAsk(List<AiMessage> messages) {
        return Uni.createFrom().item("AI service is currently unavailable. Please try again later.");
    }

    private OllamaChatRequest buildRequest(List<AiMessage> messages, boolean stream) {
        List<OllamaMessage> ollamaMessages = messages.stream()
                .map(message -> new OllamaMessage(message.role(), message.content()))
                .toList();

        return new OllamaChatRequest(defaultModel, ollamaMessages, stream);
    }
}
