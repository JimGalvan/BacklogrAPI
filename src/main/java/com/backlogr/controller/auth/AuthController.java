package com.backlogr.controller.auth;

import com.backlogr.controller.BaseController;
import com.backlogr.core.auth.AuthCore;
import com.backlogr.dto.auth.LoginRequest;
import com.backlogr.dto.auth.LoginResponse;
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

@Path("/api/v1/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Authentication operations")
public class AuthController extends BaseController {

    @Inject
    AuthCore authCore;

    @POST
    @Path("/login")
    @Operation(
        summary = "Login",
        description = "Authenticates a user by email and password and returns a signed JWT."
    )
    @APIResponses({
        @APIResponse(
            responseCode = HttpStatus.OK,
            description = HttpStatus.Description.OK,
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @APIResponse(responseCode = HttpStatus.UNAUTHORIZED,         description = HttpStatus.Description.UNAUTHORIZED),
        @APIResponse(responseCode = HttpStatus.UNPROCESSABLE_ENTITY, description = HttpStatus.Description.UNPROCESSABLE_ENTITY)
    })
    public Response login(@Valid LoginRequest request) {
        Result<LoginResponse> result = authCore.login(request);
        return toResponse(result);
    }
}
