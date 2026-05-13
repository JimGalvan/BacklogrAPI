package com.backlogr.integration;

import com.backlogr.enums.ticket.TicketSource;
import com.backlogr.shared.Result;

public interface ExternalTicketClient {

    boolean supports(TicketSource source);

    Result<ExternalTicketData> fetch(String key);
}
