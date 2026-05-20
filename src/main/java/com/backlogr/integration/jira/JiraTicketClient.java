package com.backlogr.integration.jira;

import com.backlogr.domain.user.UserIntegration;
import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketSource;
import com.backlogr.enums.ticket.TicketStatus;
import com.backlogr.integration.TicketClient;
import com.backlogr.integration.TicketData;
import com.backlogr.integration.jira.client.JiraApiClient;
import com.backlogr.integration.jira.dto.JiraIssueResponse;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class JiraTicketClient implements TicketClient {

    @Inject
    @RestClient
    JiraApiClient jiraApiClient;

    @Override
    public boolean supports(TicketSource source) {
        return source == TicketSource.JIRA;
    }

    @Override
    public Result<TicketData> fetch(String key, UserIntegration integration) {
        try {
            JiraIssueResponse issue = jiraApiClient.getIssue(
                integration.cloudId,
                key,
                "Bearer " + integration.accessToken
            );
            return Result.ok(toExternalData(issue));
        } catch (Exception e) {
            return Result.internalError("Failed to fetch Jira issue " + key + ": " + e.getMessage());
        }
    }

    private TicketData toExternalData(JiraIssueResponse issue) {
        JiraIssueResponse.JiraFields fields = issue.fields();

        return new TicketData(
            issue.key(),
            fields.summary(),
            null,
            mapStatus(fields.status()),
            mapPriority(fields.priority()),
            fields.assignee() != null ? fields.assignee().emailAddress() : null,
            fields.storyPoints(),
            fields.labels() != null ? fields.labels() : List.of()
        );
    }

    private TicketStatus mapStatus(JiraIssueResponse.JiraStatus jiraStatus) {
        if (jiraStatus == null) return TicketStatus.BACKLOG;
        return switch (jiraStatus.name().toLowerCase()) {
            case "to do"       -> TicketStatus.TODO;
            case "in progress" -> TicketStatus.IN_PROGRESS;
            case "in review"   -> TicketStatus.IN_REVIEW;
            case "done"        -> TicketStatus.DONE;
            default            -> TicketStatus.BACKLOG;
        };
    }

    private TicketPriority mapPriority(JiraIssueResponse.JiraPriority jiraPriority) {
        if (jiraPriority == null) return TicketPriority.MEDIUM;
        return switch (jiraPriority.name().toLowerCase()) {
            case "highest", "critical" -> TicketPriority.CRITICAL;
            case "high"                -> TicketPriority.HIGH;
            case "low", "lowest"       -> TicketPriority.LOW;
            default                    -> TicketPriority.MEDIUM;
        };
    }
}
