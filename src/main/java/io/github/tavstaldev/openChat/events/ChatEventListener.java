package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
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

        // Anti-spam
        if (OpenChat.Config().getBoolean("antiSpam.enabled", true) && !source.hasPermission(OpenChat.Config().getString("antiSpam.exemptPermission", "openchat.bypass.antispam"))) {
            // Feature: Chat cooldown
            if (cache.commandDelay.isAfter(LocalDateTime.now())) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiSpam.ChatCooldown", Map.of("time", String.valueOf(cache.commandDelay.getSecond() - LocalDateTime.now().getSecond())));
                for (String cmd : OpenChat.Config().getStringList("antiSpam.executeCommand")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                }
                return;
            }

            // Feature: Repeated messages
            if (cache.getChatSpamCount() >= OpenChat.Config().getInt("antiSpam.maxDuplicates", 2)) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiSpam.RepeatedMessages");
                for (String cmd : OpenChat.Config().getStringList("antiSpam.executeCommand")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                }
                return;
            }
        }

        // Anti-advertisement
        if (OpenChat.Config().getBoolean("antiAdvertisement.enabled", true) && !source.hasPermission(OpenChat.Config().getString("antiAdvertisement.exemptPermission", "openchat.bypass.antiadvertisement"))) {
            if (OpenChat.AdvertisementSystem().containsAdvertisement(rawMessage)) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiAd.AdvertisementDetected");
                for (String cmd : OpenChat.Config().getStringList("antiAdvertisement.executeCommand")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                }
                return;
            }
        }


        // Anti-capitalization
        if (OpenChat.Config().getBoolean("antiCaps.enabled", true) && !source.hasPermission(OpenChat.Config().getString("antiCaps.exemptPermission", "openchat.bypass.anticaps")) ) {
            int minLength = OpenChat.Config().getInt("antiCaps.minLength", 10);
            double maxCapsPercentage = OpenChat.Config().getInt("antiCaps.percentage", 70) / 100.0;
            if (rawMessage.length() >= minLength) {
                long capsCount = rawMessage.chars().filter(Character::isUpperCase).count();
                double capsPercentage = (double) capsCount / rawMessage.length();
                if (capsPercentage > maxCapsPercentage) {
                    event.setCancelled(true);
                    OpenChat.Instance.sendLocalizedMsg(source, "AntiCaps.TooManyCaps");
                    for (String cmd : OpenChat.Config().getStringList("antiCaps.executeCommand")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()));
                    }
                    return;
                }
            }
        }

        // Anti-swear
        if (OpenChat.Config().getBoolean("antiSwear.enabled", true) && !source.hasPermission(OpenChat.Config().getString("antiSwear.exemptPermission", "openchat.bypass.antiswear")) ) {
            // TODO
        }

        event.setMessage(rawMessage.replace("&", "§"));
        cache.setLastChatMessage(rawMessage);
        int spamDelay = OpenChat.Config().getInt("antiSpam.chatDelay", 1);
        if (spamDelay > 0)
        {
            cache.chatMessageDelay = LocalDateTime.now().plusSeconds(spamDelay);
        }
    }
}
