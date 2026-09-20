package com.example.todo.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DescriptionLinkifier {

    private static final Pattern URL_PATTERN =
            Pattern.compile(
                    "https?://[^\\s<>\"']+",
                    Pattern.CASE_INSENSITIVE
            );

    private static final String TRAILING_PUNCTUATION =
            ".,!?;:。、】【";

    private DescriptionLinkifier() {
    }

    public static List<DescriptionPart> split(String description) {

        if (description == null || description.isEmpty()) {
            return List.of();
        }

        List<DescriptionPart> parts = new ArrayList<>();

        Matcher matcher = URL_PATTERN.matcher(description);

        int previousEnd = 0;

        while (matcher.find()) {
            String url = removeTrailingPunctuation(
                    matcher.group()
            );

            if (!isSafeHttpUrl(url)) {
                continue;
            }

            int urlEnd =
                    matcher.start() + url.length();

            if (previousEnd < matcher.start()) {
                parts.add(
                        new DescriptionPart(
                                description.substring(
                                        previousEnd,
                                        matcher.start()
                                ),
                                false
                        )
                );
            }

            parts.add(
                    new DescriptionPart(url, true)
            );

            previousEnd = urlEnd;
        }

        if (previousEnd < description.length()) {
            parts.add(
                    new DescriptionPart(
                            description.substring(previousEnd),
                            false
                    )
            );
        }

        return parts;
    }

    private static boolean isSafeHttpUrl(String value) {

        try {
            URI uri = URI.create(value);

            return uri.getHost() != null
                    && ("https".equalsIgnoreCase(
                            uri.getScheme()
                    ) || "http".equalsIgnoreCase(
                            uri.getScheme()
                    ));

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String removeTrailingPunctuation(
            String value
    ) {
        int end = value.length();

        while (end > 0
                && TRAILING_PUNCTUATION.indexOf(
                        value.charAt(end - 1)
                ) >= 0) {
            end--;
        }

        return value.substring(0, end);
    }
}