package com.backlogr.services.ai.ollama;

import com.backlogr.domain.enums.AiModelProvider;
import com.backlogr.domain.ai.AiMessage;
import com.backlogr.services.ai.AiService;
import com.backlogr.domain.dto.ollama.OllamaChatRequest;
import com.backlogr.domain.dto.ollama.OllamaChatResponse;
import com.backlogr.domain.dto.ollama.OllamaMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.jboss.logging.Logger;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class OllamaAiService implements AiService {

    private static final Logger logger = Logger.getLogger(OllamaAiService.class);

    @ConfigProperty(name = "ollama.model", defaultValue = "llama3.2")
    String defaultModel;

    @Inject
    @RestClient
    OllamaHttpClient ollamaHttpClient;

    @Inject
    ObjectMapper objectMapper;

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
        // Quarkus hands us raw HTTP body chunks — not one NDJSON line per item.
        // A single chunk may contain several complete JSON objects separated by \n,
        // and the first fragment may be the tail end of the previous chunk's last line.
        // We buffer the incomplete tail and process only complete lines.
        StringBuilder[] lineBuffer = { new StringBuilder() };

        return ollamaHttpClient
                .streamChat(buildRequest(messages, true))
                .flatMap(chunk -> {
                    lineBuffer[0].append(chunk);

                    String[] parts = lineBuffer[0].toString().split("\n", -1);

                    // The last part may be an incomplete line — keep it in the buffer.
                    lineBuffer[0].setLength(0);
                    lineBuffer[0].append(parts[parts.length - 1]);

                    // Collect tokens from every complete line.
                    List<String> tokens = new ArrayList<>();
                    for (int index = 0; index < parts.length - 1; index++) {
                        String token = parseJsonLine(parts[index]);
                        if (token != null && !token.isBlank()) {
                            tokens.add(token);
                        }
                    }

                    return Multi.createFrom().iterable(tokens);
                });
    }

    private String parseJsonLine(String line) {
        if (line == null || line.isBlank()) return null;
        try {
            OllamaChatResponse chunk = objectMapper.readValue(line, OllamaChatResponse.class);
            if (chunk.done() || chunk.message() == null) return null;
            return chunk.message().content();
        } catch (Exception exception) {
            logger.warnf("Skipping unparseable Ollama line: %s", line);
            return null;
        }
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
