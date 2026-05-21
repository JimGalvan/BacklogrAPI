package com.backlogr.core.ticket;

import com.backlogr.core.ticket.TicketUrlParser.ParsedTicketUrl;
import com.backlogr.domain.ticket.Ticket;
import com.backlogr.domain.user.UserIntegration;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketResponse;
import com.backlogr.enums.integration.IntegrationProvider;
import com.backlogr.integration.TicketData;
import com.backlogr.integration.jira.JiraTicketClient;
import com.backlogr.integration.jira.oauth.JiraOAuthService;
import com.backlogr.integration.jira.oauth.dto.JiraTokens;
import com.backlogr.mapper.TicketMapper;
import com.backlogr.repository.ticket.TicketRepository;
import com.backlogr.repository.user.UserIntegrationRepository;
import com.backlogr.repository.workspace.WorkspaceMemberRepository;
import com.backlogr.repository.workspace.WorkspaceRepository;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TicketCore {

    @Inject
    TicketRepository ticketRepository;

    @Inject
    WorkspaceRepository workspaceRepository;

    @Inject
    WorkspaceMemberRepository workspaceMemberRepository;

    @Inject
    UserIntegrationRepository userIntegrationRepository;

    @Inject
    JiraTicketClient jiraTicketClient;

    @Inject
    JiraOAuthService jiraOAuthService;

    @Inject
    TicketMapper ticketMapper;

    @Transactional
    public Result<TicketResponse> importTicket(UUID userId, UUID workspaceId, TicketImportRequest request) {
        if (workspaceRepository.findByIdOptional(workspaceId).isEmpty()) {
            return Result.notFound("Workspace not found.");
        }
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            return Result.forbidden("You are not a member of this workspace.");
        }

        ParsedTicketUrl parsed = TicketUrlParser.parse(request.url()).orElse(null);
        if (parsed == null) {
            return Result.badRequest("Unrecognised tracker URL. Supported: Jira.");
        }

        if (ticketRepository.existsByTicketKeyAndWorkspaceId(parsed.key(), workspaceId)) {
            return Result.conflict("Ticket " + parsed.key() + " has already been imported into this workspace.");
        }

        UserIntegration integration = userIntegrationRepository
                .findByUserIdAndProvider(userId, IntegrationProvider.JIRA)
                .orElse(null);
        if (integration == null) {
            return Result.badRequest("No Jira account connected. Please connect your Jira account first.");
        }

        boolean tokenExpiredOrExpiringSoon = integration.tokenExpiry == null
                || integration.tokenExpiry.isBefore(Instant.now().plusSeconds(300));

        if (tokenExpiredOrExpiringSoon) {
            Result<JiraTokens> refresh = jiraOAuthService.refreshAccessToken(integration.refreshToken);
            if (!refresh.isSuccess()) {
                return Result.badRequest("Jira session expired. Please reconnect your Jira account.");
            }
            JiraTokens newTokens = refresh.getValue();
            integration.accessToken = newTokens.accessToken();
            integration.refreshToken = newTokens.refreshToken();
            integration.tokenExpiry = newTokens.tokenExpiry();
        }

        Result<TicketData> fetchResult = jiraTicketClient.fetch(parsed.key(), integration.cloudId, integration.accessToken);
        if (!fetchResult.isSuccess()) {
            return Result.internalError(fetchResult.getMessage());
        }

        TicketData data = fetchResult.getValue();
        Ticket ticket = new Ticket();
        ticket.ticketKey = parsed.key();
        ticket.workspaceId = workspaceId;
        ticket.importedBy = userId;
        ticket.projectKey = parsed.key().split("-")[0];
        ticket.summary = data.title();
        ticket.externalCreatedAt = data.externalCreatedAt();
        ticketRepository.persist(ticket);

        return Result.created(ticketMapper.toResponse(ticket));
    }

    public Result<List<TicketResponse>> getTickets(UUID userId, UUID workspaceId, boolean mine) {
        if (workspaceRepository.findByIdOptional(workspaceId).isEmpty()) {
            return Result.notFound("Workspace not found.");
        }
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            return Result.forbidden("You are not a member of this workspace.");
        }

        List<Ticket> tickets = mine
                ? ticketRepository.findByWorkspaceIdAndImportedBy(workspaceId, userId)
                : ticketRepository.findByWorkspaceId(workspaceId);

        return Result.ok(ticketMapper.toResponseList(tickets));
    }

    @Transactional
    public Result<Void> deleteTicket(UUID userId, UUID workspaceId, String ticketKey) {
        if (workspaceRepository.findByIdOptional(workspaceId).isEmpty()) {
            return Result.notFound("Workspace not found.");
        }
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            return Result.forbidden("You are not a member of this workspace.");
        }

        Ticket ticket = ticketRepository.findByTicketKeyAndWorkspaceId(ticketKey, workspaceId).orElse(null);
        if (ticket == null) {
            return Result.notFound("Ticket " + ticketKey + " not found in this workspace.");
        }

        ticketRepository.delete(ticket);
        return Result.ok(null);
    }
}
