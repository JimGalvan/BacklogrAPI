package com.backlogr.core.ticket;

import com.backlogr.enums.ticket.TicketSource;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TicketUrlParser {

    private static final Pattern JIRA = Pattern.compile("https://[^/]+\\.atlassian\\.net/browse/([A-Z]+-\\d+)");

    private TicketUrlParser() {}

    public static Optional<ParsedTicketUrl> parse(String url) {
        Matcher jira = JIRA.matcher(url);
        if (jira.find()) {
            return Optional.of(new ParsedTicketUrl(TicketSource.JIRA, jira.group(1)));
        }

        return Optional.empty();
    }

    public record ParsedTicketUrl(TicketSource source, String key) {}
}
