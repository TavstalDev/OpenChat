package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.OpenChatConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

public class ItemEventListener implements Listener {
    public ItemEventListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        BookMeta bookMeta = event.getNewBookMeta();
        Player player = event.getPlayer();
        OpenChatConfiguration config = OpenChat.OCConfig();

        // TODO: Add anti-advertisement check

        // Anti-swear
        if (!config.antiSwearEnabled || player.hasPermission(config.antiSwearExemptPermission)) {
            return;
        }

        if (bookMeta.hasTitle()) {
            if (OpenChat.AntiSwearSystem().containsSwearWord(PlainTextComponentSerializer.plainText().serialize(bookMeta.title()))) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(player, "AntiSwear.BookTitle");
                return;
            }
        }

        if (bookMeta.hasPages()) {
            for (Component page : bookMeta.pages()) {
                if (OpenChat.AntiSwearSystem().containsSwearWord(PlainTextComponentSerializer.plainText().serialize(page))) {
                    event.setCancelled(true);
                    OpenChat.Instance.sendLocalizedMsg(player, "AntiSwear.BookContent");
                    return;
                }
            }
        }
    }
}
