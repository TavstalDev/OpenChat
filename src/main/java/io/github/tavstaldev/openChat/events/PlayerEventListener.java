package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.models.PlayerCache;
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
        Player player = event.getPlayer(); // The player who joined the server.
        var playerId = player.getUniqueId();
        PlayerCache playerCache = new PlayerCache(player); // Create a new cache for the player.
        PlayerCacheManager.add(playerId, playerCache); // Add the player's cache to the manager.

        if (PlayerCacheManager.isMarkedForRemoval(playerId))
            PlayerCacheManager.unmarkForRemoval(playerId);

        if (OpenChat.database().getPlayerData(playerId).isEmpty()) {
            OpenChat.database().addPlayerData(playerId);
        }
    }

    /**
     * Handles the PlayerQuitEvent to remove the player's cache.
     *
     * @param event The event triggered when a player quits the server.
     */
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer(); // The player who quit the server.
        PlayerCacheManager.remove(player.getUniqueId()); // Remove the player's cache from the manager.
        PlayerCacheManager.markForRemoval(player.getUniqueId());
    }
}
