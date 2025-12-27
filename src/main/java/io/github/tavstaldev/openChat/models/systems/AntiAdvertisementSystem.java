package io.github.tavstaldev.openChat.models.systems;

import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.config.ModerationConfig;
import io.github.tavstaldev.openChat.models.FilterResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * System for detecting and preventing advertisements in chat messages.
 * Uses configurable regular expressions to identify advertisements and whitelist patterns.
 */
public class AntiAdvertisementSystem {
    private final Pattern adPattern; // Pattern to detect advertisements.
    private final Pattern whitelistPattern; // Pattern to detect whitelisted content.
    private final String highlightStart;
    private final String highlightEnd;

    /**
     * Constructor for AntiAdvertisementSystem.
     * Initializes the advertisement and whitelist patterns based on the plugin configuration.
     */
    public AntiAdvertisementSystem() {
        ModerationConfig config = OpenChat.moderationConfig();
        adPattern = Pattern.compile(
                config.antiAdvertisementRegex, // Regex for detecting advertisements.
                Pattern.CASE_INSENSITIVE // Case-insensitive matching.
        );

        // Combine all whitelist entries into a single regex pattern.
        String combinedWhitelistRegex = config.antiAdvertisementWhitelist.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        whitelistPattern = Pattern.compile(combinedWhitelistRegex, Pattern.CASE_INSENSITIVE);

        PluginTranslator translator = OpenChat.translator();
        highlightStart = translator.localize("Logging.Highlight.Start");
        highlightEnd = translator.localize("Logging.Highlight.End");
    }

    /**
     * Checks if a given message contains an advertisement.
     * Whitelisted content is removed from the message before checking.
     *
     * @param message The message to check for advertisements.
     * @return True if the message contains an advertisement, false otherwise.
     */
    public boolean containsAdvertisement(String message) {
        // Remove whitelisted content from the message.
        String sanitizedMessage = whitelistPattern.matcher(message).replaceAll("");
        // Check if the sanitized message matches the advertisement pattern.
        return adPattern.matcher(sanitizedMessage).find();
    }

    public FilterResult highlight(String message) {
        Matcher matcher = adPattern.matcher(message);
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;
        boolean found = false;

        while (matcher.find()) {
            result.append(message, lastIndex, matcher.start());
            result.append(highlightStart)
                    .append(message, matcher.start(), matcher.end())
                    .append(highlightEnd);
            lastIndex = matcher.end();
            found = true;
        }

        result.append(message.substring(lastIndex));
        return new FilterResult(found, result.toString());
    }

}
