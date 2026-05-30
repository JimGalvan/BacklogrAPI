package com.backlogr.core.ticket;

import com.backlogr.domain.enums.Provider;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TicketUrlParser {

    private static final Pattern JIRA = Pattern.compile("https://[^/]+\\.atlassian\\.net/browse/([A-Z]+-\\d+)");

    private TicketUrlParser() {}

    public static Optional<ParsedTicketUrl> parse(String url) {
        Matcher jira = JIRA.matcher(url);
        if (jira.find()) {
            return Optional.of(new ParsedTicketUrl(Provider.JIRA, jira.group(1)));
        }

        return Optional.empty();
    }

    public static final class ParsedTicketUrl {
        private final Provider provider;
        private final String key;

        public ParsedTicketUrl(Provider provider, String key) {
            this.provider = provider;
            this.key = key;
        }

        public Provider getProvider() { return provider; }
        public String getKey() { return key; }
    }
}
