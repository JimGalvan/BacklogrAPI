package com.backlogr.core.ticket;

import com.backlogr.core.ticket.TicketUrlParser.ParsedTicketUrl;
import com.backlogr.domain.ticket.Ticket;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketResponse;
import com.backlogr.mapper.TicketMapper;
import com.backlogr.repository.ticket.TicketRepository;
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

        // TODO: replace with real Jira fetch
        Ticket ticket = new Ticket();
        ticket.ticketKey = parsed.key();
        ticket.workspaceId = workspaceId;
        ticket.importedBy = userId;
        ticket.projectKey = parsed.key().split("-")[0];
        ticket.summary = "Mock summary for " + parsed.key();
        ticket.externalCreatedAt = Instant.now();
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
