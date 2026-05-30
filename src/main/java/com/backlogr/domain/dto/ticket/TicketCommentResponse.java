package com.backlogr.domain.dto.ticket;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record TicketCommentResponse(
    String id,
    String authorEmail,
    String authorName,
    JsonNode body,
    Instant createdAt,
    Instant updatedAt
) {}
