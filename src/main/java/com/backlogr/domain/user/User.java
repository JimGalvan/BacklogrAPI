package com.backlogr.domain.user;

import com.backlogr.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;
}
