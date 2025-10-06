package io.github.tavstaldev.openChat.util;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

/**
 * Utility class for handling sound-related operations.
 * Provides methods to retrieve sound objects based on their names.
 */
public class SoundUtils {
    /** Logger instance for logging messages related to SoundUtils. */
    private static final PluginLogger _logger = OpenChat.logger().withModule(SoundUtils.class);

    public static Optional<Sound> getSound(@NotNull String name) {
        return  getSound(name, 1.0f, 1.0f);
    }

    public static Optional<Sound> getSound(@NotNull String name, float volume, float pitch) {
        @SuppressWarnings("PatternValidation")
        String key = name.toLowerCase(Locale.ROOT);
        try {
            // Fixes null pointer exception
            if ("none".equalsIgnoreCase(key))
                return Optional.empty();

            if (key.contains("_"))
                key = key.replace("_", ".");

            @SuppressWarnings("PatternValidation")
            Key soundKey = Key.key(key);
            return Optional.of(Sound.sound(
                    soundKey,
                    Sound.Source.MASTER,
                    volume,
                    pitch
            ));
        }
        catch (Exception ex) {
            _logger.debug("Failed to get sound for name: " + name);
            _logger.debug("Exception: " + ex.getMessage());
            return Optional.empty();
        }
    }
}