package com.backlogr.integration;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record TicketComment(
    String id,
    String authorEmail,
    String authorName,
    JsonNode body,
    Instant createdAt,
    Instant updatedAt
) {}
