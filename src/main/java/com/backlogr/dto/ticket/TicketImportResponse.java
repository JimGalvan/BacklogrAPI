package com.backlogr.dto.ticket;

import java.util.List;

public record TicketImportResponse(
    int imported,
    int skipped,
    int failed,
    List<TicketItemResponse> tickets
) {}
