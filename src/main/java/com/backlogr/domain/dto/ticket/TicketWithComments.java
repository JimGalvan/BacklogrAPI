package com.backlogr.domain.dto.ticket;

import java.util.List;

public record TicketWithComments(
    TicketAggregateResponse ticket,
    List<TicketCommentResponse> comments
) {}
