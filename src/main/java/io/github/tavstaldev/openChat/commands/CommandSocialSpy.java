package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandSocialSpy implements CommandExecutor {
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
}
