package com.backlogr.domain.ai;

import java.util.ArrayList;
import java.util.List;

public record Prompt(List<AiMessage> messages) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final List<AiMessage> messages = new ArrayList<>();

        public Builder system(String content) {
            return add(AiMessage.system(content));
        }

        public Builder user(String content) {
            return add(AiMessage.user(content));
        }

        public Builder add(AiMessage message) {
            messages.add(message);
            return this;
        }

        public Builder addAll(List<AiMessage> toAdd) {
            messages.addAll(toAdd);
            return this;
        }

        public Prompt build() {
            return new Prompt(List.copyOf(messages));
        }
    }
}
