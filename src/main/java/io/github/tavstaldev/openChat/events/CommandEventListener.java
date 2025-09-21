package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.models.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Listener for handling player command events in the OpenChat plugin.
 * Implements features such as command cooldowns and anti-spam for repeated commands.
 */
public class CommandEventListener implements Listener {
    private final PluginLogger _logger = OpenChat.Logger().WithModule(CommandEventListener.class);

    public CommandEventListener(Plugin plugin) {
        _logger.Debug("Registering command event listener...");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        _logger.Debug("Event listener registered.");
    }

    /**
     * Handles the PlayerCommandPreprocessEvent to enforce command cooldowns and prevent spam.
     *
     * @param event The event triggered when a player executes a command.
     */
    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // Exit early if the event is already cancelled.
        if (event.isCancelled())
            return;

        var config = OpenChat.OCConfig(); // Plugin configuration.
        if (!config.antiSpamEnabled)
            return; // Exit if anti-spam is disabled.

        var player = event.getPlayer(); // The player executing the command.

        if (player.hasPermission(config.antiSpamExemptPermission))
            return; // Exit if the player has exemption permission.

        String command = event.getMessage(); // The command message.
        // Debug log the command execution
        _logger.Debug("Player " + player.getName() + " executed command: " + command);


        PlayerCache cache = PlayerCacheManager.get(player.getUniqueId()); // Retrieve the player's cache.
        if (cache == null)
            return;

        // Feature: cooldown
        if (LocalDateTime.now().isBefore(cache.commandDelay)) {
            // Cancel the event if the player is still on cooldown.
            event.setCancelled(true);
            // The +1 ensures that it doesn't display 0 seconds remaining when the cooldown is about to expire
            OpenChat.Instance.sendLocalizedMsg(player, "AntiSpam.CommandCooldown",
                    Map.of("time", String.valueOf(cache.commandDelay.getSecond() - LocalDateTime.now().getSecond() + 1)));

            // Execute configured commands for cooldown violations.
            for (String cmd : config.antiSpamExecuteCommand) {
                Bukkit.getScheduler().runTask(OpenChat.Instance, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                });
            }
            return;
        }

        // Update the player's last executed command in the cache.
        cache.setLastCommand(command);

        // Feature: anti-spam
        if (config.antiSpamMaxDuplicates >= 1 && config.antiSpamMaxDuplicates <= cache.getCommandSpamCount()) {
            // Cancel the event if the player exceeds the allowed duplicate commands.
            event.setCancelled(true);
            OpenChat.Instance.sendLocalizedMsg(player, "AntiSpam.RepeatedCommands");

            // Execute configured commands for spam violations.
            for (String cmd : config.antiSpamExecuteCommand) {
                Bukkit.getScheduler().runTask(OpenChat.Instance, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                });
            }
            return;
        }

        // Set the next allowed command execution time based on the configured delay.
        cache.commandDelay = LocalDateTime.now().plusSeconds(config.antiSpamCommandDelay);
    }
}