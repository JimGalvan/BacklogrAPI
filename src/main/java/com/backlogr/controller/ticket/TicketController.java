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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/tickets")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Tickets", description = "Workspace ticket operations")
@Authenticated
@SecurityRequirement(name = "jwt")
@RunOnVirtualThread
public class TicketController extends BaseController {

    @Inject
    TicketCore ticketCore;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/{workspaceId}/import")
    @Operation(summary = "Import a ticket into a workspace")
    @APIResponses({
        @APIResponse(responseCode = HttpStatus.CREATED,              description = HttpStatus.Description.CREATED),
        @APIResponse(responseCode = HttpStatus.BAD_REQUEST,          description = HttpStatus.Description.BAD_REQUEST),
        @APIResponse(responseCode = HttpStatus.FORBIDDEN,            description = HttpStatus.Description.FORBIDDEN),
        @APIResponse(responseCode = HttpStatus.NOT_FOUND,            description = HttpStatus.Description.NOT_FOUND),
        @APIResponse(responseCode = HttpStatus.CONFLICT,             description = HttpStatus.Description.CONFLICT),
        @APIResponse(responseCode = HttpStatus.UNPROCESSABLE_ENTITY, description = HttpStatus.Description.UNPROCESSABLE_ENTITY)
    })
    public Response importTicket(
            @PathParam("workspaceId") UUID workspaceId,
            @Valid TicketImportRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return toResponse(ticketCore.importTicket(userId, workspaceId, request));
    }

    @GET
    @Path("/{workspaceId}")
    @Operation(summary = "List tickets in a workspace", description = "Use ?mine=true to return only tickets imported by the authenticated user.")
    @APIResponses({
        @APIResponse(responseCode = HttpStatus.OK,        description = HttpStatus.Description.OK),
        @APIResponse(responseCode = HttpStatus.FORBIDDEN, description = HttpStatus.Description.FORBIDDEN),
        @APIResponse(responseCode = HttpStatus.NOT_FOUND, description = HttpStatus.Description.NOT_FOUND)
    })
    public Response getTickets(
            @PathParam("workspaceId") UUID workspaceId,
            @QueryParam("mine") @DefaultValue("false") boolean mine) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return toResponse(ticketCore.getTickets(userId, workspaceId, mine));
    }

    @DELETE
    @Path("/{workspaceId}/{ticketKey}")
    @Operation(summary = "Remove a ticket from a workspace")
    @APIResponses({
        @APIResponse(responseCode = HttpStatus.NO_CONTENT, description = HttpStatus.Description.NO_CONTENT),
        @APIResponse(responseCode = HttpStatus.FORBIDDEN,  description = HttpStatus.Description.FORBIDDEN),
        @APIResponse(responseCode = HttpStatus.NOT_FOUND,  description = HttpStatus.Description.NOT_FOUND)
    })
    public Response deleteTicket(
            @PathParam("workspaceId") UUID workspaceId,
            @PathParam("ticketKey") String ticketKey) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Result<Void> result = ticketCore.deleteTicket(userId, workspaceId, ticketKey);
        if (!result.isSuccess()) return toResponse(result);
        return Response.noContent().build();
    }
}
