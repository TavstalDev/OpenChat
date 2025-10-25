package io.github.tavstaldev.openChat.models.systems;

import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * System for detecting and preventing the use of swear words in chat messages.
 * Uses configurable patterns for banned words and whitelisted content.
 */
public class AntiSwearSystem {
    private final Pattern combinedBannedWordsPattern; // Pattern to detect banned words.
    private final Pattern whitelistPattern; // Pattern to detect whitelisted content.

    /**
     * Constructor for AntiSwearSystem.
     * Initializes the banned words and whitelist patterns based on the plugin configuration.
     */
    public AntiSwearSystem() {
        Set<String> bannedWords = new HashSet<>();
        Map<Character, String> characterMappings = getCharacterMappingsFromConfig();

        // Build regex patterns for banned words using character mappings.
        for (String word : OpenChat.config().getStringList("antiSwear.swearWords")) {
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
        String combinedWhitelistRegex = "(" + String.join("|", OpenChat.config().getStringList("antiSwear.whitelist")) + ")";
        whitelistPattern = Pattern.compile(combinedWhitelistRegex, Pattern.CASE_INSENSITIVE);

        // Combine all banned words into a single regex pattern.
        String combinedRegex = String.join("|", bannedWords);
        this.combinedBannedWordsPattern = Pattern.compile("(?i).*(?:" + combinedRegex + ").*", Pattern.CASE_INSENSITIVE);
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

    /**
     * Retrieves character mappings from the plugin configuration.
     * These mappings are used to handle alternative representations of characters in banned words.
     *
     * @return A map of characters to their regex representations.
     */
    private Map<Character, String> getCharacterMappingsFromConfig() {
        FileConfiguration config = OpenChat.config();
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
