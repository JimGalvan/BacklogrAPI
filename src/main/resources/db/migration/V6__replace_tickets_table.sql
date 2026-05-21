DROP TABLE IF EXISTS ticket_tags;
DROP TABLE IF EXISTS tickets;

CREATE TABLE tickets (
    id                UUID                     NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    ticket_key        VARCHAR(255)             NOT NULL,
    workspace_id      UUID                     NOT NULL,
    imported_by       UUID,
    project_key       VARCHAR(255)             NOT NULL,
    summary           VARCHAR(255)             NOT NULL,
    external_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_tickets              PRIMARY KEY (id),
    CONSTRAINT uq_ticket_key_workspace UNIQUE (ticket_key, workspace_id),
    CONSTRAINT fk_tickets_workspace    FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_tickets_imported_by  FOREIGN KEY (imported_by)  REFERENCES users (id) ON DELETE SET NULL
);
