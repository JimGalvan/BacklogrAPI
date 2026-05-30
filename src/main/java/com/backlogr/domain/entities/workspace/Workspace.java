package com.backlogr.domain.entities.workspace;

import com.backlogr.domain.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "workspaces")
public class Workspace extends BaseEntity {

    @Column(nullable = false)
    public String name;

    @Column(name = "owner_id", nullable = false, updatable = false)
    public UUID ownerId;
}
