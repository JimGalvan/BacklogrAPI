package com.backlogr.integration;

import com.backlogr.enums.Provider;
import com.backlogr.integration.jira.JiraService;
import jakarta.annotation.PostConstruct;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@Startup
@ApplicationScoped
public class ProviderFactory {

    @Inject
    JiraService jiraClient;

    private static ProviderFactory instance;

    @PostConstruct
    void init() {
        instance = this;
    }

    public static Optional<ProviderService> build(Provider provider) {

        return switch (provider) {
            case JIRA -> Optional.of(instance.jiraClient);
            default -> Optional.empty();
        };
    }
}
