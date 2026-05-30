package com.backlogr.services.ai;

import com.backlogr.domain.enums.AiModelProvider;
import com.backlogr.domain.ai.AiMessage;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface AiService {

    boolean supports(AiModelProvider provider);

    AiModelProvider getProvider();

    /** Waits for the full response before returning. */
    Uni<String> ask(List<AiMessage> messages);

    /** Emits one token at a time as the model generates it. */
    Multi<String> stream(List<AiMessage> messages);
}
