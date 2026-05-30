package com.backlogr.controller.ai;

import com.backlogr.controller.BaseController;
import com.backlogr.core.ai.AiCore;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

import static com.backlogr.shared.HttpStatus.Description;
import static com.backlogr.shared.HttpStatus.FORBIDDEN;
import static com.backlogr.shared.HttpStatus.NOT_FOUND;
import static com.backlogr.shared.HttpStatus.OK;

@Path("/api/v1/workspaces/{workspaceId}/ai")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "AI", description = "AI-powered ticket insights")
@Authenticated
@SecurityRequirement(name = "jwt")
public class AiController extends BaseController {

    @Inject
    AiCore aiCore;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/tickets/{ticketKey}/test-cases")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Blocking
    @Operation(
            summary     = "Generate test cases for a ticket",
            description = "Streams high-level QA test scenarios grouped by component, integration, system, system integration, and acceptance testing. Tokens are emitted as Server-Sent Events."
    )
    @APIResponses({
            @APIResponse(responseCode = OK,        description = Description.OK),
            @APIResponse(responseCode = FORBIDDEN,  description = Description.FORBIDDEN),
            @APIResponse(responseCode = NOT_FOUND,  description = Description.NOT_FOUND)
    })
    public Multi<String> generateTestCases(
            @PathParam("workspaceId") UUID workspaceId,
            @PathParam("ticketKey")   String ticketKey) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return aiCore.generateTestCases(userId, workspaceId, ticketKey);
    }

    @GET
    @Path("/tickets/{ticketKey}/refinement")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Blocking
    @Operation(
            summary     = "Run a refinement analysis for a ticket",
            description = "Streams a readiness assessment covering clarity, acceptance criteria, scope, dependencies, and edge cases. Tokens are emitted as Server-Sent Events."
    )
    @APIResponses({
            @APIResponse(responseCode = OK,        description = Description.OK),
            @APIResponse(responseCode = FORBIDDEN,  description = Description.FORBIDDEN),
            @APIResponse(responseCode = NOT_FOUND,  description = Description.NOT_FOUND)
    })
    public Multi<String> runRefinementAnalysis(
            @PathParam("workspaceId") UUID workspaceId,
            @PathParam("ticketKey")   String ticketKey) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return aiCore.runRefinementAnalysis(userId, workspaceId, ticketKey);
    }

    @GET
    @Path("/tickets/{ticketKey}/tldr")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Blocking
    @Operation(
            summary     = "Generate a TL;DR for a ticket",
            description = "Streams a concise 2-3 sentence summary of the ticket using the configured AI model. Tokens are emitted as Server-Sent Events."
    )
    @APIResponses({
            @APIResponse(responseCode = OK,        description = Description.OK),
            @APIResponse(responseCode = FORBIDDEN,  description = Description.FORBIDDEN),
            @APIResponse(responseCode = NOT_FOUND,  description = Description.NOT_FOUND)
    })
    public Multi<String> getTlDr(
            @PathParam("workspaceId") UUID workspaceId,
            @PathParam("ticketKey")   String ticketKey) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return aiCore.getTlDr(userId, workspaceId, ticketKey);
    }
}
