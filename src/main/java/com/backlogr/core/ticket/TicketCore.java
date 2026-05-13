package com.backlogr.core.ticket;

import com.backlogr.domain.ticket.Ticket;
import com.backlogr.repository.ticket.TicketRepository;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketImportResponse;
import com.backlogr.dto.ticket.TicketItemResponse;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TicketCore {

    @Inject
    TicketRepository ticketRepository;

    @Transactional
    public Result<TicketImportResponse> importTickets(TicketImportRequest request) {
        List<TicketItemResponse> imported = new ArrayList<>();
        int skipped = 0;

        for (var item : request.tickets()) {
            if (ticketRepository.existsByExternalIdAndSource(item.externalId(), request.source())) {
                skipped++;
                continue;
            }

            Ticket ticket = new Ticket();
            ticket.externalId = item.externalId();
            ticket.title = item.title();
            ticket.description = item.description();
            ticket.status = item.status();
            ticket.priority = item.priority();
            ticket.source = request.source();
            ticket.assignee = item.assignee();
            ticket.storyPoints = item.storyPoints();
            ticket.tags = item.tags() != null ? item.tags() : List.of();

            ticketRepository.persist(ticket);

            imported.add(toResponse(ticket));
        }

        return Result.ok(new TicketImportResponse(imported.size(), skipped, 0, imported));
    }

    private TicketItemResponse toResponse(Ticket ticket) {
        return new TicketItemResponse(
            ticket.id,
            ticket.externalId,
            ticket.title,
            ticket.status,
            ticket.priority,
            ticket.source,
            ticket.createdAt
        );
    }
}
