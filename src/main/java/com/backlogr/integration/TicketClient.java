package com.backlogr.integration;

import com.backlogr.enums.Provider;
import com.backlogr.shared.Result;

public interface TicketClient {

    boolean supports(Provider provider);

    Result<TicketData> fetch(String key, String cloudId, String accessToken);
}
