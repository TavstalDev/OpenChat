package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.models.PlayerCache;
import io.github.tavstaldev.openChat.models.database.EViolationType;
import io.github.tavstaldev.openChat.util.ViolationUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.Plugin;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Listener for handling player command events in the OpenChat plugin.
 * Implements features such as command cooldowns and anti-spam for repeated commands.
 */
public class CommandEventListener implements Listener {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandEventListener.class);

    public CommandEventListener(Plugin plugin) {
        _logger.debug("Registering command event listener...");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        _logger.debug("Event listener registered.");
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

        var config = OpenChat.config(); // Plugin configuration.
        if (!config.antiSpamEnabled)
            return; // Exit if anti-spam is disabled.

        var player = event.getPlayer(); // The player executing the command.

        if (player.hasPermission(config.antiSpamExemptPermission))
            return; // Exit if the player has exemption permission.

        String command = event.getMessage(); // The command message.
        // Debug log the command execution
        _logger.debug("Player " + player.getName() + " executed command: " + command);


        PlayerCache cache = PlayerCacheManager.get(player.getUniqueId()); // Retrieve the player's cache.
        if (cache == null)
            return;

        // Feature: cooldown
        if (LocalDateTime.now().isBefore(cache.getCommandDelay())) {
            // Cancel the event if the player is still on cooldown.
            event.setCancelled(true);
            // The +1 ensures that it doesn't display 0 seconds remaining when the cooldown is about to expire
            OpenChat.Instance.sendLocalizedMsg(player, "AntiSpam.CommandCooldown",
                    Map.of("time", String.valueOf(cache.getCommandDelay().getSecond() - LocalDateTime.now().getSecond() + 1)));

            // Execute configured commands for cooldown violations.
            ViolationUtil.handleViolationAsync(player, EViolationType.SPAM_DELAY,
                    "<red>[COMMAND]:</red> " + command,
                    config.antiSpamDelayViolationActions);
            return;
        }

        // Feature: command blocker
        if (config.commandBlockerEnabled && (!config.commandBlockerEnableBypass || !player.hasPermission(config.commandBlockerBypassPermission))) {
            if (OpenChat.commandCheckerSystem().isBlocked(command)) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(player, "CommandBlocker.Blocked");
                return;
            }
        }

        // Update the player's last executed command in the cache.
        cache.setLastCommand(command);

        // Feature: anti-spam
        if (config.antiSpamMaxCommandDuplicates >= 1 && config.antiSpamMaxCommandDuplicates <= cache.getCommandSpamCount()) {
            // Cancel the event if the player exceeds the allowed duplicate commands.
            event.setCancelled(true);
            OpenChat.Instance.sendLocalizedMsg(player, "AntiSpam.RepeatedCommands");

            // Execute configured commands for spam violations.
            ViolationUtil.handleViolationAsync(player, EViolationType.SPAM_REPETITION,
                    "<red>[COMMAND]:</red> " + command,
                    config.antiSpamSimilarityViolationActions);
            return;
        }

        // Set the next allowed command execution time based on the configured delay.
        cache.setCommandDelay(LocalDateTime.now().plusSeconds(config.antiSpamCommandDelay));
    }

    /**
     * Handles the PlayerCommandSendEvent to provide custom tab completions for commands.
     *
     * @param event The event triggered when a player requests tab completions for commands.
     */
    @EventHandler
    public void onTabComplete(PlayerCommandSendEvent event) {
        if (!OpenChat.config().tabCompletionEnabled)
            return;
        var source = event.getPlayer();
        var commands = event.getCommands();
        OpenChat.commandCheckerSystem().getTabCompletions(source, commands);
    }
}