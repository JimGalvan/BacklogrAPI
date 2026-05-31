package com.backlogr.core.ai;

import com.backlogr.core.BaseCore;
import com.backlogr.core.ticket.TicketCore;
import com.backlogr.domain.dto.ticket.TicketAggregateResponse;
import com.backlogr.domain.dto.ticket.TicketCommentResponse;
import com.backlogr.domain.dto.ticket.TicketWithComments;
import com.backlogr.domain.enums.AiModelProvider;
import com.backlogr.domain.ai.Prompt;
import com.backlogr.prompts.SystemPromptConstants;
import com.backlogr.services.ai.AiService;
import com.backlogr.services.core.factories.AiServiceFactory;
import com.backlogr.common.Result;
import com.backlogr.common.TicketContentFormatter;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AiCore extends BaseCore {

    private static final Logger logger = Logger.getLogger(AiCore.class);

    @Inject
    TicketCore ticketCore;

    @Inject
    TicketContentFormatter ticketContentFormatter;

    public Multi<String> getTlDr(UUID userId, UUID workspaceId, String ticketKey) {
        logger.infof("Generating TL;DR for ticket %s in workspace %s", ticketKey, workspaceId);

        TicketWithComments ticketWithComments = getTicketWithComments(userId, workspaceId, ticketKey);
        AiService aiService = getAiService();

        TicketAggregateResponse ticket = ticketWithComments.ticket();
        List<TicketCommentResponse> comments = ticketWithComments.comments();

        Prompt prompt = Prompt.builder()
                .system(SystemPromptConstants.TLDR)
                .user("""
                        Ticket title: %s

                        Description:
                        %s

                        Comments:
                        %s
                        """.formatted(
                        ticket.title(),
                        ticketContentFormatter.extractText(ticket.description()),
                        ticketContentFormatter.formatComments(comments)
                ))
                .build();
        return aiService.stream(prompt);
    }

    public Multi<String> generateTestCases(UUID userId, UUID workspaceId, String ticketKey) {
        logger.infof("Generating test cases for ticket %s in workspace %s", ticketKey, workspaceId);

        TicketWithComments ticketWithComments = getTicketWithComments(userId, workspaceId, ticketKey);
        AiService aiService = getAiService();

        TicketAggregateResponse ticket = ticketWithComments.ticket();
        List<TicketCommentResponse> comments = ticketWithComments.comments();

        Prompt prompt = Prompt.builder()
                .system(SystemPromptConstants.TEST_CASES)
                .user("""
                        Ticket: %s

                        Description:
                        %s

                        Comments:
                        %s
                        """.formatted(
                        ticket.title(),
                        ticketContentFormatter.extractText(ticket.description()),
                        ticketContentFormatter.formatComments(comments)
                ))
                .build();
        return aiService.stream(prompt);
    }

    public Multi<String> runRefinementAnalysis(UUID userId, UUID workspaceId, String ticketKey) {
        logger.infof("Running refinement analysis for ticket %s in workspace %s", ticketKey, workspaceId);

        TicketWithComments ticketWithComments = getTicketWithComments(userId, workspaceId, ticketKey);
        AiService aiService = getAiService();

        TicketAggregateResponse ticket = ticketWithComments.ticket();
        List<TicketCommentResponse> comments = ticketWithComments.comments();

        logger.infof("Running first-pass refinement analysis for ticket %s", ticketKey);
        Prompt firstPassPrompt = Prompt.builder()
                .system(SystemPromptConstants.REFINEMENT_ANALYSIS)
                .user("""
                        Ticket title: %s

                        Description:
                        %s

                        Comments:
                        %s
                        """.formatted(
                        ticket.title(),
                        ticketContentFormatter.extractText(ticket.description()),
                        ticketContentFormatter.formatComments(comments)
                ))
                .build();
        String firstPassJson = aiService.ask(firstPassPrompt).await().indefinitely();

        logger.infof("Running reviewer pass for ticket %s", ticketKey);
        Prompt reviewerPrompt = Prompt.builder()
                .system(SystemPromptConstants.REFINEMENT_REVIEWER)
                .user("""
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
                        ticketContentFormatter.extractText(ticket.description()),
                        ticketContentFormatter.formatComments(comments),
                        firstPassJson
                ))
                .build();
        return aiService.stream(reviewerPrompt);
    }

    private TicketWithComments getTicketWithComments(UUID userId, UUID workspaceId, String ticketKey) {
        Result<TicketWithComments> result = ticketCore.getTicketWithComments(userId, workspaceId, ticketKey);
        if (!result.isSuccess()) {
            throw new WebApplicationException(result.getMessage(), result.getStatus());
        }
        return result.getValue();
    }

    private AiService getAiService() {
        return AiServiceFactory.build(AiModelProvider.OLLAMA)
                .orElseThrow(() -> new WebApplicationException("AI service not available.", 503));
    }

}
