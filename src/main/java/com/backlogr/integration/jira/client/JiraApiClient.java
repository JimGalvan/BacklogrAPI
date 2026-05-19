package com.backlogr.integration.jira.client;

import com.backlogr.integration.jira.dto.JiraIssueResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "atlassian-api")
@Produces(MediaType.APPLICATION_JSON)
public interface JiraApiClient {

    @GET
    @Path("/ex/jira/{cloudId}/rest/api/3/issue/{key}")
    JiraIssueResponse getIssue(
        @PathParam("cloudId") String cloudId,
        @PathParam("key") String key,
        @HeaderParam("Authorization") String authorization
    );
}
