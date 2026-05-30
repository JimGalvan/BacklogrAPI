package com.backlogr.core.auth;

import com.backlogr.common.auth.ExternalAuthConstants;
import com.backlogr.services.jira.JiraOAuthService;
import com.backlogr.services.jira.JiraOAuthStateService;
import com.backlogr.common.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ExternalAuthCore {

    @Inject
    JiraOAuthStateService stateService;

    @Inject
    JiraOAuthService jiraOAuthService;

    @Inject
    @ConfigProperty(name = "jira.oauth.client-id")
    String clientId;

    @Inject
    @ConfigProperty(name = "jira.oauth.redirect-uri")
    String redirectUri;

    public Result<URI> buildAuthorizationUrl(UUID userId, String provider) {
        if (!"jira".equals(provider)) {
            return Result.notFound("Unsupported provider: " + provider);
        }
        String state = stateService.generateState(userId);
        String url = ExternalAuthConstants.ATLASSIAN_AUTH_URL
                + "?audience=" + encode("api.atlassian.com")
                + "&client_id=" + encode(clientId)
                + "&scope=" + encode(ExternalAuthConstants.JIRA_SCOPES)
                + "&redirect_uri=" + encode(redirectUri)
                + "&state=" + encode(state)
                + "&response_type=code"
                + "&prompt=consent";
        return Result.ok(URI.create(url));
    }

    public Result<String> handleCallback(String provider, String code, String state) {
        if (!"jira".equals(provider)) {
            return Result.notFound("Unsupported provider: " + provider);
        }
        if (state == null || state.isBlank()) {
            return Result.badRequest("Missing state parameter.");
        }
        Optional<UUID> userId = stateService.validateAndConsume(state);
        if (userId.isEmpty()) {
            return Result.badRequest("Invalid or expired state parameter.");
        }
        return jiraOAuthService.completeOAuth(userId.get(), code);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
