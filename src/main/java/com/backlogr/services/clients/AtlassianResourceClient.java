package com.backlogr.services.clients;

import com.backlogr.domain.dto.jira.AtlassianResource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/oauth/token/accessible-resources")
@RegisterRestClient(configKey = "atlassian-api")
public interface AtlassianResourceClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<AtlassianResource> getAccessibleResources(@HeaderParam("Authorization") String authorization);
}
