package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandWhisperToggle implements CommandExecutor, TabCompleter {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandWhisperToggle.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "whispertoggle";

    public CommandWhisperToggle() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("openchat.commands.whisper")) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        var rawData = OpenChat.database().getPlayerData(player.getUniqueId());
        if (rawData.isEmpty()) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
            return true;
        }
        var data = rawData.get();
        data.setWhisperEnabled(!data.isWhisperEnabled());
        OpenChat.database().updatePlayerData(data);
        if (data.isWhisperEnabled()) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.WhisperToggle.Enabled");
        } else {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.WhisperToggle.Disabled");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
