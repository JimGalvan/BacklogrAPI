package com.backlogr.integration.jira;

import com.backlogr.enums.Provider;
import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketStatus;
import com.backlogr.integration.AuthTokens;
import com.backlogr.integration.ProviderService;
import com.backlogr.integration.TicketComment;
import com.backlogr.integration.TicketData;
import com.backlogr.integration.jira.client.JiraHttpClient;
import com.backlogr.integration.jira.dto.JiraCommentListResponse;
import com.backlogr.integration.jira.dto.JiraIssueResponse;
import com.backlogr.integration.jira.oauth.JiraOAuthService;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class JiraService implements ProviderService {

    @Inject
    JiraOAuthService jiraOAuthService;

    @Inject
    @RestClient
    JiraHttpClient httpClient;

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.JIRA;
    }

    @Override
    public Provider getProvider() {
        return Provider.JIRA;
    }

    @Override
    public Result<AuthTokens> refreshToken(String refreshToken) {
        return jiraOAuthService.refreshAccessToken(refreshToken);
    }

    @Override
    public Result<TicketData> fetch(String key, String cloudId, String accessToken) {
        try {
            JiraIssueResponse issue = httpClient.getIssue(cloudId, key, "Bearer " + accessToken);
            return Result.ok(toTicketData(issue));
        } catch (Exception e) {
            return Result.internalError("Failed to fetch Jira issue " + key + ": " + e.getMessage());
        }
    }

    @Override
    public Result<List<TicketComment>> fetchComments(String key, String cloudId, String accessToken) {
        try {
            JiraCommentListResponse response = httpClient.getComments(cloudId, key, "Bearer " + accessToken);
            List<TicketComment> comments = response.comments().stream()
                    .map(this::toTicketComment)
                    .toList();
            return Result.ok(comments);
        } catch (Exception e) {
            return Result.internalError("Failed to fetch comments for Jira issue " + key + ": " + e.getMessage());
        }
    }

    private TicketComment toTicketComment(JiraCommentListResponse.JiraComment comment) {
        JiraCommentListResponse.JiraAuthor author = comment.author();
        return new TicketComment(
            comment.id(),
            author != null ? author.emailAddress() : null,
            author != null ? author.displayName() : null,
            comment.body(),
            parseCreated(comment.created()),
            parseCreated(comment.updated())
        );
    }

    private TicketData toTicketData(JiraIssueResponse issue) {
        JiraIssueResponse.JiraFields fields = issue.fields();
        return new TicketData(
            issue.key(),
            fields.summary(),
            fields.description(),
            mapStatus(fields.status()),
            mapPriority(fields.priority()),
            fields.assignee() != null ? fields.assignee().emailAddress() : null,
            fields.storyPoints(),
            fields.labels() != null ? fields.labels() : List.of(),
            parseCreated(fields.created())
        );
    }

    private static Instant parseCreated(String created) {
        if (created == null || created.isBlank()) return Instant.now();
        try {
            return OffsetDateTime.parse(created, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxx"))
                    .toInstant();
        } catch (Exception e) {
            try {
                return Instant.parse(created);
            } catch (Exception e2) {
                return Instant.now();
            }
        }
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
