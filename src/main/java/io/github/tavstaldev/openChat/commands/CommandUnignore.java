package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CommandUnignore implements CommandExecutor {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandUnignore.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "unignore";


    public CommandUnignore() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;

        if (args.length != 1) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.Usage");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || !target.hasPlayedBefore()) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.PlayerNotFound", Map.of("player", args[0]));
            return true;
        }

        var playerId = player.getUniqueId();
        var targetId = target.getUniqueId();

        if (playerId == targetId)
        {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.Self");
            return true;
        }

        if (!OpenChat.database().isPlayerIgnored(playerId, targetId))
        {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.AlreadyDisabled", Map.of("player", args[0]));
            return true;
        }

        OpenChat.database().removeIgnoredPlayer(playerId, targetId);
        OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.Disabled", Map.of("player", args[0]));
        return true;
    }
}
