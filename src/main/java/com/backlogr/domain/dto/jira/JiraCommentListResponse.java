package com.backlogr.domain.dto.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraCommentListResponse(List<JiraComment> comments) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JiraComment(
        String id,
        JiraAuthor author,
        JsonNode body,
        String created,
        String updated
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JiraAuthor(
        String emailAddress,
        String displayName
    ) {}
}
