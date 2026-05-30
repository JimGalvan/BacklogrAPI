package com.backlogr.domain.ai;

public record AiMessage(String role, String content) {

    public static AiMessage user(String content) {
        return new AiMessage("user", content);
    }

    public static AiMessage system(String content) {
        return new AiMessage("system", content);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage("assistant", content);
    }
}
