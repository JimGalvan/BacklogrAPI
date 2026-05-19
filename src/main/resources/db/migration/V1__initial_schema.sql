CREATE TABLE users (
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE   NOT NULL,
    email           VARCHAR(255)                NOT NULL,
    password_hash   VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE user_integrations (
    id                  UUID                        NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE    NOT NULL,
    last_modified_at    TIMESTAMP WITH TIME ZONE    NOT NULL,
    user_id             UUID                        NOT NULL,
    provider            VARCHAR(255)                NOT NULL,
    access_token        TEXT                        NOT NULL,
    refresh_token       TEXT,
    workspace_id        VARCHAR(255),
    external_account_id VARCHAR(255),
    token_expiry        TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_user_integrations PRIMARY KEY (id),
    CONSTRAINT fk_user_integrations_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_user_integration_provider UNIQUE (user_id, provider)
);

CREATE TABLE tickets (
    id               UUID                        NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE    NOT NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE    NOT NULL,
    external_id      VARCHAR(255)                NOT NULL,
    url              VARCHAR(255)                NOT NULL,
    title            VARCHAR(255)                NOT NULL,
    description      TEXT,
    status           VARCHAR(255)                NOT NULL,
    priority         VARCHAR(255)                NOT NULL,
    source           VARCHAR(255)                NOT NULL,
    assignee         VARCHAR(255),
    story_points     INTEGER,
    CONSTRAINT pk_tickets PRIMARY KEY (id),
    CONSTRAINT uq_ticket_external_id_source UNIQUE (external_id, source)
);

CREATE TABLE ticket_tags (
    ticket_id UUID         NOT NULL,
    tag       VARCHAR(255),
    CONSTRAINT fk_ticket_tags_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id)
);
