package com.backlogr.controller.ticket;

import com.backlogr.core.ticket.TicketCore;
import com.backlogr.dto.ticket.TicketImportRequest;
import com.backlogr.dto.ticket.TicketImportResponse;
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
public class TicketController {

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
            responseCode = "200",
            description = "Import completed — check imported/skipped/failed counts",
            content = @Content(schema = @Schema(implementation = TicketImportResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Malformed request body"),
        @APIResponse(responseCode = "422", description = "Bean Validation failure"),
        @APIResponse(responseCode = "500", description = "Unexpected server error")
    })
    public Response importTickets(@Valid TicketImportRequest request) {
        TicketImportResponse response = ticketCore.importTickets(request);
        return Response.ok(response).build();
    }
}
