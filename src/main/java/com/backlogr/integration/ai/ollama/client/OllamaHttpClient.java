package com.backlogr.integration.ai.ollama.client;

import com.backlogr.integration.ai.ollama.dto.OllamaChatRequest;
import com.backlogr.integration.ai.ollama.dto.OllamaChatResponse;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ollama-api")
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
public interface OllamaHttpClient {

    @POST
    @Path("/chat")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OllamaChatResponse> chat(OllamaChatRequest request);

    // Returns raw NDJSON lines as strings — each line is one JSON object from Ollama.
    // We deserialize manually in OllamaAiService to avoid Quarkus's NDJSON token-level
    // reader splitting individual JSON field values into separate Multi items.
    @POST
    @Path("/chat")
    @Produces("application/x-ndjson")
    Multi<String> streamChat(OllamaChatRequest request);
}
