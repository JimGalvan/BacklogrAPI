package com.backlogr.integration;

import com.backlogr.domain.user.UserIntegration;
import com.backlogr.enums.ticket.TicketSource;
import com.backlogr.shared.Result;

public interface TicketClient {

    boolean supports(TicketSource source);

    Result<TicketData> fetch(String key, UserIntegration integration);
}
