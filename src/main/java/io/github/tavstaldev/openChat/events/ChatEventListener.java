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

public class ChatEventListener implements Listener {
    private final PluginLogger _logger = OpenChat.Logger().WithModule(ChatEventListener.class);

    public ChatEventListener(Plugin plugin) {
        _logger.Debug("Registering chat event listener...");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        _logger.Debug("Event listener registered.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        Player source = event.getPlayer();
        PlayerCache cache = PlayerCacheManager.get(source.getUniqueId());
        String rawMessage = event.getMessage();
        OpenChatConfiguration config = OpenChat.OCConfig();

        // Anti-spam
        if (config.antiSpamEnabled && !source.hasPermission(config.antiSpamExemptPermission)) {
            // Feature: Chat cooldown
            if (cache.commandDelay.isAfter(LocalDateTime.now())) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiSpam.ChatCooldown", Map.of("time", String.valueOf(cache.commandDelay.getSecond() - LocalDateTime.now().getSecond())));
                for (String cmd : OpenChat.Config().getStringList("antiSpam.executeCommand")) {
                    Bukkit.getScheduler().runTask(OpenChat.Instance, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                    });
                }
                return;
            }

            // Feature: Repeated messages
            if (cache.getChatSpamCount() >= config.antiSpamMaxDuplicates) {
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
        if (config.antiCapsEnabled && !source.hasPermission(config.antiCapsExemptPermission) ) {
            double maxCapsPercentage = config.antiCapsPercentage / 100.0;
            if (rawMessage.length() >= config.antiCapsMinLength) {
                long capsCount = rawMessage.chars().filter(Character::isUpperCase).count();
                double capsPercentage = (double) capsCount / rawMessage.length();
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

        //event.setMessage(rawMessage.replace("&", "§"));
        cache.setLastChatMessage(rawMessage);
        int spamDelay = config.antiSpamChatDelay;
        if (spamDelay > 0)
        {
            cache.chatMessageDelay = LocalDateTime.now().plusSeconds(spamDelay);
        }
    }
}
