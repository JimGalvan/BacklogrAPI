package com.backlogr.core.ticket;

import com.backlogr.core.BaseCore;
import com.backlogr.core.ticket.TicketUrlParser.ParsedTicketUrl;
import com.backlogr.domain.ticket.Ticket;
import com.backlogr.domain.user.UserIntegration;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketResponse;
import com.backlogr.integration.OAuthTokens;
import com.backlogr.integration.TicketData;
import com.backlogr.integration.TicketIntegration;
import com.backlogr.integration.IntegrationFactory;
import com.backlogr.mapper.TicketMapper;
import com.backlogr.repository.ticket.TicketRepository;
import com.backlogr.repository.user.UserIntegrationRepository;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TicketCore extends BaseCore {

    @Inject
    TicketRepository ticketRepository;

    @Inject
    UserIntegrationRepository userIntegrationRepository;

    @Inject
    IntegrationFactory integrationFactory;

    @Inject
    TicketMapper ticketMapper;

    @Transactional
    public Result<TicketResponse> importTicket(UUID userId, UUID workspaceId, TicketImportRequest request) {
        Result<Void> guard = requireWorkspaceMember(userId, workspaceId);
        if (!guard.isSuccess()) return guard.asError();

        ParsedTicketUrl parsed = TicketUrlParser.parse(request.url()).orElse(null);
        if (parsed == null) {
            return Result.badRequest("Unrecognised tracker URL. Supported: Jira.");
        }

        if (ticketRepository.existsByTicketKeyAndWorkspaceId(parsed.getKey(), workspaceId)) {
            return Result.conflict("Ticket " + parsed.getKey() + " has already been imported into this workspace.");
        }

        TicketIntegration ticketIntegration = integrationFactory.build(parsed.getProvider()).orElse(null);
        if (ticketIntegration == null) {
            return Result.badRequest("No integration available for source: " + parsed.getProvider());
        }

        UserIntegration userIntegration = userIntegrationRepository
                .findByUserIdAndProvider(userId, ticketIntegration.getProvider())
                .orElse(null);

        if (userIntegration == null) {
            return Result.badRequest("No " + parsed.getProvider() + " account connected. Please connect your account first.");
        }

        boolean tokenExpiredOrExpiringSoon = userIntegration.tokenExpiry == null
                || userIntegration.tokenExpiry.isBefore(Instant.now().plusSeconds(300));

        if (tokenExpiredOrExpiringSoon) {
            Result<OAuthTokens> refresh = ticketIntegration.refreshToken(userIntegration.refreshToken);
            if (!refresh.isSuccess()) {
                return Result.badRequest(parsed.getProvider() + " session expired. Please reconnect your account.");
            }
            OAuthTokens newTokens = refresh.getValue();
            userIntegration.accessToken = newTokens.accessToken();
            userIntegration.refreshToken = newTokens.refreshToken();
            userIntegration.tokenExpiry = newTokens.tokenExpiry();
        }

        Result<TicketData> fetchResult = ticketIntegration.fetch(parsed.getKey(), userIntegration.cloudId, userIntegration.accessToken);
        if (!fetchResult.isSuccess()) {
            return Result.internalError(fetchResult.getMessage());
        }

        TicketData data = fetchResult.getValue();
        Ticket ticket = new Ticket();
        ticket.ticketKey = parsed.getKey();
        ticket.workspaceId = workspaceId;
        ticket.importedBy = userId;
        ticket.projectKey = parsed.getKey().split("-")[0];
        ticket.summary = data.title();
        ticket.externalCreatedAt = data.externalCreatedAt();
        ticket.provider = parsed.getProvider();
        ticketRepository.persist(ticket);

        return Result.created(ticketMapper.toResponse(ticket));
    }

    public Result<TicketResponse> getTicket(UUID userId, UUID workspaceId, String ticketKey) {
        Result<Void> guard = requireWorkspaceMember(userId, workspaceId);
        if (!guard.isSuccess()) return guard.asError();

        Result<Ticket> ticketResult = resolveTicket(ticketKey, workspaceId);
        if (!ticketResult.isSuccess()) return ticketResult.asError();

        return Result.ok(ticketMapper.toResponse(ticketResult.getValue()));
    }

    public Result<List<TicketResponse>> getTickets(UUID userId, UUID workspaceId, boolean mine) {
        Result<Void> guard = requireWorkspaceMember(userId, workspaceId);
        if (!guard.isSuccess()) return guard.asError();

        List<Ticket> tickets = mine
                ? ticketRepository.findByWorkspaceIdAndImportedBy(workspaceId, userId)
                : ticketRepository.findByWorkspaceId(workspaceId);

        return Result.ok(ticketMapper.toResponseList(tickets));
    }

    @Transactional
    public Result<Void> deleteTicket(UUID userId, UUID workspaceId, String ticketKey) {
        Result<Void> guard = requireWorkspaceMember(userId, workspaceId);
        if (!guard.isSuccess()) return guard.asError();

        Result<Ticket> ticketResult = resolveTicket(ticketKey, workspaceId);
        if (!ticketResult.isSuccess()) return ticketResult.asError();

        ticketRepository.delete(ticketResult.getValue());
        return Result.ok(null);
    }

    private Result<Ticket> resolveTicket(String ticketKey, UUID workspaceId) {
        return ticketRepository.findByTicketKeyAndWorkspaceId(ticketKey, workspaceId)
                .map(Result::ok)
                .orElseGet(() -> Result.notFound("Ticket " + ticketKey + " not found in this workspace."));
    }
}
