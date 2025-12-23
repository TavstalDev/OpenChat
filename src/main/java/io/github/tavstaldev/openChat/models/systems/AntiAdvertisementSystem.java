package io.github.tavstaldev.openChat.models.systems;

import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.config.ModerationConfig;

import java.util.regex.Pattern;

/**
 * System for detecting and preventing advertisements in chat messages.
 * Uses configurable regular expressions to identify advertisements and whitelist patterns.
 */
public class AntiAdvertisementSystem {
    private final Pattern adPattern; // Pattern to detect advertisements.
    private final Pattern whitelistPattern; // Pattern to detect whitelisted content.

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
        String combinedWhitelistRegex = "(" + String.join("|", config.antiAdvertisementWhitelist) + ")";
        whitelistPattern = Pattern.compile(combinedWhitelistRegex, Pattern.CASE_INSENSITIVE);
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
        return adPattern.matcher(sanitizedMessage).matches();
    }
}
