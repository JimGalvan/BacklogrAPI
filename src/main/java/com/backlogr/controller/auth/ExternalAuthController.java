package com.backlogr.controller.auth;

import com.backlogr.controller.BaseController;
import com.backlogr.core.auth.ExternalAuthCore;
import com.backlogr.shared.HttpStatus;
import com.backlogr.shared.Result;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.UUID;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "External Auth", description = "OAuth callbacks for external integrations")
@SecurityScheme(securitySchemeName = "jwt", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class ExternalAuthController extends BaseController {

    @Inject
    ExternalAuthCore externalAuthCore;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/{provider}/connect")
    @Authenticated
    @SecurityRequirement(name = "jwt")
    @Operation(
        summary = "Initiate OAuth flow for an external provider",
        description = "Generates a CSRF state token, stores it server-side, and redirects to the provider's authorization page. Supported providers: jira."
    )
    @APIResponses({
        @APIResponse(responseCode = "302",                   description = "Redirect to provider authorization page"),
        @APIResponse(responseCode = HttpStatus.UNAUTHORIZED, description = HttpStatus.Description.UNAUTHORIZED),
        @APIResponse(responseCode = HttpStatus.NOT_FOUND,    description = HttpStatus.Description.NOT_FOUND)
    })
    public Response connect(
        @Parameter(description = "External provider name (e.g. jira)")
        @PathParam("provider") String provider
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Result<URI> result = externalAuthCore.buildAuthorizationUrl(userId, provider);
        if (!result.isSuccess()) return toResponse(result);
        return Response.seeOther(result.getValue()).build();
    }

    @GET
    @Path("/{provider}/callback")
    @Operation(
        summary = "OAuth callback for an external provider",
        description = "Receives the authorization code from the provider, validates state, exchanges the code for tokens, and persists the integration. Supported providers: jira."
    )
    @APIResponses({
        @APIResponse(responseCode = HttpStatus.OK,                    description = HttpStatus.Description.OK),
        @APIResponse(responseCode = HttpStatus.BAD_REQUEST,           description = HttpStatus.Description.BAD_REQUEST),
        @APIResponse(responseCode = HttpStatus.INTERNAL_SERVER_ERROR, description = HttpStatus.Description.INTERNAL_SERVER_ERROR)
    })
    public Response handleCallback(
        @Parameter(description = "External provider name (e.g. jira)")
        @PathParam("provider") String provider,
        @Parameter(description = "Authorization code returned by the provider")
        @QueryParam("code") String code,
        @Parameter(description = "State token for CSRF verification")
        @QueryParam("state") String state
    ) {
        Result<String> result = externalAuthCore.handleCallback(provider, code, state);
        return toResponse(result);
    }
}
