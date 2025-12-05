package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.util.PlayerUtil;
import io.github.tavstaldev.openChat.util.VanishUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Handles the `/whisper` command, allowing players to send private messages to other players.
 * Implements the `CommandExecutor` interface.
 */
public class CommandWhisper implements CommandExecutor {
    @SuppressWarnings("FieldCanBeLocal")
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandWhisper.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "whisper";

    /**
     * Constructor for the CommandWhisper class.
     * Initializes the command executor for the `/whisper` command.
     */
    public CommandWhisper() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    /**
     * Handles the execution of the `/whisper` command.
     *
     * @param sender  The sender of the command (player or console).
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments provided with the command.
     * @return True if the command was successfully executed, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // Check if the sender has the required permission
        if (!sender.hasPermission("openchat.commands.whisper")) {
            OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
            return true;
        }

        // Ensure the command has at least two arguments
        if (args.length < 2) {
            OpenChat.Instance.sendCommandReply(sender, "Commands.Whisper.Usage");
            return true;
        }

        // Retrieve the target player
        Player target = OpenChat.Instance.getServer().getPlayerExact(args[0]);
        if (target == null || !target.isOnline() || (VanishUtil.isVanished(target) && !sender.hasPermission(OpenChat.config().privateMessagingVanishBypassPermission))) {
            OpenChat.Instance.sendCommandReply(sender, "General.PlayerNotFound", Map.of("player", args[0]));
            return true;
        }

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

        String senderName;
        UUID sourceId;

        // Handle the sender if they are a player
        if (sender instanceof Player senderPlayer) {
            var senderId = senderPlayer.getUniqueId();

            // Prevent the sender from whispering to themselves
            if (senderId.equals(targetId)) {
                OpenChat.Instance.sendCommandReply(sender, "Whisper.Self");
                return true;
            }

            // Check if the target has ignored the sender
            if (OpenChat.database().isPlayerIgnored(targetId, senderId)) {
                OpenChat.Instance.sendCommandReply(sender, "Whisper.Disabled", Map.of("player", PlayerUtil.getPlayerPlainDisplayName(target)));
                return true;
            }

            senderName = PlayerUtil.getPlayerPlainDisplayName(senderPlayer);
            PlayerCacheManager.get(senderId).setLastRepliedTo(targetId);
            PlayerCacheManager.get(targetId).setLastRepliedTo(senderId);
            sourceId = senderId;
        } else {
            // Handle the sender if they are the console
            sourceId = null;
            senderName = "&4Console";
        }

        // Construct the whisper message
        String message = String.join(" ", args).substring(args[0].length()).trim();

        // Send the whisper message to the target and notify the sender
        OpenChat.Instance.sendCommandReply(sender, "Whisper.Sender", Map.of("receiver", PlayerUtil.getPlayerPlainDisplayName(target), "message", message));
        OpenChat.Instance.sendLocalizedMsg(target, "Whisper.Receiver", Map.of("sender", senderName, "message", message));

        // Notify social spies if enabled
        if (OpenChat.config().privateMessagingSocialSpyEnabled) {
            OpenChat.Instance.getServer().getOnlinePlayers().stream()
                    // 1. Filter out the sender (if Player) and the receiver
                    .filter(p -> {
                        UUID pId = p.getUniqueId();
                        return !pId.equals(targetId) && !pId.equals(sourceId);
                    })
                    // 2. Filter for players who have the spy permission
                    .filter(p -> OpenChat.database().isSocialSpyEnabled(p))
                    // 3. Process the remaining players
                    .forEach(p -> {
                        OpenChat.Instance.sendLocalizedMsg(p, "Whisper.Spy", Map.of(
                                "sender", senderName,
                                "receiver", PlayerUtil.getPlayerPlainDisplayName(target),
                                "message", message
                        ));
                    });
        }
        return true;
    }
}