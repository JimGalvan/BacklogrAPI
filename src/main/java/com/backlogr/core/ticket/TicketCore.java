package com.backlogr.core.ticket;

import com.backlogr.core.ticket.TicketUrlParser.ParsedTicketUrl;
import com.backlogr.domain.ticket.Ticket;
import com.backlogr.domain.user.UserIntegration;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketResponse;
import com.backlogr.enums.integration.IntegrationProvider;
import com.backlogr.enums.ticket.TicketSource;
import com.backlogr.integration.TicketClient;
import com.backlogr.integration.TicketData;
import com.backlogr.mapper.TicketMapper;
import com.backlogr.repository.ticket.TicketRepository;
import com.backlogr.repository.user.UserIntegrationRepository;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class TicketCore {

    @Inject
    TicketRepository ticketRepository;

    @Inject
    UserIntegrationRepository userIntegrationRepository;

    @Inject
    Instance<TicketClient> clients;

    @Inject
    TicketMapper ticketMapper;

    @Transactional
    public Result<TicketResponse> importTicket(UUID userId, TicketImportRequest request) {
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

        UserIntegration integration = resolveIntegration(userId, parsedTicketUrl.source());
        if (integration == null) {
            return Result.badRequest("No " + parsedTicketUrl.source() + " account connected. Please connect your account first.");
        }

        if (ticketRepository.existsByExternalIdAndSource(parsedTicketUrl.key(), parsedTicketUrl.source())) {
            return Result.conflict("Ticket " + parsedTicketUrl.key() + " has already been imported.");
        }

        Result<TicketData> fetchResult = client.fetch(parsedTicketUrl.key(), integration);
        if (!fetchResult.isSuccess()) {
            return Result.internalError(fetchResult.getMessage());
        }

        Ticket ticket = ticketMapper.toEntity(fetchResult.getValue(), parsedTicketUrl, request.url());
        ticketRepository.persist(ticket);

        return Result.ok(ticketMapper.toResponse(ticket));
    }

    private UserIntegration resolveIntegration(UUID userId, TicketSource source) {
        IntegrationProvider provider = switch (source) {
            case JIRA -> IntegrationProvider.JIRA;
            default   -> null;
        };
        if (provider == null) return null;
        return userIntegrationRepository.findByUserIdAndProvider(userId, provider).orElse(null);
    }
}
