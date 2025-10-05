package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Listener class for handling operator-related events in the OpenChat plugin.
 * Ensures operator protection by restricting unauthorized operator actions.
 */
public class OpEventListener implements Listener {
    private final PluginLogger _logger = OpenChat.logger().withModule(OpEventListener.class);
    private final Set<OfflinePlayer> allowedOperators = new HashSet<>();

    /**
     * Constructor for the OpEventListener.
     * Registers the event listener with the plugin manager.
     *
     * @param plugin The plugin instance to register the listener for.
     */
    public OpEventListener(Plugin plugin) {
        _logger.debug("Registering op event listener...");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        _logger.debug("Event listener registered.");
    }

    /**
     * Handles the AsyncPlayerPreLoginEvent to check if a player is allowed to be an operator.
     * If the player is not allowed, they are de-opped and kicked from the server.
     *
     * @param event The event triggered when a player attempts to log in.
     */
    @EventHandler
    public void onLoginOpCheck(AsyncPlayerPreLoginEvent event) {
        var config = OpenChat.config();
        if (!config.opProtectionEnabled)
            return;

        UUID uuid = event.getUniqueId();
        OfflinePlayer player = Bukkit.getOfflinePlayer((UUID)uuid);

        if (isAllowedOperator(player))
            return;

        if (player.isOp() && this.allowedOperators.stream().noneMatch(allowedPlayer -> allowedPlayer.getUniqueId().equals(player.getUniqueId()))) {
            player.setOp(false);
            String kickMessage = OpenChat.translator().localize("OpProtection.Kick");
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatUtils.translateColors(kickMessage, true));
        }
    }

    /**
     * Handles the ServerCommandEvent to prevent unauthorized use of the "op" command.
     * Cancels the command if the target player is not in the allowed operators list.
     *
     * @param event The event triggered when a server command is executed.
     */
    @EventHandler
    public void onOPServer(ServerCommandEvent event) {
        if (event.isCancelled())
            return;

        var config = OpenChat.config();
        if (!config.opProtectionEnabled)
            return;

        if ((event.getCommand().toLowerCase().startsWith("op ") || event.getCommand().toLowerCase().startsWith("minecraft:op "))) {
            var args = event.getCommand().split(" ");
            if (args.length < 2) {
                return;
            }

            var player = Bukkit.getOfflinePlayer(args[1]);
            if (isAllowedOperator(player))
                return;

            event.setCancelled(true);
            OpenChat.logger().error("The targeted player is not in the allowed operators list. Command cancelled.");
        }
    }

    /**
     * Handles the PlayerCommandPreprocessEvent to prevent unauthorized players from using the "op" command.
     * Cancels the command if the target player is not in the allowed operators list.
     *
     * @param event The event triggered when a player executes a command.
     */
    @EventHandler
    public void onOpPlayer(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled())
            return;

        var config = OpenChat.config();
        if (!config.opProtectionEnabled)
            return;

        var player = event.getPlayer();
        if ((player.isOp() || player.hasPermission("*")) && (event.getMessage().toLowerCase().startsWith("/op ") || event.getMessage().toLowerCase().startsWith("/minecraft:op "))) {
            var message = event.getMessage();
            var splitMessage = message.split(" ");
            if (splitMessage.length < 2) {
                return;
            }

            var targetPlayer = Bukkit.getOfflinePlayer(splitMessage[1]);
            //noinspection ConstantValue
            if (targetPlayer == null)
                return;

            if (isAllowedOperator(targetPlayer))
                return;

            event.setCancelled(true);
            OpenChat.Instance.sendLocalizedMsg(player, "OpProtection.Unallowed");
        }
    }

    /**
     * Updates the list of allowed operators based on the plugin configuration.
     * De-ops any operators not in the allowed list.
     */
    public void updateAllowedOperators() {
        var config = OpenChat.config();
        this.allowedOperators.clear();
        for (String username : config.opProtectionOperators) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(username);
            this.allowedOperators.add(player);
        }

        for (OfflinePlayer player : Bukkit.getOperators()) {
            if (!isAllowedOperator(player)) {
                player.setOp(false);
                _logger.info("Deopped player " + player.getName() + " as they are not in the allowed operators list.");
            }
        }
    }

    /**
     * Checks if a given player is in the allowed operators list.
     *
     * @param player The player to check.
     * @return True if the player is allowed, false otherwise.
     */
    private boolean isAllowedOperator(OfflinePlayer player) {
        return this.allowedOperators.contains(player);
    }
}
