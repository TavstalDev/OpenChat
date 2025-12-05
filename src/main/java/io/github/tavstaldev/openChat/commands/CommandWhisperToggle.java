package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handles the `/whispertoggle` command, allowing players to enable or disable the whisper feature.
 * Implements both `CommandExecutor` and `TabCompleter` interfaces.
 */
public class CommandWhisperToggle implements CommandExecutor, TabCompleter {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandWhisperToggle.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "whispertoggle";

    /**
     * Constructor for the CommandWhisperToggle class.
     * Initializes the command executor and tab completer for the `/whispertoggle` command.
     */
    public CommandWhisperToggle() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    /**
     * Handles the execution of the `/whispertoggle` command.
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

        // Check if the player has the required permission
        if (!player.hasPermission("openchat.commands.whisper")) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        // Retrieve the player's data from the database
        var rawData = OpenChat.database().getPlayerData(player.getUniqueId());
        if (rawData.isEmpty()) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
            return true;
        }

        // Toggle the whisper feature for the player
        var data = rawData.get();
        data.setWhisperEnabled(!data.isWhisperEnabled());
        OpenChat.database().updatePlayerData(data);

        // Notify the player of the updated whisper status
        if (data.isWhisperEnabled()) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.WhisperToggle.Enabled");
        } else {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.WhisperToggle.Disabled");
        }
        return true;
    }

    /**
     * Provides tab completion suggestions for the `/whispertoggle` command.
     *
     * @param commandSender The sender of the command.
     * @param command       The command being executed.
     * @param label         The alias of the command used.
     * @param args          The arguments provided with the command.
     * @return A list of possible completions for the last argument, or null if no completions are available.
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of(); // No tab completions for this command
    }
}