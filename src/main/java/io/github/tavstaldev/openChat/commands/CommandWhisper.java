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

public class CommandWhisper implements CommandExecutor {
    @SuppressWarnings("FieldCanBeLocal")
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandWhisper.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "whisper";

    public CommandWhisper() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("openchat.commands.whisper")) {
            OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
            return true;
        }

        if (args.length < 2) {
            OpenChat.Instance.sendCommandReply(sender, "Commands.Whisper.Usage");
            return true;
        }

        Player target = OpenChat.Instance.getServer().getPlayerExact(args[0]);
        if (target == null || !target.isOnline() || VanishUtil.isVanished(target)) {
            OpenChat.Instance.sendCommandReply(sender, "General.PlayerNotFound", Map.of("player", args[0]));
            return true;
        }

        var targetId = target.getUniqueId();
        var targetData = OpenChat.database().getPlayerData(targetId);
        if (targetData.isEmpty()) {
            OpenChat.Instance.sendCommandReply(sender, "General.Error");
            return true;
        }

        if (!targetData.get().isWhisperEnabled()) {
            OpenChat.Instance.sendCommandReply(sender, "Whisper.Disabled", Map.of("player", PlayerUtil.getPlayerPlainDisplayName(target)));
            return true;
        }

        String senderName;
        UUID sourceId;
        if (sender instanceof Player senderPlayer) {
            var senderId = senderPlayer.getUniqueId();
            if (senderId.equals(targetId)) {
                OpenChat.Instance.sendCommandReply(sender, "Whisper.Self");
                return true;
            }

            if (OpenChat.database().isPlayerIgnored(targetId, senderId)) {
                OpenChat.Instance.sendCommandReply(sender, "Whisper.Disabled", Map.of("player", PlayerUtil.getPlayerPlainDisplayName(target)));
                return true;
            }
            senderName = PlayerUtil.getPlayerPlainDisplayName(senderPlayer);
            PlayerCacheManager.get(senderId).setLastRepliedTo(targetId);
            PlayerCacheManager.get(targetId).setLastRepliedTo(senderId);
            sourceId = senderId;
        } else {
            sourceId = null;
            senderName = "&4Console";
        }

        String message = String.join(" ", args).substring(args[0].length()).trim();
        OpenChat.Instance.sendCommandReply(sender, "Whisper.Sender", Map.of("receiver", PlayerUtil.getPlayerPlainDisplayName(target), "message", message));
        OpenChat.Instance.sendLocalizedMsg(target, "Whisper.Receiver", Map.of("sender", senderName, "message", message));
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