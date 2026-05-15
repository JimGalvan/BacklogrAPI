package com.backlogr.integration.jira.oauth.client;

import com.backlogr.integration.jira.oauth.dto.AtlassianTokenResponse;
import com.backlogr.integration.jira.oauth.dto.TokenExchangeRequest;
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
}
