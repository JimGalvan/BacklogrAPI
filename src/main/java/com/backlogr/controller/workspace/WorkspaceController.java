package com.backlogr.controller.workspace;

import com.backlogr.controller.BaseController;
import com.backlogr.core.workspace.WorkspaceCore;
import com.backlogr.dto.workspace.CreateWorkspaceRequest;
import com.backlogr.dto.workspace.InviteMemberRequest;
import com.backlogr.dto.workspace.WorkspaceMemberResponse;
import com.backlogr.dto.workspace.WorkspaceResponse;
import com.backlogr.shared.HttpStatus;
import com.backlogr.shared.Result;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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

import java.util.List;
import java.util.UUID;

@Path("/api/v1/workspaces")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Workspaces", description = "Workspace management operations")
@Authenticated
@SecurityRequirement(name = "jwt")
@RunOnVirtualThread
public class WorkspaceController extends BaseController {

    @Inject
    WorkspaceCore workspaceCore;

    @Inject
    JsonWebToken jwt;

    @POST
    @Operation(summary = "Create a workspace", description = "Creates a workspace and automatically adds the owner as a member.")
    @APIResponses({
        @APIResponse(responseCode = HttpStatus.CREATED, description = HttpStatus.Description.CREATED,
            content = @Content(schema = @Schema(implementation = WorkspaceResponse.class))),
        @APIResponse(responseCode = HttpStatus.BAD_REQUEST,           description = HttpStatus.Description.BAD_REQUEST),
        @APIResponse(responseCode = HttpStatus.FORBIDDEN,             description = HttpStatus.Description.FORBIDDEN),
        @APIResponse(responseCode = HttpStatus.NOT_FOUND,             description = HttpStatus.Description.NOT_FOUND),
        @APIResponse(responseCode = HttpStatus.UNPROCESSABLE_ENTITY,  description = HttpStatus.Description.UNPROCESSABLE_ENTITY)
    })
    public Response createWorkspace(@Valid CreateWorkspaceRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Result<WorkspaceResponse> result = workspaceCore.createWorkspace(userId, request);
        return toResponse(result);
    }

    @GET
    @Path("/{workspaceId}")
    @Operation(summary = "Get workspace details")
    @APIResponses({
        @APIResponse(responseCode = HttpStatus.OK, description = HttpStatus.Description.OK,
            content = @Content(schema = @Schema(implementation = WorkspaceResponse.class))),
        @APIResponse(responseCode = HttpStatus.NOT_FOUND, description = HttpStatus.Description.NOT_FOUND)
    })
    public Response getWorkspace(@PathParam("workspaceId") UUID workspaceId) {
        Result<WorkspaceResponse> result = workspaceCore.getWorkspace(workspaceId);
        return toResponse(result);
    }

    @GET
    @Path("/{workspaceId}/members")
    @Operation(summary = "List all members of a workspace")
    @APIResponses({
        @APIResponse(responseCode = HttpStatus.OK, description = HttpStatus.Description.OK,
            content = @Content(schema = @Schema(implementation = WorkspaceMemberResponse.class))),
        @APIResponse(responseCode = HttpStatus.NOT_FOUND, description = HttpStatus.Description.NOT_FOUND)
    })
    public Response getMembers(@PathParam("workspaceId") UUID workspaceId) {
        Result<List<WorkspaceMemberResponse>> result = workspaceCore.getMembers(workspaceId);
        return toResponse(result);
    }

    @POST
    @Path("/{workspaceId}/invite")
    @Operation(summary = "Invite a member to a workspace", description = "Only the workspace owner can invite members.")
    @APIResponses({
        @APIResponse(responseCode = HttpStatus.CREATED, description = HttpStatus.Description.CREATED,
            content = @Content(schema = @Schema(implementation = WorkspaceMemberResponse.class))),
        @APIResponse(responseCode = HttpStatus.FORBIDDEN,  description = HttpStatus.Description.FORBIDDEN),
        @APIResponse(responseCode = HttpStatus.NOT_FOUND,  description = HttpStatus.Description.NOT_FOUND),
        @APIResponse(responseCode = HttpStatus.CONFLICT,   description = HttpStatus.Description.CONFLICT)
    })
    public Response inviteMember(@PathParam("workspaceId") UUID workspaceId, @Valid InviteMemberRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Result<WorkspaceMemberResponse> result = workspaceCore.inviteMember(userId, workspaceId, request);
        return toResponse(result);
    }

    @DELETE
    @Path("/{workspaceId}/members/{userId}")
    @Operation(summary = "Remove a member from a workspace", description = "Only the workspace owner can remove members. The owner cannot be removed.")
    @APIResponses({
        @APIResponse(responseCode = HttpStatus.NO_CONTENT, description = HttpStatus.Description.NO_CONTENT),
        @APIResponse(responseCode = HttpStatus.BAD_REQUEST, description = HttpStatus.Description.BAD_REQUEST),
        @APIResponse(responseCode = HttpStatus.FORBIDDEN,   description = HttpStatus.Description.FORBIDDEN),
        @APIResponse(responseCode = HttpStatus.NOT_FOUND,   description = HttpStatus.Description.NOT_FOUND)
    })
    public Response removeMember(@PathParam("workspaceId") UUID workspaceId, @PathParam("userId") UUID targetUserId) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        Result<Void> result = workspaceCore.removeMember(authenticatedUserId, workspaceId, targetUserId);
        if (!result.isSuccess()) return toResponse(result);
        return Response.noContent().build();
    }
}
