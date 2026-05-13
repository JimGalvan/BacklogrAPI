package com.backlogr.repository.ticket;

import com.backlogr.domain.ticket.Ticket;
import com.backlogr.enums.ticket.TicketSource;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class TicketRepository implements PanacheRepository<Ticket> {

    public Optional<Ticket> findByExternalIdAndSource(String externalId, TicketSource source) {
        return find("externalId = ?1 and source = ?2", externalId, source).firstResultOptional();
    }

    public boolean existsByExternalIdAndSource(String externalId, TicketSource source) {
        return count("externalId = ?1 and source = ?2", externalId, source) > 0;
    }
}
