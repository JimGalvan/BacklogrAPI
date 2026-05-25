package com.backlogr.integration;

import com.backlogr.enums.Provider;
import com.backlogr.shared.Result;

public interface ProviderService {

    boolean supports(Provider provider);

    Provider getProvider();

    Result<OAuthTokens> refreshToken(String refreshToken);

    Result<TicketData> fetch(String key, String cloudId, String accessToken);
}
