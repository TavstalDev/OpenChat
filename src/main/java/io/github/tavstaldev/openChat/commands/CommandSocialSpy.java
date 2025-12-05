package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.util.SoundUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandSocialSpy implements CommandExecutor, TabCompleter {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandSocialSpy.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "socialspy";

    public CommandSocialSpy() {
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
        if (!player.hasPermission(OpenChat.config().privateMessagingSocialSpyPermission)) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        var rawData = OpenChat.database().getPlayerData(player.getUniqueId());
        if (rawData.isEmpty()) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
            return true;
        }
        var data = rawData.get();
        data.setSocialSpyEnabled(!data.isSocialSpyEnabled());
        OpenChat.database().updatePlayerData(data);
        if (data.isSocialSpyEnabled()) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.SocialSpy.Enabled");
        } else {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.SocialSpy.Disabled");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of(); // No tab completions for this command
    }
}
