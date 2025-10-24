package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.models.PlayerCache;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Listener for handling player-related events in the OpenChat plugin.
 * Manages player cache on join and quit events.
 */
public class PlayerEventListener implements Listener {

    /**
     * Constructor for PlayerEventListener.
     * Registers the event listener with the Bukkit plugin manager.
     *
     * @param plugin The plugin instance to register the listener for.
     */
    public PlayerEventListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles the PlayerJoinEvent to initialize and add the player's cache.
     *
     * @param event The event triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        var playerId = player.getUniqueId();
        PlayerCache playerCache = new PlayerCache(player); // Create a new cache for the player.
        PlayerCacheManager.add(playerId, playerCache); // Add the player's cache to the manager.

        if (PlayerCacheManager.isMarkedForRemoval(playerId))
            PlayerCacheManager.unmarkForRemoval(playerId);

        // TODO: Consider making this asynchronous
        var playerData = OpenChat.database().getPlayerData(playerId);
        if (playerData.isEmpty()) {
            OpenChat.database().addPlayerData(playerId);
            // Since addPlayerData initializes it and also adds it to the cache, we can retrieve it again.
            playerData = OpenChat.database().getPlayerData(playerId);
        }

        var config = OpenChat.config();
        if (config.customGreetingEnabled && config.customGreetingOverrideJoinMessage) {
            event.joinMessage(null);
            String message;
            if (playerData.isPresent() && playerData.get().getCustomJoinMessage() != null)
                message = playerData.get().getCustomJoinMessage();
            else
                message = config.customGreetingJoinMessage;

            String playerName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
            message = PlaceholderAPI.setPlaceholders(player, message.replace("{player}", playerName));
            player.getServer().broadcast(ChatUtils.translateColors(message, true));
        }
    }

    /**
     * Handles the PlayerQuitEvent to remove the player's cache.
     *
     * @param event The event triggered when a player quits the server.
     */
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        var playerId = player.getUniqueId();

        var config = OpenChat.config();
        // TODO: Consider making this asynchronous
        if (config.customGreetingEnabled && config.customGreetingOverrideLeaveMessage) {
            event.quitMessage(null);
            String message;
            var playerData = OpenChat.database().getPlayerData(playerId);
            if (playerData.isPresent() && playerData.get().getCustomLeaveMessage() != null)
                message = playerData.get().getCustomLeaveMessage();
            else
                message = config.customGreetingLeaveMessage;

            String playerName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
            message = PlaceholderAPI.setPlaceholders(player,  message.replace("{player}", playerName));
            player.getServer().broadcast(ChatUtils.translateColors(message, true));
        }

        PlayerCacheManager.remove(player.getUniqueId()); // Remove the player's cache from the manager.
        PlayerCacheManager.markForRemoval(player.getUniqueId());
    }
}
