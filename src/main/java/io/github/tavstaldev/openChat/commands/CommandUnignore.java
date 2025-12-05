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

/**
 * Handles the `/unignore` command, allowing players to remove other players from their ignore list.
 * Implements both `CommandExecutor` and `TabCompleter` interfaces.
 */
public class CommandUnignore implements CommandExecutor, TabCompleter {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandUnignore.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "unignore";

    /**
     * Constructor for the CommandUnignore class.
     * Initializes the command executor and tab completer for the `/unignore` command.
     */
    public CommandUnignore() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    /**
     * Handles the execution of the `/unignore` command.
     *
     * @param sender  The sender of the command (player or console).
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments provided with the command.
     * @return True if the command was successfully executed, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Prevent console from executing this command
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;

        // Validate the number of arguments
        if (args.length != 1) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.Usage");
            return true;
        }

        // Retrieve the target player
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || !target.hasPlayedBefore()) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.PlayerNotFound", Map.of("player", args[0]));
            return true;
        }

        var playerId = player.getUniqueId();
        var targetId = target.getUniqueId();

        // Prevent players from unignoring themselves
        if (playerId == targetId) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.Self");
            return true;
        }

        // Check if the target player is already not ignored
        if (!OpenChat.database().isPlayerIgnored(playerId, targetId)) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.AlreadyDisabled", Map.of("player", args[0]));
            return true;
        }

        // Remove the target player from the ignore list
        OpenChat.database().removeIgnoredPlayer(playerId, targetId);
        OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.Disabled", Map.of("player", args[0]));
        return true;
    }

    /**
     * Provides tab completion suggestions for the `/unignore` command.
     *
     * @param commandSender The sender of the command.
     * @param command       The command being executed.
     * @param label         The alias of the command used.
     * @param args          The arguments provided with the command.
     * @return A list of possible completions for the last argument, or null if no completions are available.
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length > 1) {
            return List.of();
        }
        return null; // Let Bukkit handle player name completions
    }
}