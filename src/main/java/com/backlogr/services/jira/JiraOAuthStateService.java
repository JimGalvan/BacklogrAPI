package com.backlogr.services.jira;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class JiraOAuthStateService {

    private static final int STATE_TTL_MINUTES = 10;

    private record StateEntry(UUID userId, Instant expiry) {}

    private final ConcurrentHashMap<String, StateEntry> states = new ConcurrentHashMap<>();

    public String generateState(UUID userId) {
        String state = UUID.randomUUID().toString();
        states.put(state, new StateEntry(userId, Instant.now().plus(STATE_TTL_MINUTES, ChronoUnit.MINUTES)));
        return state;
    }

    /**
     * Validates the state was issued by this server and has not expired.
     * Removes it on first use to prevent replay. Returns the associated userId on success.
     */
    public Optional<UUID> validateAndConsume(String state) {
        if (state == null) return Optional.empty();
        StateEntry entry = states.remove(state);
        if (entry == null || Instant.now().isAfter(entry.expiry())) return Optional.empty();
        return Optional.of(entry.userId());
    }
}
