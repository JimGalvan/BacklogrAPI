package com.backlogr.controller.ticket;

import com.backlogr.controller.BaseController;
import com.backlogr.core.ticket.TicketCore;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketImportResponse;
import com.backlogr.shared.HttpStatus;
import com.backlogr.shared.Result;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/tickets")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Tickets", description = "Ticket management operations")
public class TicketController extends BaseController {

    @Inject
    TicketCore ticketCore;

    @POST
    @Path("/import")
    @Operation(
        summary = "Import tickets in bulk",
        description = "Imports a batch of tickets from an external source. Duplicates (same externalId + source) are silently skipped."
    )
    @APIResponses({
        @APIResponse(
            responseCode = HttpStatus.OK,
            description = "Import completed — check imported/skipped/failed counts",
            content = @Content(schema = @Schema(implementation = TicketImportResponse.class))
        ),
        @APIResponse(responseCode = HttpStatus.BAD_REQUEST,           description = HttpStatus.Description.BAD_REQUEST),
        @APIResponse(responseCode = HttpStatus.UNPROCESSABLE_ENTITY,  description = HttpStatus.Description.UNPROCESSABLE_ENTITY),
        @APIResponse(responseCode = HttpStatus.INTERNAL_SERVER_ERROR, description = HttpStatus.Description.INTERNAL_SERVER_ERROR)
    })
    public Response importTickets(@Valid TicketImportRequest request) {
        Result<TicketImportResponse> result = ticketCore.importTickets(request);
        return toResponse(result);
    }
}
