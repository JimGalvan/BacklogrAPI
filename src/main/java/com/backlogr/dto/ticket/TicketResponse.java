package com.backlogr.dto.ticket;

import com.backlogr.enums.Provider;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
    UUID id,
    String ticketKey,
    UUID workspaceId,
    UUID importedBy,
    String projectKey,
    String summary,
    Provider provider,
    JsonNode description,
    Instant createdAt,
    Instant importedAt
) {}
