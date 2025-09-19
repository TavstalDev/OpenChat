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

public class BlockEventListener implements Listener {

    public BlockEventListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        OpenChatConfiguration config = OpenChat.OCConfig();

        // Anti-swear
        if (!config.antiSwearEnabled || player.hasPermission(config.antiSwearExemptPermission)) {
            return;
        }

        for (Component line : event.lines()) {
            if (OpenChat.AntiSwearSystem().containsSwearWord(PlainTextComponentSerializer.plainText().serialize(line))) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(player, "AntiSwear.Sign");
                return;
            }
        }
    }

    @EventHandler
    public void anvilRenameEvent(PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) {
            return;
        }

        OpenChatConfiguration config = OpenChat.OCConfig();
        // Anti-swear
        if (!config.antiSwearEnabled || player.hasPermission(config.antiSwearExemptPermission)) {
            return;
        }


        AnvilInventory anvil = event.getInventory();
        if (anvil.getResult() == null)
            return;

        ItemStack result = anvil.getResult();
        if (!result.hasItemMeta() || !result.getItemMeta().hasDisplayName())
            return;

        if (OpenChat.AntiSwearSystem().containsSwearWord(PlainTextComponentSerializer.plainText().serialize(result.displayName()))) {
            anvil.close();
            OpenChat.Instance.sendLocalizedMsg(player, "AntiSwear.AnvilRename");
        }
    }
}
