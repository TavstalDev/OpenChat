package io.github.tavstaldev.openChat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public class PlayerUtil {
    public static String getPlayerPlainDisplayName(Player player) {
        Component component = player.displayName();
        // Serialize the Component to a plain String
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
