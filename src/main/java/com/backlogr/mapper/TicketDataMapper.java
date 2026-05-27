package com.backlogr.mapper;

import com.backlogr.domain.ticket.Ticket;
import com.backlogr.dto.ticket.TicketAggregateResponse;
import com.backlogr.integration.TicketData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TicketDataMapper {

    @Mapping(target = "id",          source = "ticket.id")
    @Mapping(target = "ticketKey",   source = "ticket.ticketKey")
    @Mapping(target = "workspaceId", source = "ticket.workspaceId")
    @Mapping(target = "importedBy",  source = "ticket.importedBy")
    @Mapping(target = "projectKey",  source = "ticket.projectKey")
    @Mapping(target = "provider",    source = "ticket.provider")
    @Mapping(target = "createdAt",   source = "ticket.externalCreatedAt")
    @Mapping(target = "importedAt",  source = "ticket.createdAt")
    @Mapping(target = "title",       source = "ticketData.title")
    @Mapping(target = "description", source = "ticketData.description")
    @Mapping(target = "status",      source = "ticketData.status")
    @Mapping(target = "priority",    source = "ticketData.priority")
    @Mapping(target = "assignee",    source = "ticketData.assignee")
    @Mapping(target = "storyPoints", source = "ticketData.storyPoints")
    @Mapping(target = "tags",        source = "ticketData.tags")
    TicketAggregateResponse toResponse(Ticket ticket, TicketData ticketData);
}
