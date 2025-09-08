package io.github.tavstaldev.openChat.models;

import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AntiSwearSystem {
    private Set<String> bannedWords;

    public AntiSwearSystem() {
        Map<Character, String> characterMappings = getCharacterMappingsFromConfig();
        for (String word : OpenChat.Config().getStringList("antiSwear.swearWords")) {

        }
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
