package com.backlogr.integration.jira;

import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketSource;
import com.backlogr.enums.ticket.TicketStatus;
import com.backlogr.integration.ExternalTicketClient;
import com.backlogr.integration.ExternalTicketData;
import com.backlogr.integration.jira.client.JiraApiClient;
import com.backlogr.integration.jira.dto.JiraIssueResponse;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class JiraTicketClient implements ExternalTicketClient {

    @Inject
    @RestClient
    JiraApiClient jiraApiClient;

    @Override
    public boolean supports(TicketSource source) {
        return source == TicketSource.JIRA;
    }

    @Override
    public Result<ExternalTicketData> fetch(String key) {
        try {
            JiraIssueResponse issue = jiraApiClient.getIssue(key);
            return Result.ok(toExternalData(issue));
        } catch (Exception e) {
            return Result.internalError("Failed to fetch Jira issue " + key + ": " + e.getMessage());
        }
    }

    private ExternalTicketData toExternalData(JiraIssueResponse issue) {
        JiraIssueResponse.JiraFields fields = issue.fields();

        return new ExternalTicketData(
            issue.key(),
            fields.summary(),
            null,                           // Jira description uses Atlassian Document Format — out of scope for POC
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
            case "to do"      -> TicketStatus.TODO;
            case "in progress" -> TicketStatus.IN_PROGRESS;
            case "in review"  -> TicketStatus.IN_REVIEW;
            case "done"       -> TicketStatus.DONE;
            default           -> TicketStatus.BACKLOG;
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
