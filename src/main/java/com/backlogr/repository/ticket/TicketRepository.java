package com.backlogr.repository.ticket;

import com.backlogr.domain.ticket.Ticket;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TicketRepository implements PanacheRepository<Ticket> {

    public boolean existsByTicketKeyAndWorkspaceId(String ticketKey, UUID workspaceId) {
        return count("ticketKey = ?1 and workspaceId = ?2", ticketKey, workspaceId) > 0;
    }

    public Optional<Ticket> findByTicketKeyAndWorkspaceId(String ticketKey, UUID workspaceId) {
        return find("ticketKey = ?1 and workspaceId = ?2", ticketKey, workspaceId).firstResultOptional();
    }

    public List<Ticket> findByWorkspaceId(UUID workspaceId) {
        return find("workspaceId", workspaceId).list();
    }

    public List<Ticket> findByWorkspaceIdAndImportedBy(UUID workspaceId, UUID importedBy) {
        return find("workspaceId = ?1 and importedBy = ?2", workspaceId, importedBy).list();
    }
}
