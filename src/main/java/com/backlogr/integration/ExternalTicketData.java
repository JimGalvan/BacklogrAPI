package com.backlogr.integration;

import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketStatus;

import java.util.List;

public record ExternalTicketData(
    String key,
    String title,
    String description,
    TicketStatus status,
    TicketPriority priority,
    String assignee,
    Integer storyPoints,
    List<String> tags
) {}
