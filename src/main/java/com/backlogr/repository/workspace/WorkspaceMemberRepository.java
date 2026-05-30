package com.backlogr.repository.workspace;

import com.backlogr.domain.entities.workspace.WorkspaceMember;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class WorkspaceMemberRepository implements PanacheRepository<WorkspaceMember> {

    public List<WorkspaceMember> findByWorkspaceId(UUID workspaceId) {
        return find("SELECT wm FROM WorkspaceMember wm JOIN FETCH wm.user WHERE wm.workspaceId = ?1", workspaceId).list();
    }

    public Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId) {
        return find("workspaceId = ?1 and userId = ?2", workspaceId, userId).firstResultOptional();
    }

    public boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId) {
        return count("workspaceId = ?1 and userId = ?2", workspaceId, userId) > 0;
    }

    public List<UUID> findWorkspaceIdsByUserId(UUID userId) {
        return find("userId", userId).<WorkspaceMember>stream()
                .map(m -> m.workspaceId)
                .toList();
    }
}
