package com.backlogr.domain.ticket;

import com.backlogr.domain.BaseEntity;
import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketSource;
import com.backlogr.enums.ticket.TicketStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "tickets",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_ticket_external_id_source",
        columnNames = {"external_id", "source"}
    )
)
public class Ticket extends BaseEntity {

    @Column(name = "external_id", nullable = false)
    public String externalId;

    @Column(nullable = false)
    public String url;

    @Column(nullable = false, length = 255)
    public String title;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TicketSource source;

    public String assignee;

    @Column(name = "story_points")
    public Integer storyPoints;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ticket_tags", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "tag")
    public List<String> tags = new ArrayList<>();
}
