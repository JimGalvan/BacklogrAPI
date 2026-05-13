package com.backlogr.core.ticket;

import com.backlogr.enums.ticket.TicketSource;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TicketUrlParser {

    private static final Pattern JIRA   = Pattern.compile("https://[^/]+\\.atlassian\\.net/browse/([A-Z]+-\\d+)");
    private static final Pattern GITHUB = Pattern.compile("https://github\\.com/([^/]+/[^/]+)/issues/(\\d+)");
    private static final Pattern LINEAR = Pattern.compile("https://linear\\.app/[^/]+/issue/([A-Z]+-\\d+)");
    private static final Pattern TRELLO = Pattern.compile("https://trello\\.com/c/([^/]+)");

    private TicketUrlParser() {}

    public static Optional<ParsedTicketUrl> parse(String url) {
        Matcher jira = JIRA.matcher(url);
        if (jira.find()) {
            return Optional.of(new ParsedTicketUrl(TicketSource.JIRA, jira.group(1)));
        }

        Matcher github = GITHUB.matcher(url);
        if (github.find()) {
            return Optional.of(new ParsedTicketUrl(TicketSource.GITHUB, github.group(1) + "#" + github.group(2)));
        }

        Matcher linear = LINEAR.matcher(url);
        if (linear.find()) {
            return Optional.of(new ParsedTicketUrl(TicketSource.LINEAR, linear.group(1)));
        }

        Matcher trello = TRELLO.matcher(url);
        if (trello.find()) {
            return Optional.of(new ParsedTicketUrl(TicketSource.TRELLO, trello.group(1)));
        }

        return Optional.empty();
    }

    public record ParsedTicketUrl(TicketSource source, String key) {}
}
