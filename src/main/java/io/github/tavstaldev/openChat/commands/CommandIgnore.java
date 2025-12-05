package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class CommandIgnore implements CommandExecutor, TabCompleter {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandIgnore.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "ignore";

    public CommandIgnore() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;

        if (args.length != 1) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.Usage");
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
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.Self");
            return true;
        }

        if (OpenChat.database().isPlayerIgnored(playerId, targetId))
        {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.AlreadyEnabled", Map.of("player", args[0]));
            return true;
        }

        OpenChat.database().addIgnoredPlayer(playerId, targetId);
        OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.Enabled", Map.of("player", args[0]));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length > 1) {
            return List.of();
        }
        return null; // Let Bukkit handle player name completions
    }
}
