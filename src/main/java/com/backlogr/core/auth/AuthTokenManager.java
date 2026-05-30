package com.backlogr.core.auth;

import com.backlogr.domain.entities.user.UserIntegration;
import com.backlogr.domain.model.AuthTokens;
import com.backlogr.services.core.ProviderService;
import com.backlogr.common.Result;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class AuthTokenManager {

    /**
     * Returns valid tokens for the given integration, refreshing them via the provider if expired or expiring within 5 minutes.
     */
    public Result<AuthTokens> resolveToken(UserIntegration userIntegration, ProviderService providerService) {
        boolean tokenExpiredOrExpiringSoon = userIntegration.tokenExpiry == null
                || userIntegration.tokenExpiry.isBefore(Instant.now().plusSeconds(300));

        if (!tokenExpiredOrExpiringSoon) {
            return Result.ok(new AuthTokens(
                    userIntegration.accessToken,
                    userIntegration.refreshToken,
                    userIntegration.tokenExpiry
            ));
        }

        return providerService.refreshToken(userIntegration.refreshToken);
    }
}
