package com.backlogr.repository.user;

import com.backlogr.domain.entities.user.User;
import com.backlogr.domain.entities.user.UserIntegration;
import com.backlogr.domain.enums.Provider;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserIntegrationRepository implements PanacheRepository<UserIntegration> {

    public Optional<UserIntegration> findByUserAndProvider(User user, Provider provider) {
        return find("user = ?1 and provider = ?2", user, provider).firstResultOptional();
    }

    public Optional<UserIntegration> findByUserIdAndProvider(UUID userId, Provider provider) {
        return find("user.id = ?1 and provider = ?2", userId, provider).firstResultOptional();
    }
}
