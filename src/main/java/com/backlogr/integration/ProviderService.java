package com.backlogr.integration;

import com.backlogr.enums.Provider;
import com.backlogr.shared.Result;

import java.util.List;

public interface ProviderService {

    boolean supports(Provider provider);

    Provider getProvider();

    Result<AuthTokens> refreshToken(String refreshToken);

    Result<TicketData> fetch(String key, String cloudId, String accessToken);

    Result<List<TicketComment>> fetchComments(String key, String cloudId, String accessToken);
}
