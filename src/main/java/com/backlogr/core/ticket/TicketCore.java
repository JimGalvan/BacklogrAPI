package com.backlogr.core.ticket;

import com.backlogr.core.BaseCore;
import com.backlogr.core.auth.AuthTokenManager;
import com.backlogr.core.ticket.TicketUrlParser.ParsedTicketUrl;
import com.backlogr.domain.ticket.Ticket;
import com.backlogr.domain.user.UserIntegration;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketResponse;
import com.backlogr.enums.Provider;
import com.backlogr.integration.AuthTokens;
import com.backlogr.integration.TicketData;
import com.backlogr.integration.ProviderService;
import com.backlogr.integration.ProviderFactory;
import com.backlogr.mapper.TicketDataMapper;
import com.backlogr.mapper.TicketMapper;
import com.backlogr.repository.ticket.TicketRepository;
import com.backlogr.repository.user.UserIntegrationRepository;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.security.Principal;
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
    AuthTokenManager authTokenManager;

    @Inject
    TicketMapper ticketMapper;

    @Inject
    TicketDataMapper ticketDataMapper;
    @Inject
    Principal principal;

    @Transactional
    public Result<TicketResponse> importTicket(UUID userId, UUID workspaceId, TicketImportRequest request) {
        Result<Void> guard = requireWorkspaceMember(userId, workspaceId);
        if (!guard.isSuccess()) return guard.asError();

        ParsedTicketUrl parsed = TicketUrlParser.parse(request.url()).orElse(null);
        if (parsed == null) {
            return Result.badRequest("Unrecognised tracker URL. Supported: Jira.");
        }

        Provider ticketProvider = parsed.getProvider();
        if (ticketRepository.existsByTicketKeyAndWorkspaceId(parsed.getKey(), workspaceId)) {
            return Result.conflict("Ticket " + parsed.getKey() + " has already been imported into this workspace.");
        }

        ProviderService providerService = ProviderFactory.build(ticketProvider).orElse(null);
        if (providerService == null) {
            return Result.badRequest("No integration available for source: " + ticketProvider);
        }

        UserIntegration userIntegration = userIntegrationRepository
                .findByUserIdAndProvider(userId, providerService.getProvider())
                .orElse(null);

        if (userIntegration == null) {
            return Result.badRequest("No " + ticketProvider + " account connected. Please connect your account first.");
        }

        Result<AuthTokens> tokenResult = authTokenManager.resolveToken(userIntegration, providerService);
        if (!tokenResult.isSuccess()) {
            return Result.badRequest(ticketProvider + " session expired. Please reconnect your account.");
        }
        AuthTokens tokens = tokenResult.getValue();
        userIntegration.accessToken = tokens.accessToken();
        userIntegration.refreshToken = tokens.refreshToken();
        userIntegration.tokenExpiry = tokens.tokenExpiry();

        Result<TicketData> fetchResult = providerService.fetch(parsed.getKey(), userIntegration.cloudId, tokens.accessToken());
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
        ticket.provider = ticketProvider;
        ticketRepository.persist(ticket);

        return Result.created(ticketDataMapper.toResponse(ticket, data));
    }

    public Result<TicketResponse> getTicket(UUID userId, UUID workspaceId, String ticketKey) {
        Result<Void> guard = requireWorkspaceMember(userId, workspaceId);
        if (!guard.isSuccess()) return guard.asError();

        Result<Ticket> ticketResult = resolveTicket(ticketKey, workspaceId);
        if (!ticketResult.isSuccess()) return ticketResult.asError();

        Ticket ticket = ticketResult.getValue();

        ProviderService providerService = ProviderFactory.build(ticket.getProvider()).orElse(null);
        if (providerService == null) return Result.notFound("Provider client not found");

        UserIntegration userIntegration = userIntegrationRepository
                .findByUserIdAndProvider(userId, providerService.getProvider())
                .orElse(null);

        if (userIntegration == null) return Result.notFound("user integration client not found");

        Result<AuthTokens> tokenResult = authTokenManager.resolveToken(userIntegration, providerService);
        if (!tokenResult.isSuccess()) {
            return Result.badRequest(ticket.provider + " session expired. Please reconnect your account.");
        }
        AuthTokens tokens = tokenResult.getValue();
        userIntegration.accessToken = tokens.accessToken();
        userIntegration.refreshToken = tokens.refreshToken();
        userIntegration.tokenExpiry = tokens.tokenExpiry();

        Result<TicketData> dataResult = providerService.fetch(ticket.ticketKey, userIntegration.cloudId, tokens.accessToken());
        TicketData ticketData = dataResult.getValue();

        return Result.ok(ticketDataMapper.toResponse(ticket, ticketData));
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
