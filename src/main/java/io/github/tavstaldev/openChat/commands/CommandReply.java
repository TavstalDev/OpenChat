package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.models.PlayerCache;
import io.github.tavstaldev.openChat.util.PlayerUtil;
import io.github.tavstaldev.openChat.util.VanishUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Handles the `/reply` command, allowing players to reply to the last player who messaged them.
 * Implements the `CommandExecutor` interface.
 */
public class CommandReply implements CommandExecutor {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandReply.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "reply";

    /**
     * Constructor for the CommandReply class.
     * Initializes the command executor for the `/reply` command.
     */
    public CommandReply() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    /**
     * Handles the execution of the `/reply` command.
     *
     * @param sender  The sender of the command (player or console).
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments provided with the command.
     * @return True if the command was successfully executed, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // Prevent console from executing this command
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }

        Player player = (Player) sender;

        // Check if the sender has the required permission
        if (!sender.hasPermission("openchat.commands.reply")) {
            OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
            return true;
        }

        // Ensure the command has at least one argument
        if (args.length < 1) {
            OpenChat.Instance.sendCommandReply(sender, "Commands.Reply.Usage");
            return true;
        }

        // Retrieve the last player the sender replied to
        var sourceId = player.getUniqueId();
        PlayerCache cache = PlayerCacheManager.get(sourceId);
        if (cache.getLastRepliedTo() == null) {
            OpenChat.Instance.sendCommandReply(sender, "Commands.Reply.NoOne");
            return true;
        }

        // Get the target player
        Player target = OpenChat.Instance.getServer().getPlayer(cache.getLastRepliedTo());
        if (target == null || !target.isOnline() || (VanishUtil.isVanished(target) && !sender.hasPermission(OpenChat.config().privateMessagingVanishBypassPermission))) {
            OpenChat.Instance.sendCommandReply(sender, "General.PlayerNotFound", Map.of("player", args[0]));
            return true;
        }

        // Retrieve the target player's data
        var targetId = target.getUniqueId();
        var targetData = OpenChat.database().getPlayerData(targetId);
        if (targetData.isEmpty()) {
            OpenChat.Instance.sendCommandReply(sender, "General.Error");
            return true;
        }

        // Check if the target has whispers enabled
        if (!targetData.get().isWhisperEnabled()) {
            OpenChat.Instance.sendCommandReply(sender, "Whisper.Disabled", Map.of("player", PlayerUtil.getPlayerPlainDisplayName(target)));
            return true;
        }

        // Prevent the sender from replying to themselves
        if (sourceId.equals(targetId)) {
            OpenChat.Instance.sendCommandReply(sender, "Whisper.Self");
            return true;
        }

        // Check if the target has ignored the sender
        if (OpenChat.database().isPlayerIgnored(targetId, sourceId)) {
            OpenChat.Instance.sendCommandReply(sender, "Whisper.Disabled", Map.of("player", PlayerUtil.getPlayerPlainDisplayName(target)));
            return true;
        }

        // Construct the reply message
        String message = String.join(" ", args).trim();
        String targetName = PlayerUtil.getPlayerPlainDisplayName(target);
        String sourceName = PlayerUtil.getPlayerPlainDisplayName(player);

        // Send the reply message to the target and notify the sender
        OpenChat.Instance.sendCommandReply(sender, "Whisper.Sender", Map.of("receiver", targetName, "message", message));
        OpenChat.Instance.sendLocalizedMsg(target, "Whisper.Receiver", Map.of("sender", sourceName, "message", message));

        // Notify social spies if enabled
        if (OpenChat.config().privateMessagingSocialSpyEnabled) {
            OpenChat.Instance.getServer().getOnlinePlayers().stream()
                    // Filter out the sender and the receiver
                    .filter(p -> {
                        UUID pId = p.getUniqueId();
                        return !pId.equals(targetId) && !pId.equals(sourceId);
                    })
                    // Filter for players who have the spy permission
                    .filter(p -> OpenChat.database().isSocialSpyEnabled(p))
                    // Notify the remaining players
                    .forEach(p -> {
                        OpenChat.Instance.sendLocalizedMsg(p, "Whisper.Spy", Map.of(
                                "sender", sourceName,
                                "receiver", targetName,
                                "message", message
                        ));
                    });
        }
        return true;
    }
}