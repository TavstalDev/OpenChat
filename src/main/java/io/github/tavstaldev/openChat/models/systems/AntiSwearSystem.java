package io.github.tavstaldev.openChat.models.systems;

import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.models.FilterResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * System for detecting and preventing the use of swear words in chat messages.
 * Uses configurable patterns for banned words and whitelisted content.
 */
public class AntiSwearSystem {
    private final Pattern combinedBannedWordsPattern; // Pattern to detect banned words.
    private final Pattern highlightPattern; // Pattern to highlight banned words.
    private final Pattern whitelistPattern; // Pattern to detect whitelisted content.
    private final String highlightStart;
    private final String highlightEnd;

    /**
     * Constructor for AntiSwearSystem.
     * Initializes the banned words and whitelist patterns based on the plugin configuration.
     */
    public AntiSwearSystem() {
        Set<String> bannedWords = new HashSet<>();
        Map<Character, String> characterMappings = getCharacterMappingsFromConfig();

        // Build regex patterns for banned words using character mappings.
        for (String word : OpenChat.badWordsConfig().getStringList("blacklist")) {
            StringBuilder regexWord = new StringBuilder();
            for (char c : word.toCharArray()) {
                if (characterMappings.containsKey(c)) {
                    regexWord.append(characterMappings.get(c));
                } else {
                    regexWord.append(Pattern.quote(String.valueOf(c))); // Escape special regex characters.
                }
            }
            bannedWords.add(regexWord.toString());
        }

        // Combine all whitelist entries into a single regex pattern.
        String combinedWhitelistRegex = OpenChat.badWordsConfig().getStringList("whitelist").stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        whitelistPattern = Pattern.compile(combinedWhitelistRegex, Pattern.CASE_INSENSITIVE);

        // Combine all banned words into a single regex pattern.
        String combinedRegex = String.join("|", bannedWords);
        this.combinedBannedWordsPattern = Pattern.compile("(?i).*(?:" + combinedRegex + ").*", Pattern.CASE_INSENSITIVE);
        this.highlightPattern = Pattern.compile("(?i)(" + combinedRegex + ")", Pattern.CASE_INSENSITIVE);

        PluginTranslator translator = OpenChat.translator();
        highlightStart = translator.localize("Logging.Highlight.Start");
        highlightEnd = translator.localize("Logging.Highlight.End");
    }

    /**
     * Checks if a given message contains a swear word.
     * Whitelisted content is removed from the message before checking.
     *
     * @param message The message to check for swear words.
     * @return True if the message contains a swear word, false otherwise.
     */
    public boolean containsSwearWord(String message) {
        // Remove whitelisted content from the message.
        String sanitizedMessage = whitelistPattern.matcher(message).replaceAll("");
        // Check if the sanitized message matches the banned words pattern.
        return combinedBannedWordsPattern.matcher(sanitizedMessage).matches();
    }

    public FilterResult highlight(String message) {
        Matcher matcher = highlightPattern.matcher(message);
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

    /**
     * Retrieves character mappings from the plugin configuration.
     * These mappings are used to handle alternative representations of characters in banned words.
     *
     * @return A map of characters to their regex representations.
     */
    private Map<Character, String> getCharacterMappingsFromConfig() {
        FileConfiguration config = OpenChat.moderationConfig();
        ConfigurationSection section = config.getConfigurationSection("antiSwear.characterMapping");
        Map<Character, String> mappings = new HashMap<>();

        // Populate the mappings from the configuration section.
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (key.length() == 1) {
                    char charKey = key.charAt(0);
                    String regexValue = section.getString(key);
                    mappings.put(charKey, regexValue);
                }
            }
        }

        return mappings;
    }
}
