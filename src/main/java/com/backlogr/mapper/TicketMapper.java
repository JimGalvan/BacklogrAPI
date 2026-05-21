package com.backlogr.mapper;

import com.backlogr.domain.ticket.Ticket;
import com.backlogr.dto.ticket.TicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface TicketMapper {

    @Mapping(target = "createdAt",  source = "externalCreatedAt")
    @Mapping(target = "importedAt", source = "createdAt")
    TicketResponse toResponse(Ticket ticket);

    List<TicketResponse> toResponseList(List<Ticket> tickets);
}
