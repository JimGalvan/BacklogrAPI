package com.backlogr.mapper;

import com.backlogr.domain.ticket.Ticket;
import com.backlogr.dto.ticket.TicketResponse;
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
    @Mapping(target = "summary",     source = "ticketData.title")
    @Mapping(target = "description", source = "ticketData.description")
    TicketResponse toResponse(Ticket ticket, TicketData ticketData);
}
