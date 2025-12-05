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
 * Handles the `/ignore` command, allowing players to ignore messages from other players.
 * Implements both `CommandExecutor` and `TabCompleter` interfaces.
 */
public class CommandIgnore implements CommandExecutor, TabCompleter {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandIgnore.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "ignore";

    /**
     * Constructor for the CommandIgnore class.
     * Initializes the command executor and tab completer for the `/ignore` command.
     */
    public CommandIgnore() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    /**
     * Handles the execution of the `/ignore` command.
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
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.Usage");
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

        // Prevent players from ignoring themselves
        if (playerId == targetId) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.Self");
            return true;
        }

        // Check if the target player is already ignored
        if (OpenChat.database().isPlayerIgnored(playerId, targetId)) {
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.AlreadyEnabled", Map.of("player", args[0]));
            return true;
        }

        // Add the target player to the ignore list
        OpenChat.database().addIgnoredPlayer(playerId, targetId);
        OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.Enabled", Map.of("player", args[0]));
        return true;
    }

    /**
     * Provides tab completion suggestions for the `/ignore` command.
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