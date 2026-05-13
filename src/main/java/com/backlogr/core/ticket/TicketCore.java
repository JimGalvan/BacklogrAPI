package com.backlogr.core.ticket;

import com.backlogr.core.ticket.TicketUrlParser.ParsedTicketUrl;
import com.backlogr.domain.ticket.Ticket;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketResponse;
import com.backlogr.integration.ExternalTicketClient;
import com.backlogr.integration.ExternalTicketData;
import com.backlogr.repository.ticket.TicketRepository;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TicketCore {

    @Inject
    TicketRepository ticketRepository;

    @Inject
    Instance<ExternalTicketClient> clients;

    @Transactional
    public Result<TicketResponse> importTicket(TicketImportRequest request) {
        ParsedTicketUrl parsed = TicketUrlParser.parse(request.url()).orElse(null);

        if (parsed == null) {
            return Result.badRequest("Unrecognised tracker URL. Supported: Jira, GitHub, Linear, Trello.");
        }

        ExternalTicketClient client = clients.stream()
                .filter(currentClient -> currentClient.supports(parsed.source()))
                .findFirst()
                .orElse(null);

        if (client == null) {
            return Result.badRequest("No integration available for " + parsed.source() + ". Currently supported: JIRA.");
        }

        if (ticketRepository.existsByExternalIdAndSource(parsed.key(), parsed.source())) {
            return Result.conflict("Ticket " + parsed.key() + " has already been imported.");
        }

        Result<ExternalTicketData> fetchResult = client.fetch(parsed.key());
        if (!fetchResult.isSuccess()) {
            return Result.internalError(fetchResult.getMessage());
        }

        Ticket ticket = toEntity(fetchResult.getValue(), parsed, request.url());
        ticketRepository.persist(ticket);

        return Result.ok(toResponse(ticket));
    }

    private Ticket toEntity(ExternalTicketData data, ParsedTicketUrl parsed, String url) {
        Ticket ticket = new Ticket();
        ticket.externalId   = parsed.key();
        ticket.url          = url;
        ticket.source       = parsed.source();
        ticket.title        = data.title();
        ticket.description  = data.description();
        ticket.status       = data.status();
        ticket.priority     = data.priority();
        ticket.assignee     = data.assignee();
        ticket.storyPoints  = data.storyPoints();
        ticket.tags         = data.tags();
        return ticket;
    }

    public static TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
            ticket.id,
            ticket.externalId,
            ticket.url,
            ticket.title,
            ticket.description,
            ticket.status,
            ticket.priority,
            ticket.source,
            ticket.assignee,
            ticket.storyPoints,
            ticket.tags,
            ticket.createdAt,
            ticket.lastModifiedAt
        );
    }
}
