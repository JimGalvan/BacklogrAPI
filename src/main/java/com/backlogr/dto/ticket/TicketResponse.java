package com.backlogr.dto.ticket;

import com.backlogr.enums.Provider;

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
    Instant createdAt,
    Instant importedAt
) {}
