package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.config.ModerationConfig;
import io.github.tavstaldev.openChat.models.systems.AntiAdvertisementSystem;
import io.github.tavstaldev.openChat.models.systems.AntiSwearSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

/**
 * Listener for handling item-related events in the OpenChat plugin.
 * Implements anti-swear mechanisms for book titles and content.
 */
public class ItemEventListener implements Listener {

    /**
     * Constructor for ItemEventListener.
     * Registers the event listener with the Bukkit plugin manager.
     *
     * @param plugin The plugin instance to register the listener for.
     */
    public ItemEventListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles the PlayerEditBookEvent to prevent players from using swear words
     * in book titles or content.
     *
     * @param event The event triggered when a player edits a book.
     */
    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        BookMeta bookMeta = event.getNewBookMeta(); // The metadata of the edited book.
        Player player = event.getPlayer(); // The player editing the book.
        ModerationConfig config = OpenChat.moderationConfig();

        boolean checkForSwearWords = config.antiSwearEnabled && !player.hasPermission(config.antiSwearExemptPermission);
        boolean checkForAdvertisement = config.antiAdvertisementEnabled && !player.hasPermission(config.antiAdvertisementExemptPermission);
        final AntiAdvertisementSystem advertisementSystem = OpenChat.advertisementSystem();
        final AntiSwearSystem swearSystem = OpenChat.antiSwearSystem();

        // Check the book title for swear words.
        if (bookMeta.hasTitle()) {
            //noinspection DataFlowIssue
            String title = PlainTextComponentSerializer.plainText().serialize(bookMeta.title());
            if (checkForSwearWords && swearSystem.containsSwearWord(title)) {
                event.setCancelled(true); // Cancel the event if a swear word is detected in the title.
                OpenChat.Instance.sendLocalizedMsg(player, "AntiSwear.BookTitle"); // Notify the player.
                return;
            }
            if (checkForAdvertisement && advertisementSystem.containsAdvertisement(title)) {
                event.setCancelled(true); // Cancel the event if an advertisement is detected in the title.
                OpenChat.Instance.sendLocalizedMsg(player, "AntiAd.BookTitle"); // Notify the player.
                return;
            }
        }

        // If the book has no pages, there's nothing more to check.
        if (!bookMeta.hasPages())
            return;

        // Check the book pages for swear words.
        for (Component page : bookMeta.pages()) {
            String pageText = PlainTextComponentSerializer.plainText().serialize(page);
            if (checkForSwearWords && swearSystem.containsSwearWord(pageText)) {
                event.setCancelled(true); // Cancel the event if a swear word is detected in the content.
                OpenChat.Instance.sendLocalizedMsg(player, "AntiSwear.BookContent"); // Notify the player.
                return;
            }
            if (checkForAdvertisement && advertisementSystem.containsAdvertisement(pageText)) {
                event.setCancelled(true); // Cancel the event if an advertisement is detected in the content.
                OpenChat.Instance.sendLocalizedMsg(player, "AntiAd.BookContent"); // Notify the player.
                return;
            }
        }
    }
}