package com.backlogr.core.ticket;

import com.backlogr.core.ticket.TicketUrlParser.ParsedTicketUrl;
import com.backlogr.domain.ticket.Ticket;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketResponse;
import com.backlogr.integration.TicketClient;
import com.backlogr.integration.TicketData;
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
    Instance<TicketClient> clients;

    @Transactional
    public Result<TicketResponse> importTicket(TicketImportRequest request) {
        ParsedTicketUrl parsedTicketUrl = TicketUrlParser.parse(request.url()).orElse(null);

        if (parsedTicketUrl == null) {
            return Result.badRequest("Unrecognised tracker URL. Supported: Jira, GitHub, Linear, Trello.");
        }

        TicketClient client = null;
        for (TicketClient ticketClient : clients) {
            if (ticketClient.supports(parsedTicketUrl.source())) {
                client = ticketClient;
                break;
            }
        }

        if (client == null) {
            return Result.badRequest("No integration available for " + parsedTicketUrl.source() + ". Currently supported: JIRA.");
        }

        if (ticketRepository.existsByExternalIdAndSource(parsedTicketUrl.key(), parsedTicketUrl.source())) {
            return Result.conflict("Ticket " + parsedTicketUrl.key() + " has already been imported.");
        }

        Result<TicketData> fetchResult = client.fetch(parsedTicketUrl.key());
        if (!fetchResult.isSuccess()) {
            return Result.internalError(fetchResult.getMessage());
        }

        Ticket ticket = toEntity(fetchResult.getValue(), parsedTicketUrl, request.url());
        ticketRepository.persist(ticket);

        return Result.ok(toResponse(ticket));
    }

    private Ticket toEntity(TicketData data, ParsedTicketUrl parsedTicketUrl, String url) {
        Ticket ticket = new Ticket();
        ticket.externalId   = parsedTicketUrl.key();
        ticket.url          = url;
        ticket.source       = parsedTicketUrl.source();
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
