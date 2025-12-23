package io.github.tavstaldev.openChat.util;

import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.config.GeneralConfig;
import io.github.tavstaldev.openChat.config.ModerationConfig;
import io.github.tavstaldev.openChat.managers.IPermissionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Utility class for player-related operations.
 */
public class PlayerUtil {

    /**
     * Retrieves the plain text display name of a player.
     *
     * @param player The player whose display name is to be retrieved.
     * @return The plain text representation of the player's display name.
     */
    public static String getPlayerPlainDisplayName(Player player) {
        Component component = player.displayName();
        // Serialize the Component to a plain String
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static String getChatFormat(Player player) {
        IPermissionManager permissionManager = OpenChat.permissionManager();
        GeneralConfig config = OpenChat.config();
        if (!config.customChatEnableGroupFormats) {
            return config.customChatFormat;
        }

        if (permissionManager.hasPermissions()) {
            String group = permissionManager.getPrimaryGroup(player);
            String format = config.customChatGroupFormats.get(group);
            if (format != null) {
                return format;
            }
        }

        return config.customChatFormat;
    }
}
