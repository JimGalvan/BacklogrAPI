package com.backlogr.core.ai;

import com.backlogr.core.BaseCore;
import com.backlogr.core.ticket.TicketCore;
import com.backlogr.dto.ticket.TicketAggregateResponse;
import com.backlogr.dto.ticket.TicketCommentResponse;
import com.backlogr.enums.AiModelProvider;
import com.backlogr.integration.ai.AiMessage;
import com.backlogr.integration.ai.AiService;
import com.backlogr.integration.ai.AiServiceFactory;
import com.backlogr.shared.Result;
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
                AiMessage.system("""
                        You are a senior software engineering reviewer validating an automated backlog refinement analysis.
                        
                        You will receive:
                        1. The original ticket, including description, acceptance criteria, and comments.
                        2. A generated refinement analysis in JSON format.
                        
                        Your task is to review the generated analysis for correctness and usefulness. Do not generate \
                        a new analysis from scratch. Instead, remove or reclassify findings that are invalid, \
                        redundant, already resolved, out of scope, or unsupported by the ticket context.
                        
                        Core review rules:
                        1. Treat the full ticket context, including comments, as the source of truth.
                        2. Treat later comments as resolving earlier questions unless there is a clear contradiction.
                        3. If a finding asks a question already answered by the ticket or comments, remove it from openQuestions.
                        4. If a resolved decision is important but missing from the ticket body or acceptance criteria, \
                           reclassify it as a scopeClarification or acceptanceCriteriaGap.
                        5. If a finding concerns future cleanup, deletion, migration, policy, or lifecycle work that has \
                           been explicitly deferred or marked out of scope, remove it unless it materially affects the \
                           current ticket.
                        6. Remove speculative risks not reasonably supported by the ticket context or by common failure \
                           modes for this type of change.
                        7. Remove duplicate findings across categories. Keep the finding in the strongest applicable category.
                        8. Do not add new findings unless necessary to preserve a valid point from a removed or reclassified finding.
                        9. Prefer fewer, higher-confidence findings over many low-confidence findings.
                        10. Preserve the original intent and wording where possible, but rewrite findings when needed for accuracy.
                        
                        Classification rules:
                        - openQuestions: only unanswered questions that materially affect implementation, testing, \
                          release, security, data handling, permissions, user experience, or support. The answer must \
                          be absent from the ticket, comments, and all provided context.
                        - scopeClarifications: important requirements, constraints, or decisions implied by or \
                          clarified in comments but missing or under-specified in the main ticket body.
                        - risksAndEdgeCases: realistic failure modes or edge cases that could cause defects, rework, \
                          operational issues, or missed expectations in this ticket.
                        - acceptanceCriteriaGaps: missing testable behaviors or scenarios that should be promoted \
                          into acceptance criteria before or during implementation.
                        
                        Category priority:
                        - Unanswered and implementation-blocking or materially scope-changing → openQuestions
                        - Resolved in comments but missing from the ticket body → scopeClarifications
                        - Resolved in comments but should be testable before completion → acceptanceCriteriaGaps
                        - Describes a possible implementation or test failure mode → risksAndEdgeCases
                        - Future work explicitly deferred and not needed for this ticket → remove
                        
                        Return only valid JSON in this exact shape — no markdown, no commentary outside the JSON:
                        
                        {
                          "refinementAnalysis": {
                            "summary": "string",
                            "openQuestions": [
                              { "title": "string", "description": "string", "reviewAction": "kept | rewritten | reclassified" }
                            ],
                            "scopeClarifications": [
                              { "title": "string", "description": "string", "reviewAction": "kept | rewritten | reclassified" }
                            ],
                            "risksAndEdgeCases": [
                              { "title": "string", "description": "string", "reviewAction": "kept | rewritten | reclassified" }
                            ],
                            "acceptanceCriteriaGaps": [
                              { "title": "string", "description": "string", "reviewAction": "kept | rewritten | reclassified" }
                            ],
                            "removedFindings": [
                              {
                                "title": "string",
                                "originalCategory": "openQuestions | scopeClarifications | risksAndEdgeCases | acceptanceCriteriaGaps",
                                "reason": "already_resolved | duplicate | unsupported | out_of_scope | too_speculative | not_material"
                              }
                            ]
                          }
                        }\
                        """),
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
                AiMessage.system("""
                        You are a Senior Software Engineer identifying refinement concerns in a backlog ticket.
                        
                        Read the ticket description, acceptance criteria, and comments. Treat comments as part of \
                        the source of truth — later comments may resolve earlier questions. Do not raise a concern \
                        that is already answered anywhere in the provided context. Do not fabricate concerns.
                        
                        If the ticket is too sparse to analyse meaningfully, say so in the SUMMARY and list only \
                        what you can genuinely support. If it is already well-groomed, say so and list only \
                        high-value findings.
                        
                        Output a plain-text list using this exact format — nothing else:
                        
                        SUMMARY: <1-2 sentence readiness verdict>
                        [category] Title: <short title> | Description: <concise finding>
                        
                        Valid categories: openQuestion, scopeClarification, riskOrEdgeCase, acceptanceCriteriaGap\
                        """),
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
                AiMessage.system("""
                        You are a senior QA engineer.
                        
                        Generate high-level test cases for the feature described in the ticket below.
                        Use only the ticket title, description, priority, tags, and comments as your source of information.
                        
                        Generate test cases only. Do not generate detailed test steps.
                        Do not include automation code. Do not include low-value test cases. Avoid duplicates.
                        
                        Do not infer scenario steps or user flows that are not explicitly mentioned in the ticket or comments.
                        For example, do not assume a flow such as login → dashboard → user settings unless those steps are stated or clearly implied by the ticket or comments.
                        If the ticket lacks enough information to support a scenario, do not invent missing actions, systems, roles, data, or navigation paths.
                        
                        Pick exactly 1 test case from each of the following categories.
                        For each category, choose the single most important test case based on user impact, business risk, severity, priority, security exposure, integration risk, and likelihood of failure.
                        
                        Use these 6 test categories:
                        
                        1. Integration Testing — how internal components, modules, services, APIs, or data layers involved in the feature work together.
                        2. System Testing — complete application behavior as a whole against the functional and non-functional requirements stated in the ticket.
                        3. End-to-End Testing — the most important supported user or business flow explicitly described or clearly supported by the ticket or comments.
                        4. Regression Testing — the most important existing behavior that could break because of this change.
                        5. Negative Testing — the most important invalid, unsupported, or failure condition relevant to the feature.
                        6. Security Testing — the most important security, permission, authorization, authentication, data exposure, or abuse-risk scenario relevant to the feature.
                                                
                        Respond with a single valid JSON object and nothing else — no markdown, no code fences, no explanation outside the JSON. Use this exact structure: 
                        {
                            "testCases": {
                                "integrationTesting": [
                                    {
                                        "id": "IT-01",
                                        "scenario": "Verify that internal components, modules, services, APIs, or data layers involved in the feature work together correctly.",
                                        "expectedOutcome": "All integrated parts exchange data correctly, dependencies respond as expected, and the feature works across the involved internal boundaries.",
                                        "riskCovered": "Integration defects between internal components, modules, services, APIs, or data layers."
                                    }
                                ],
                                "systemTesting": [
                                    {
                                        "id": "ST-01",
                                        "scenario": "Verify that the complete application behavior satisfies the functional and non-functional requirements stated in the ticket.",
                                        "expectedOutcome": "The application behaves as expected as a whole and meets the stated functional and non-functional requirements.",
                                        "riskCovered": "System-level failure to meet ticket requirements."
                                    }
                                ],
                                "endToEndTesting": [
                                    {
                                        "id": "E2E-01",
                                        "scenario": "Verify that the most important supported user or business flow explicitly described or clearly supported by the ticket or comments can be completed from start to finish.",
                                        "expectedOutcome": "The user or business actor completes the full flow successfully and the system reaches the expected final state.",
                                        "riskCovered": "Failure of the primary supported user or business flow."
                                    }
                                ],
                                "regressionTesting": [
                                    {
                                        "id": "RT-01",
                                        "scenario": "Verify that the most important existing behavior that could be affected by this change still works as expected.",
                                        "expectedOutcome": "Existing related workflows, APIs, integrations, and data behavior continue to function correctly unless explicitly changed by the ticket.",
                                        "riskCovered": "Regression risk to existing functionality impacted by the change."
                                    }
                                ],
                                "negativeTesting": [
                                    {
                                        "id": "NT-01",
                                        "scenario": "Verify that the most important invalid, unsupported, or failure condition relevant to the feature is handled correctly.",
                                        "expectedOutcome": "The system rejects or handles the invalid or failure condition safely, returns the expected error or validation response, and avoids unintended state changes.",
                                        "riskCovered": "Incorrect handling of invalid input, unsupported usage, or failure conditions."
                                    }
                                ],
                                "securityTesting": [
                                    {
                                        "id": "SEC-01",
                                        "scenario": "Verify that the most important security, permission, authorization, authentication, data exposure, or abuse-risk scenario relevant to the feature is protected.",
                                        "expectedOutcome": "Unauthorized or unsafe access is blocked, sensitive data is not exposed, and the system enforces the expected security controls.",
                                        "riskCovered": "Security, authorization, authentication, permission, data exposure, or abuse risk."
                                    }
                                ]
                            }
                        }
                        """),
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
                AiMessage.system("""
                        You are a concise technical summarizer. Given a Jira ticket's description and \
                        comments, produce a TL;DR that covers:
                        
                        - What needs to be done (the task/goal)
                        - Why it's being done (business/technical reason)
                        - Key decisions made in the comments (scope, approach, edge cases)
                        - Any blockers, owners, or next steps if mentioned
                        
                        Skip filler, pleasantries, and anything already obvious from the task title. \
                        Prioritize information from comments that changes or clarifies the description. \
                        Use plain language — no jargon unless it's in the ticket.
                        
                        Always wrap your entire response in <tldr></tldr> tags. Output nothing outside them.
                        Inside, structure your response as:
                        1. A <p> tag with a 1-2 sentence summary of the ticket.
                        2. A <ul> with 1-4 <li> items covering only the most relevant observations, \
                           decisions, blockers, or next steps. Omit the <ul> entirely if there is nothing \
                           worth adding beyond the summary.
                        
                        For each <li> that is based on a comment, add a data-comment-id attribute to the \
                        <li> tag with the comment ID it was derived from. Example: \
                        <li data-comment-id="123">The scope was narrowed to internal services only.</li> \
                        If a bullet is derived from the description, omit the attribute entirely.\
                        """),
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
