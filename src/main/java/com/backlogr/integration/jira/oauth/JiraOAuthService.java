package com.backlogr.integration.jira.oauth;

import com.backlogr.domain.user.User;
import com.backlogr.domain.user.UserIntegration;
import com.backlogr.enums.integration.IntegrationProvider;
import com.backlogr.integration.jira.oauth.client.AtlassianAuthClient;
import com.backlogr.integration.jira.oauth.client.AtlassianResourceClient;
import com.backlogr.integration.jira.oauth.dto.AtlassianResource;
import com.backlogr.integration.jira.oauth.dto.AtlassianTokenResponse;
import com.backlogr.integration.jira.oauth.dto.TokenExchangeRequest;
import com.backlogr.repository.user.UserIntegrationRepository;
import com.backlogr.repository.user.UserRepository;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JiraOAuthService {

    @Inject @RestClient
    AtlassianAuthClient authClient;

    @Inject @RestClient
    AtlassianResourceClient resourceClient;

    @Inject
    UserRepository userRepository;

    @Inject
    UserIntegrationRepository userIntegrationRepository;

    @Inject
    @ConfigProperty(name = "jira.oauth.client-id")
    String clientId;

    @Inject
    @ConfigProperty(name = "jira.oauth.client-secret")
    String clientSecret;

    @Inject
    @ConfigProperty(name = "jira.oauth.redirect-uri")
    String redirectUri;

    @Transactional
    public Result<String> completeOAuth(UUID userId, String code) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Result.notFound("User not found.");
        }

        AtlassianTokenResponse tokens;
        try {
            tokens = authClient.exchangeCode(new TokenExchangeRequest(
                    "authorization_code", clientId, clientSecret, code, redirectUri
            ));
        } catch (Exception e) {
            return Result.internalError("Token exchange failed: " + e.getMessage());
        }

        List<AtlassianResource> resources;
        try {
            resources = resourceClient.getAccessibleResources("Bearer " + tokens.accessToken());
        } catch (Exception e) {
            return Result.internalError("Failed to retrieve accessible resources: " + e.getMessage());
        }

        if (resources.isEmpty()) {
            return Result.badRequest("No accessible Jira resources found for this account.");
        }

        AtlassianResource resource = resources.getFirst();

        UserIntegration integration = userIntegrationRepository
                .findByUserAndProvider(user, IntegrationProvider.JIRA)
                .orElse(null);

        boolean isNew = integration == null;
        if (isNew) {
            integration = new UserIntegration();
            integration.user = user;
            integration.provider = IntegrationProvider.JIRA;
        }

        integration.accessToken = tokens.accessToken();
        integration.refreshToken = tokens.refreshToken();
        integration.cloudId = resource.id();
        integration.tokenExpiry = Instant.now().plusSeconds(tokens.expiresIn());

        if (isNew) {
            userIntegrationRepository.persist(integration);
        }

        return Result.ok("Jira connected successfully.");
    }
}
