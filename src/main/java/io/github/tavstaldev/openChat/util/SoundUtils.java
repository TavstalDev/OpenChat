package io.github.tavstaldev.openChat.util;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class for handling sound-related operations.
 * Provides methods to retrieve sound objects based on their names.
 */
public class SoundUtils {
    /** Logger instance for logging messages related to SoundUtils. */
    private static final PluginLogger _logger = OpenChat.logger().withModule(SoundUtils.class);
    private static final Set<String> cachedSoundNames = new LinkedHashSet<>();

    /**
     * Retrieves a sound object based on its name with default volume and pitch.
     *
     * @param name The name of the sound to retrieve.
     * @return An Optional containing the Sound object if found, or an empty Optional if not.
     */
    public static Optional<Sound> getSound(@NotNull String name) {
        return getSound(name, 1.0f, 1.0f);
    }

    /**
     * Retrieves a sound object based on its name, volume, and pitch.
     *
     * @param name   The name of the sound to retrieve.
     * @param volume The volume of the sound.
     * @param pitch  The pitch of the sound.
     * @return An Optional containing the Sound object if found, or an empty Optional if not.
     */
    public static Optional<Sound> getSound(@NotNull String name, float volume, float pitch) {
        @SuppressWarnings("PatternValidation")
        String key = name.toLowerCase(Locale.ROOT);
        try {
            // Return an empty Optional if the name is "none"
            if ("none".equalsIgnoreCase(key))
                return Optional.empty();

            // Replace underscores with dots in the sound name
            if (key.contains("_"))
                key = key.replace("_", ".");

            @SuppressWarnings("PatternValidation")
            Key soundKey = Key.key(key);

            // Create and return the Sound object
            return Optional.of(Sound.sound(
                    soundKey,
                    Sound.Source.MASTER,
                    volume,
                    pitch
            ));
        } catch (Exception ex) {
            // Log the exception and return an empty Optional
            _logger.debug("Failed to get sound for name: " + name);
            _logger.debug("Exception: " + ex.getMessage());
            return Optional.empty();
        }
    }

    public static Set<String> getAllSoundNames() {
        if (cachedSoundNames.isEmpty()) {
            for (var key : Registry.SOUNDS.keyStream().toList()) {
                cachedSoundNames.add(key.examinableName());
            }
        }
        return cachedSoundNames;
    }
}
