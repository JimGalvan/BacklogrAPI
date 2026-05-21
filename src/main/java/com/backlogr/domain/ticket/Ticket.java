package com.backlogr.domain.ticket;

import com.backlogr.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "tickets",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_ticket_key_workspace",
        columnNames = {"ticket_key", "workspace_id"}
    )
)
public class Ticket extends BaseEntity {

    @Column(name = "ticket_key", nullable = false)
    public String ticketKey;

    @Column(name = "workspace_id", nullable = false)
    public UUID workspaceId;

    @Column(name = "imported_by")
    public UUID importedBy;

    @Column(name = "project_key", nullable = false)
    public String projectKey;

    @Column(nullable = false)
    public String summary;

    @Column(name = "external_created_at", nullable = false)
    public Instant externalCreatedAt;
}
