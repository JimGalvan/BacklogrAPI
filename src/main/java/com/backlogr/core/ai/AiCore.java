package com.backlogr.core.ai;

import com.backlogr.core.BaseCore;
import com.backlogr.core.ticket.TicketCore;
import com.backlogr.domain.dto.ticket.TicketAggregateResponse;
import com.backlogr.domain.dto.ticket.TicketCommentResponse;
import com.backlogr.domain.enums.AiModelProvider;
import com.backlogr.domain.ai.AiMessage;
import com.backlogr.services.ai.AiService;
import com.backlogr.services.core.factories.AiServiceFactory;
import com.backlogr.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class AiCore extends BaseCore {

    private static final Logger logger = Logger.getLogger(AiCore.class);

    @Inject
    TicketCore ticketCore;

    public Multi<String> getTlDr(UUID userId, UUID workspaceId, String ticketKey) {
        logger.infof("Generating TL;DR for ticket %s in workspace %s", ticketKey, workspaceId);
        return streamTicketAnalysis(userId, workspaceId, ticketKey, this::buildTlDrPrompt);
    }

    public Multi<String> generateTestCases(UUID userId, UUID workspaceId, String ticketKey) {
        logger.infof("Generating test cases for ticket %s in workspace %s", ticketKey, workspaceId);
        return streamTicketAnalysis(userId, workspaceId, ticketKey, this::buildTestCasesPrompt);
    }

    public Multi<String> runRefinementAnalysis(UUID userId, UUID workspaceId, String ticketKey) {
        logger.infof("Running refinement analysis for ticket %s in workspace %s", ticketKey, workspaceId);

        Result<TicketAggregateResponse> ticketResult = ticketCore.getTicket(userId, workspaceId, ticketKey);
        if (!ticketResult.isSuccess()) {
            return Multi.createFrom().failure(new WebApplicationException(ticketResult.getMessage(), ticketResult.getStatus()));
        }

        Result<List<TicketCommentResponse>> commentsResult = ticketCore.getTicketComments(userId, workspaceId, ticketKey);
        List<TicketCommentResponse> comments = commentsResult.isSuccess()
                ? commentsResult.getValue()
                : List.of();

        if (!commentsResult.isSuccess()) {
            logger.warnf("Could not fetch comments for ticket %s — proceeding with description only: %s",
                    ticketKey, commentsResult.getMessage());
        }

        AiService aiService = AiServiceFactory.build(AiModelProvider.OLLAMA).orElse(null);
        if (aiService == null) {
            return Multi.createFrom().failure(new WebApplicationException("AI service not available.", 503));
        }

        TicketAggregateResponse ticket = ticketResult.getValue();

        logger.infof("Running first-pass refinement analysis for ticket %s", ticketKey);
        String firstPassJson = aiService.ask(buildRefinementPrompt(ticket, comments))
                .await().indefinitely();

        logger.infof("Running reviewer pass for ticket %s", ticketKey);
        return aiService.stream(buildRefinementReviewerPrompt(ticket, comments, firstPassJson));
    }

    private Multi<String> streamTicketAnalysis(
            UUID userId,
            UUID workspaceId,
            String ticketKey,
            BiFunction<TicketAggregateResponse, List<TicketCommentResponse>, List<AiMessage>> promptBuilder) {

        Result<TicketAggregateResponse> ticketResult = ticketCore.getTicket(userId, workspaceId, ticketKey);
        if (!ticketResult.isSuccess()) {
            return Multi.createFrom().failure(new WebApplicationException(ticketResult.getMessage(), ticketResult.getStatus()));
        }

        Result<List<TicketCommentResponse>> commentsResult = ticketCore.getTicketComments(userId, workspaceId, ticketKey);
        List<TicketCommentResponse> comments = commentsResult.isSuccess()
                ? commentsResult.getValue()
                : List.of();

        if (!commentsResult.isSuccess()) {
            logger.warnf("Could not fetch comments for ticket %s — proceeding with description only: %s",
                    ticketKey, commentsResult.getMessage());
        }

        AiService aiService = AiServiceFactory.build(AiModelProvider.OLLAMA).orElse(null);
        if (aiService == null) {
            return Multi.createFrom().failure(new WebApplicationException("AI service not available.", 503));
        }

        return aiService.stream(promptBuilder.apply(ticketResult.getValue(), comments));
    }

    private List<AiMessage> buildRefinementReviewerPrompt(
            TicketAggregateResponse ticket,
            List<TicketCommentResponse> comments,
            String firstPassJson) {
        return List.of(
                AiMessage.system(SystemPrompts.REFINEMENT_REVIEWER),
                AiMessage.user("""
                        Ticket title: %s
                        
                        Description:
                        %s
                        
                        Comments:
                        %s
                        
                        ---
                        
                        First-pass findings to review (plain-text list):
                        %s
                        """.formatted(
                        ticket.title(),
                        extractText(ticket.description()),
                        formatComments(comments),
                        firstPassJson
                ))
        );
    }

    private List<AiMessage> buildRefinementPrompt(TicketAggregateResponse ticket, List<TicketCommentResponse> comments) {
        return List.of(
                AiMessage.system(SystemPrompts.REFINEMENT_ANALYSIS),
        AiMessage.user("""
                Ticket title: %s
                
                Description:
                %s
                
                Comments:
                %s
                """.formatted(
                ticket.title(),
                extractText(ticket.description()),
                formatComments(comments)
        ))
        );
    }

    private List<AiMessage> buildTestCasesPrompt(TicketAggregateResponse ticket, List<TicketCommentResponse> comments) {
        return List.of(
                AiMessage.system(SystemPrompts.TEST_CASES),
                AiMessage.user("""
                        Ticket: %s
                        
                        Description:
                        %s
                        
                        Comments:
                        %s
                        """.formatted(
                        ticket.title(),
                        extractText(ticket.description()),
                        formatComments(comments)
                ))
        );
    }

    private List<AiMessage> buildTlDrPrompt(TicketAggregateResponse ticket, List<TicketCommentResponse> comments) {
        return List.of(
                AiMessage.system(SystemPrompts.TLDR),
                AiMessage.user("""
                        Ticket title: %s
                        
                        Description:
                        %s
                        
                        Comments:
                        %s
                        """.formatted(
                        ticket.title(),
                        extractText(ticket.description()),
                        formatComments(comments)
                ))
        );
    }

    private String formatComments(List<TicketCommentResponse> comments) {
        if (comments.isEmpty()) return "No comments.";

        return IntStream.range(0, comments.size())
                .mapToObj(index -> {
                    TicketCommentResponse comment = comments.get(index);
                    String author = comment.authorName() != null ? comment.authorName() : "Unknown";
                    String body = extractText(comment.body());
                    String id = comment.id();
                    return "[%d] - id:[%s] %s: %s".formatted(index + 1, id, author, body);
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Recursively walks a JsonNode produced by Jira's Atlassian Document Format
     * and returns the visible plain text, preserving paragraph and list breaks.
     */
    private String extractText(JsonNode node) {
        if (node == null || node.isNull()) return "";

        String type = node.path("type").asText("");

        // leaf: plain text node
        if ("text".equals(type)) {
            return node.path("text").asText("");
        }

        // leaf: inline nodes with no useful text
        if ("hardBreak".equals(type) || "rule".equals(type)) {
            return "\n";
        }

        // recurse into content array
        JsonNode content = node.path("content");
        if (content.isMissingNode() || !content.isArray()) return "";

        StringBuilder builder = new StringBuilder();
        content.forEach(child -> {
            String childText = extractText(child);
            if (!childText.isBlank()) {
                builder.append(childText);
            }
        });

        // block-level nodes get a trailing newline so paragraphs/list items are separated
        String result = builder.toString();
        boolean isBlock = switch (type) {
            case "paragraph", "heading", "listItem",
                 "bulletList", "orderedList", "blockquote",
                 "codeBlock", "panel" -> true;
            default -> false;
        };

        return isBlock ? result + "\n" : result;
    }
}
