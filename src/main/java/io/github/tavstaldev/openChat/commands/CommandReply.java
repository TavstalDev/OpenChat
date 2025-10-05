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

public class CommandReply implements CommandExecutor {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandReply.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "reply";

    public CommandReply() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;
        if (!sender.hasPermission("openchat.commands.reply")) {
            OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
            return true;
        }

        if (args.length < 1) {
            OpenChat.Instance.sendCommandReply(sender, "Commands.Reply.Usage");
            return true;
        }

        PlayerCache cache = PlayerCacheManager.get(player.getUniqueId());
        if (cache.getLastRepliedTo() == null) {
            OpenChat.Instance.sendCommandReply(sender, "Commands.Reply.NoOne");
            return true;
        }

        Player target = OpenChat.Instance.getServer().getPlayer(cache.getLastRepliedTo());
        if (target == null || !target.isOnline() || VanishUtil.isVanished(target)) {
            OpenChat.Instance.sendCommandReply(sender, "General.PlayerNotFound", Map.of("player", args[0]));
            return true;
        }

        var targetData = OpenChat.database().getPlayerData(target.getUniqueId());
        if (targetData.isEmpty()) {
            OpenChat.Instance.sendCommandReply(sender, "General.Error");
            return true;
        }

        if (!targetData.get().isWhisperEnabled()) {
            OpenChat.Instance.sendCommandReply(sender, "Whisper.Disabled", Map.of("player", PlayerUtil.getPlayerPlainDisplayName(target)));
            return true;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            OpenChat.Instance.sendCommandReply(sender, "Whisper.Self");
            return true;
        }

        if (OpenChat.database().isPlayerIgnored(target.getUniqueId(), player.getUniqueId())) {
            OpenChat.Instance.sendCommandReply(sender, "Whisper.Disabled", Map.of("player", PlayerUtil.getPlayerPlainDisplayName(target)));
            return true;
        }

        String message = String.join(" ", args).trim();
        OpenChat.Instance.sendCommandReply(sender, "Whisper.Sender", Map.of("receiver", PlayerUtil.getPlayerPlainDisplayName(target), "message", message));
        OpenChat.Instance.sendLocalizedMsg(target, "Whisper.Receiver", Map.of("sender", PlayerUtil.getPlayerPlainDisplayName(player), "message", message));
        return true;
    }
}