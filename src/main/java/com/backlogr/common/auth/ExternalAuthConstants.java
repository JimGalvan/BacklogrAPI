package com.backlogr.common.auth;

public final class ExternalAuthConstants {

    private ExternalAuthConstants() {}

    public static final String ATLASSIAN_AUTH_URL = "https://auth.atlassian.com/authorize";

    public static final String JIRA_SCOPES =
            "read:jira-work read:jira-user " +
            "read:issue:jira read:issue:jira-software read:project:jira read:user:jira " +
            "read:issue-status:jira read:comment:jira read:issue-details:jira read:attachment:jira " +
            "read:board-scope:jira-software offline_access";
}
