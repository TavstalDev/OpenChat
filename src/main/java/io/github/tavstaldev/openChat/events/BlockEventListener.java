package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.OpenChatConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Listener for handling block-related events in the OpenChat plugin.
 * Implements anti-swear mechanisms for signs and anvil renaming.
 */
public class BlockEventListener implements Listener {

    /**
     * Constructor for BlockEventListener.
     * Registers the event listener with the Bukkit plugin manager.
     *
     * @param plugin The plugin instance to register the listener for.
     */
    public BlockEventListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles the SignChangeEvent to prevent players from writing swear words on signs.
     *
     * @param event The event triggered when a player changes the text on a sign.
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        OpenChatConfiguration config = OpenChat.OCConfig();

        // Check if anti-swear is enabled and the player is not exempt.
        if (!config.antiSwearEnabled || player.hasPermission(config.antiSwearExemptPermission)) {
            return;
        }

        // Iterate through each line of the sign and check for swear words.
        for (Component line : event.lines()) {
            if (OpenChat.AntiSwearSystem().containsSwearWord(PlainTextComponentSerializer.plainText().serialize(line))) {
                event.setCancelled(true); // Cancel the event if a swear word is detected.
                OpenChat.Instance.sendLocalizedMsg(player, "AntiSwear.Sign"); // Notify the player.
                return;
            }
        }
    }

    /**
     * Handles the PrepareAnvilEvent to prevent players from renaming items with swear words.
     *
     * @param event The event triggered when a player prepares to rename an item in an anvil.
     */
    @EventHandler
    public void anvilRenameEvent(PrepareAnvilEvent event) {
        // Ensure the event is triggered by a player.
        if (!(event.getView().getPlayer() instanceof Player player)) {
            return;
        }

        OpenChatConfiguration config = OpenChat.OCConfig();

        // Check if anti-swear is enabled and the player is not exempt.
        if (!config.antiSwearEnabled || player.hasPermission(config.antiSwearExemptPermission)) {
            return;
        }

        AnvilInventory anvil = event.getInventory();

        // Ensure the anvil has a result item.
        if (anvil.getResult() == null) {
            return;
        }

        ItemStack result = anvil.getResult();

        // Ensure the result item has a display name.
        if (!result.hasItemMeta() || !result.getItemMeta().hasDisplayName()) {
            return;
        }

        // Check if the display name contains swear words.
        if (OpenChat.AntiSwearSystem().containsSwearWord(PlainTextComponentSerializer.plainText().serialize(result.displayName()))) {
            anvil.close(); // Close the anvil if a swear word is detected.
            OpenChat.Instance.sendLocalizedMsg(player, "AntiSwear.AnvilRename"); // Notify the player.
        }
    }
}
