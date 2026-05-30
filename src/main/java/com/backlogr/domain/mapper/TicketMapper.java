package com.backlogr.domain.mapper;

import com.backlogr.domain.entities.ticket.Ticket;
import com.backlogr.domain.dto.ticket.TicketAggregateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface TicketMapper {

    @Mapping(target = "createdAt",  source = "externalCreatedAt")
    @Mapping(target = "importedAt", source = "createdAt")
    TicketAggregateResponse toResponse(Ticket ticket);

    List<TicketAggregateResponse> toResponseList(List<Ticket> tickets);
}
