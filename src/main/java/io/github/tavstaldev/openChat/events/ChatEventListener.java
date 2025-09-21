package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.OpenChatConfiguration;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.models.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Listener for handling chat-related events in the OpenChat plugin.
 * Implements various anti-abuse mechanisms such as anti-spam, anti-advertisement,
 * anti-capitalization, and anti-swearing.
 */
public class ChatEventListener implements Listener {
    private final PluginLogger _logger = OpenChat.Logger().WithModule(ChatEventListener.class);

    /**
     * Constructor for ChatEventListener.
     * Registers the event listener with the Bukkit plugin manager.
     *
     * @param plugin The plugin instance to register the listener for.
     */
    public ChatEventListener(Plugin plugin) {
        _logger.Debug("Registering chat event listener...");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        _logger.Debug("Event listener registered.");
    }

    /**
     * Handles the AsyncPlayerChatEvent to apply various chat moderation features.
     *
     * @param event The chat event triggered when a player sends a message.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        Player source = event.getPlayer(); // The player who sent the message.
        PlayerCache cache = PlayerCacheManager.get(source.getUniqueId()); // Retrieve the player's cache.
        String rawMessage = event.getMessage(); // The raw chat message.
        cache.setLastChatMessage(rawMessage); // Store the last chat message in the cache.
        OpenChatConfiguration config = OpenChat.OCConfig(); // Retrieve the plugin configuration.

        // Debug log the received message to find false positives
        _logger.Debug("Player " + source.getName() + " sent message: " + rawMessage);

        // Anti-spam
        if (config.antiSpamEnabled && !source.hasPermission(config.antiSpamExemptPermission)) {
            // Feature: Chat cooldown
            if (LocalDateTime.now().isBefore(cache.chatMessageDelay)) {
                event.setCancelled(true);
                // The +1 ensures that it doesn't display 0 seconds remaining when the cooldown is about to expire
                OpenChat.Instance.sendLocalizedMsg(source, "AntiSpam.ChatCooldown", Map.of("time", String.valueOf(cache.chatMessageDelay.getSecond() - LocalDateTime.now().getSecond() + 1)));
                for (String cmd : config.antiSpamExecuteCommand) {
                    Bukkit.getScheduler().runTask(OpenChat.Instance, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                    });
                }
                return;
            }

            // Feature: Repeated messages
            // TODO: Improve repeated message detection to ignore minor differences (e.g., punctuation, spacing)
            if (config.antiSpamMaxDuplicates >= 1 && cache.getChatSpamCount() >= config.antiSpamMaxDuplicates) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiSpam.RepeatedMessages");
                for (String cmd : config.antiSpamExecuteCommand) {
                    Bukkit.getScheduler().runTask(OpenChat.Instance, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                    });
                }
                return;
            }
        }

        // Anti-advertisement
        if (config.antiAdvertisementEnabled && !source.hasPermission(config.antiAdvertisementExemptPermission)) {
            if (OpenChat.AdvertisementSystem().containsAdvertisement(rawMessage)) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiAd.AdvertisementDetected");
                for (String cmd : config.antiAdvertisementExecuteCommand) {
                    Bukkit.getScheduler().runTask(OpenChat.Instance, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                    });
                }
                return;
            }
        }


        // Anti-capitalization
        if (config.antiCapsEnabled && !source.hasPermission(config.antiCapsExemptPermission)) {
            double maxCapsPercentage = config.antiCapsPercentage / 100.0; // Maximum allowed percentage of capital letters.
            if (rawMessage.length() >= config.antiCapsMinLength) { // Check if the message meets the minimum length.
                long capsCount = rawMessage.chars().filter(Character::isUpperCase).count(); // Count uppercase letters.
                double capsPercentage = (double) capsCount / rawMessage.length(); // Calculate the percentage of uppercase letters.
                if (capsPercentage > maxCapsPercentage) {
                    event.setCancelled(true);
                    OpenChat.Instance.sendLocalizedMsg(source, "AntiCaps.TooManyCaps");
                    for (String cmd : config.antiCapsExecuteCommand) {
                        Bukkit.getScheduler().runTask(OpenChat.Instance, () -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                        });
                    }
                    return;
                }
            }
        }

        // Anti-swear
        if (config.antiSwearEnabled && !source.hasPermission(config.antiSwearExemptPermission) ) {
            if (OpenChat.AntiSwearSystem().containsSwearWord(rawMessage)) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiSwear.WordDetected");
                for (String cmd : config.antiSwearExecuteCommand) {
                    Bukkit.getScheduler().runTask(OpenChat.Instance, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                    });
                }
                return;
            }
        }

        int spamDelay = config.antiSpamChatDelay;
        if (spamDelay > 0)
            cache.chatMessageDelay = LocalDateTime.now().plusSeconds(spamDelay);
    }
}
