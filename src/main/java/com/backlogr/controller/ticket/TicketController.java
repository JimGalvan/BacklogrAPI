package com.backlogr.controller.ticket;

import com.backlogr.controller.BaseController;
import com.backlogr.core.ticket.TicketCore;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketResponse;
import com.backlogr.shared.HttpStatus;
import com.backlogr.shared.Result;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/api/v1/tickets")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Tickets", description = "Ticket management operations")
@Authenticated
@SecurityRequirement(name = "jwt")
@RunOnVirtualThread
public class TicketController extends BaseController {

    @Inject
    TicketCore ticketCore;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/import")
    @Operation(
        summary = "Import a ticket from an external tracker URL",
        description = "Parses the tracker URL, determines the source, and imports the ticket. The user must have the corresponding integration connected. Supported: Jira."
    )
    @APIResponses({
        @APIResponse(
            responseCode = HttpStatus.OK,
            description = HttpStatus.Description.OK,
            content = @Content(schema = @Schema(implementation = TicketResponse.class))
        ),
        @APIResponse(responseCode = HttpStatus.BAD_REQUEST,           description = HttpStatus.Description.BAD_REQUEST),
        @APIResponse(responseCode = HttpStatus.UNAUTHORIZED,          description = HttpStatus.Description.UNAUTHORIZED),
        @APIResponse(responseCode = HttpStatus.CONFLICT,              description = HttpStatus.Description.CONFLICT),
        @APIResponse(responseCode = HttpStatus.UNPROCESSABLE_ENTITY,  description = HttpStatus.Description.UNPROCESSABLE_ENTITY),
        @APIResponse(responseCode = HttpStatus.INTERNAL_SERVER_ERROR, description = HttpStatus.Description.INTERNAL_SERVER_ERROR)
    })
    public Response importTicket(@Valid TicketImportRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Result<TicketResponse> result = ticketCore.importTicket(userId, request);
        return toResponse(result);
    }
}
