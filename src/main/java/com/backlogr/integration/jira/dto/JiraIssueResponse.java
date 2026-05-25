package com.backlogr.integration.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraIssueResponse(
    String id,
    String key,
    JiraFields fields
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JiraFields(
        String summary,
        JsonNode description,
        String created,
        JiraStatus status,
        JiraPriority priority,
        JiraAssignee assignee,
        List<String> labels,
        @JsonProperty("story_points") Integer storyPoints
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JiraStatus(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JiraPriority(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JiraAssignee(String emailAddress) {}
}
