package com.backlogr.controller.user;

import com.backlogr.controller.BaseController;
import com.backlogr.core.user.UserCore;
import com.backlogr.dto.user.CreateUserRequest;
import com.backlogr.dto.user.UserResponse;
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
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management operations")
@RunOnVirtualThread
public class UserController extends BaseController {

    @Inject
    UserCore userCore;

    @POST
    @Operation(
        summary = "Create a new user",
        description = "Registers a new user by email. The email must be unique."
    )
    @APIResponses({
        @APIResponse(
            responseCode = HttpStatus.CREATED,
            description = HttpStatus.Description.CREATED,
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @APIResponse(responseCode = HttpStatus.BAD_REQUEST,          description = HttpStatus.Description.BAD_REQUEST),
        @APIResponse(responseCode = HttpStatus.CONFLICT,             description = HttpStatus.Description.CONFLICT),
        @APIResponse(responseCode = HttpStatus.UNPROCESSABLE_ENTITY, description = HttpStatus.Description.UNPROCESSABLE_ENTITY)
    })
    public Response createUser(@Valid CreateUserRequest request) {
        Result<UserResponse> result = userCore.createUser(request);
        return toResponse(result);
    }
}
