package com.backlogr.services.clients;

import com.backlogr.domain.dto.jira.AtlassianTokenResponse;
import com.backlogr.domain.dto.auth.TokenExchangeRequest;
import com.backlogr.domain.dto.auth.TokenRefreshRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/oauth/token")
@RegisterRestClient(configKey = "atlassian-auth")
public interface AtlassianAuthClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    AtlassianTokenResponse exchangeCode(TokenExchangeRequest request);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    AtlassianTokenResponse refreshToken(TokenRefreshRequest request);
}
