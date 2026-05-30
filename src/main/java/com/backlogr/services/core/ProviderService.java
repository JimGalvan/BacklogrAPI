package com.backlogr.services.core;

import com.backlogr.domain.enums.Provider;
import com.backlogr.common.Result;
import com.backlogr.domain.model.AuthTokens;
import com.backlogr.domain.model.TicketComment;
import com.backlogr.domain.model.TicketData;

import java.util.List;

public interface ProviderService {

    boolean supports(Provider provider);

    Provider getProvider();

    Result<AuthTokens> refreshToken(String refreshToken);

    Result<TicketData> fetch(String key, String cloudId, String accessToken);

    Result<List<TicketComment>> fetchComments(String key, String cloudId, String accessToken);
}
