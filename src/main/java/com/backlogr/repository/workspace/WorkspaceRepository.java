package com.backlogr.repository.workspace;

import com.backlogr.domain.workspace.Workspace;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class WorkspaceRepository implements PanacheRepository<Workspace> {

    public Optional<Workspace> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public List<Workspace> findAllByIds(List<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        return list("id IN ?1", ids);
    }
}
