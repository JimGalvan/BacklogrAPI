package com.backlogr.integration.jira.client;

import com.backlogr.integration.jira.dto.JiraIssueResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "jira-api")
@Produces(MediaType.APPLICATION_JSON)
public interface JiraApiClient {

    @GET
    @Path("/rest/api/3/issue/{key}")
    JiraIssueResponse getIssue(@PathParam("key") String key);
}
