package com.backlogr.domain.entities.user;

import com.backlogr.domain.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column
    public String name;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    public String refreshToken;

    @Column(name = "refresh_token_expiry")
    public Instant refreshTokenExpiry;
}
