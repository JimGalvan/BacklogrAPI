package com.backlogr.common;

import com.backlogr.domain.dto.ticket.TicketCommentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Renders ticket content into plain text suitable for prompts. Handles Jira's
 * Atlassian Document Format (ADF) for descriptions and comment bodies.
 */
@ApplicationScoped
public class TicketContentFormatter {

    public String formatComments(List<TicketCommentResponse> comments) {
        if (comments.isEmpty()) return "No comments.";

        return IntStream.range(0, comments.size())
                .mapToObj(index -> {
                    TicketCommentResponse comment = comments.get(index);
                    String author = comment.authorName() != null ? comment.authorName() : "Unknown";
                    String body = extractText(comment.body());
                    String id = comment.id();
                    return "[%d] - id:[%s] %s: %s".formatted(index + 1, id, author, body);
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Recursively walks a JsonNode produced by Jira's Atlassian Document Format
     * and returns the visible plain text, preserving paragraph and list breaks.
     */
    public String extractText(JsonNode node) {
        if (node == null || node.isNull()) return "";

        String type = node.path("type").asText("");

        // leaf: plain text node
        if ("text".equals(type)) {
            return node.path("text").asText("");
        }

        // leaf: inline nodes with no useful text
        if ("hardBreak".equals(type) || "rule".equals(type)) {
            return "\n";
        }

        // recurse into content array
        JsonNode content = node.path("content");
        if (content.isMissingNode() || !content.isArray()) return "";

        StringBuilder builder = new StringBuilder();
        content.forEach(child -> {
            String childText = extractText(child);
            if (!childText.isBlank()) {
                builder.append(childText);
            }
        });

        // block-level nodes get a trailing newline so paragraphs/list items are separated
        String result = builder.toString();
        boolean isBlock = switch (type) {
            case "paragraph", "heading", "listItem",
                 "bulletList", "orderedList", "blockquote",
                 "codeBlock", "panel" -> true;
            default -> false;
        };

        return isBlock ? result + "\n" : result;
    }
}
