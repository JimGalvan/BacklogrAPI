package com.backlogr.domain.model;

import com.backlogr.domain.enums.ticket.TicketPriority;
import com.backlogr.domain.enums.ticket.TicketStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

public record TicketData(
    String key,
    String title,
    JsonNode description,
    TicketStatus status,
    TicketPriority priority,
    String assignee,
    Integer storyPoints,
    List<String> tags,
    Instant externalCreatedAt
) {}
