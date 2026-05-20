package com.backlogr.mapper;

import com.backlogr.core.ticket.TicketUrlParser.ParsedTicketUrl;
import com.backlogr.domain.ticket.Ticket;
import com.backlogr.dto.ticket.TicketResponse;
import com.backlogr.integration.TicketData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TicketMapper {

    @Mapping(target = "externalId", source = "parsedUrl.key")
    @Mapping(target = "source",     source = "parsedUrl.source")
    @Mapping(target = "url",        expression = "java(url)")
    @Mapping(target = "title",       source = "data.title")
    @Mapping(target = "description", source = "data.description")
    @Mapping(target = "status",      source = "data.status")
    @Mapping(target = "priority",    source = "data.priority")
    @Mapping(target = "assignee",    source = "data.assignee")
    @Mapping(target = "storyPoints", source = "data.storyPoints")
    @Mapping(target = "tags",        source = "data.tags")
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "lastModifiedAt",  ignore = true)
    Ticket toEntity(TicketData data, ParsedTicketUrl parsedUrl, String url);

    @Mapping(target = "key", source = "externalId")
    TicketResponse toResponse(Ticket ticket);
}
