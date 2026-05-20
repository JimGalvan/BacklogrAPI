package com.backlogr.domain.workspace;

import com.backlogr.domain.BaseEntity;
import com.backlogr.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
    name = "workspace_members",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_workspace_member",
        columnNames = {"workspace_id", "user_id"}
    )
)
public class WorkspaceMember extends BaseEntity {

    @Column(name = "workspace_id", nullable = false, updatable = false)
    public UUID workspaceId;

    @Column(name = "user_id", nullable = false, updatable = false)
    public UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    public User user;
}
