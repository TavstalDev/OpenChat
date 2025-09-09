package io.github.tavstaldev.openChat.models;

import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class AntiSwearSystem {
    private final Pattern combinedBannedWordsPattern;
    private final Pattern whitelistPattern;

    public AntiSwearSystem() {
        Set<String> bannedWords = new HashSet<>();
        Map<Character, String> characterMappings = getCharacterMappingsFromConfig();
        for (String word : OpenChat.Config().getStringList("antiSwear.swearWords")) {
            StringBuilder regexWord = new StringBuilder();
            for (char c : word.toCharArray()) {
                if (characterMappings.containsKey(c)) {
                    regexWord.append(characterMappings.get(c));
                } else {
                    regexWord.append(Pattern.quote(String.valueOf(c))); // Escape special regex characters
                }
            }
            bannedWords.add(regexWord.toString());
        }

        String combinedWhitelistRegex = "(" + String.join("|", OpenChat.Config().getStringList("antiSwear.whitelist")) + ")";
        whitelistPattern = Pattern.compile(combinedWhitelistRegex, Pattern.CASE_INSENSITIVE);

        String combinedRegex = String.join("|", bannedWords);
        this.combinedBannedWordsPattern = Pattern.compile("(?i).*(?:" + combinedRegex + ").*", Pattern.CASE_INSENSITIVE);
    }

    public boolean containsSwearWord(String message) {
        String sanitizedMessage = whitelistPattern.matcher(message).replaceAll("");
        return combinedBannedWordsPattern.matcher(sanitizedMessage).matches();
    }

    private Map<Character, String> getCharacterMappingsFromConfig() {
        FileConfiguration config = OpenChat.Config();

        ConfigurationSection section = config.getConfigurationSection("antiSwear.characterMapping");

        Map<Character, String> mappings = new HashMap<>();

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
