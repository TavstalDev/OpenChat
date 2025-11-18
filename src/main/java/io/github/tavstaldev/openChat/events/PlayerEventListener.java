package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.models.PlayerCache;
import io.github.tavstaldev.openChat.util.VanishUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ThreadLocalRandom;

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

        if (PlayerCacheManager.isMarkedForRemoval(playerId))
            PlayerCacheManager.unmarkForRemoval(playerId);

        if (PlayerCacheManager.get(playerId) == null) {
            PlayerCache playerCache = new PlayerCache(player);
            PlayerCacheManager.add(playerId, playerCache);
        }

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
            if (!config.customGreetingIgnoreVanished || !VanishUtil.isVanished(player)) {
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

        var motds = config.customMotds;
        if (config.customMotdsEnabled && !motds.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(OpenChat.Instance, () -> {
                if (motds.size() == 1) {
                    String motd = PlaceholderAPI.setPlaceholders(player, motds.getFirst());
                    motd = motd.replace("{player}", player.getName())
                            .replace("{displayname}", PlainTextComponentSerializer.plainText().serialize(player.displayName()));
                    player.sendMessage(ChatUtils.translateColors(motd, true));
                } else {
                    int index = ThreadLocalRandom.current().nextInt(motds.size());
                    String motd = PlaceholderAPI.setPlaceholders(player, motds.get(index));
                    motd = motd.replace("{player}", player.getName())
                            .replace("{displayname}", PlainTextComponentSerializer.plainText().serialize(player.displayName()));
                    player.sendMessage(ChatUtils.translateColors(motd, true));
                }
            }, 10L);
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
            if (!config.customGreetingIgnoreVanished || !VanishUtil.isVanished(player)) {
                String message;
                var playerData = OpenChat.database().getPlayerData(playerId);
                if (playerData.isPresent() && playerData.get().getCustomLeaveMessage() != null)
                    message = playerData.get().getCustomLeaveMessage();
                else
                    message = config.customGreetingLeaveMessage;

                String playerName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
                message = PlaceholderAPI.setPlaceholders(player, message.replace("{player}", playerName));
                player.getServer().broadcast(ChatUtils.translateColors(message, true));
            }
        }

        PlayerCacheManager.markForRemoval(player.getUniqueId());
    }
}
