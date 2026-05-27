package com.backlogr.integration.ai;

import com.backlogr.enums.AiModelProvider;
import com.backlogr.integration.ai.ollama.OllamaAiService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@Startup
@ApplicationScoped
public class AiServiceFactory {

    @Inject
    OllamaAiService ollamaAiService;

    private static AiServiceFactory instance;

    @PostConstruct
    void init() {
        instance = this;
    }

    public static Optional<AiService> build(AiModelProvider provider) {
        return switch (provider) {
            case OLLAMA -> Optional.of(instance.ollamaAiService);
            case OPENAI -> Optional.empty();
        };
    }
}
