package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final PluginLogger _logger = OpenChat.Logger().WithModule(ChatListener.class);

    public ChatListener() {
        _logger.Debug("Registering chat event listener...");
        Bukkit.getPluginManager().registerEvents(this, OpenChat.Instance);
        _logger.Debug("Event listener registered.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player source = event.getPlayer();
        String rawMessage = event.getMessage();


        event.setMessage(rawMessage.replace("&", "§"));
    }
}
