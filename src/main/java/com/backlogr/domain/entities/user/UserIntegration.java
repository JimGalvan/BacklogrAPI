package com.backlogr.domain.entities.user;

import com.backlogr.domain.entities.BaseEntity;
import com.backlogr.domain.enums.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
    name = "user_integrations",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_integration_provider",
        columnNames = {"user_id", "provider"}
    )
)
public class UserIntegration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    public User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Provider provider;

    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false)
    public String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    public String refreshToken;

    @Column(name = "cloud_id")
    public String cloudId;

    @Column(name = "external_account_id")
    public String externalAccountId;

    @Column(name = "token_expiry")
    public Instant tokenExpiry;
}
