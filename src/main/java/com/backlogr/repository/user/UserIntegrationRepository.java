package com.backlogr.repository.user;

import com.backlogr.domain.user.User;
import com.backlogr.domain.user.UserIntegration;
import com.backlogr.enums.integration.IntegrationProvider;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserIntegrationRepository implements PanacheRepository<UserIntegration> {

    public Optional<UserIntegration> findByUserAndProvider(User user, IntegrationProvider provider) {
        return find("user = ?1 and provider = ?2", user, provider).firstResultOptional();
    }

    public Optional<UserIntegration> findByUserIdAndProvider(UUID userId, IntegrationProvider provider) {
        return find("user.id = ?1 and provider = ?2", userId, provider).firstResultOptional();
    }
}
