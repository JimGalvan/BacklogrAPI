package com.backlogr.services.ai;

import com.backlogr.domain.enums.AiModelProvider;
import com.backlogr.domain.ai.Prompt;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface AiService {

    boolean supports(AiModelProvider provider);

    AiModelProvider getProvider();

    /** Waits for the full response before returning. */
    Uni<String> ask(Prompt prompt);

    /** Emits one token at a time as the model generates it. */
    Multi<String> stream(Prompt prompt);
}
