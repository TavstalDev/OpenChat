package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command executor for the OpenChat plugin.
 * Handles various subcommands such as help, version, reload, and clear.
 */
public class CommandChat implements CommandExecutor, TabCompleter {

    // TODO: Move Reload to the admin command set
    // TODO: Add tab completion
    // Clear can stay here since staff may need it
    // Also add subcommands for other standalone commands, so players can see them in one place

    private final PluginLogger _logger = OpenChat.logger().withModule(CommandChat.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "openchat";
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP
            add(new SubCommandData("help", "openchat.commands.help", Map.of(
                    "syntax", "Commands.Help.Syntax",
                    "description", "Commands.Help.Desc"
            )));
            // VERSION
            add(new SubCommandData("version", "openchat.commands.version", Map.of(
                    "syntax", "",
                    "description", "Commands.Version.Desc"
            )));
            // CLEAR
            add(new SubCommandData("clear", "openchat.commands.clear", Map.of(
                    "syntax", "",
                    "description", "Commands.Clear.Desc"
            )));
        }
    };

    public CommandChat() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    /**
     * Handles the execution of commands.
     *
     * @param sender  The sender of the command (player or console).
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments provided with the command.
     * @return True if the command was successfully executed, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            // Default to the help command if no arguments are provided
            if (!sender.hasPermission("openchat.commands.help")) {
                OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                return true;
            }
            help(sender, 1);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
            case "?": {
                // Check if the player has permission to use the help command
                if (!sender.hasPermission("openchat.commands.help")) {
                    OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                    return true;
                }

                // Parse the page number for the help command
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (Exception ex) {
                        OpenChat.Instance.sendCommandReply(sender, "Commands.InvalidPage");
                        return true;
                    }
                }

                help(sender, page);
                return true;
            }
            case "version": {
                // Check if the player has permission to use the version command
                if (!sender.hasPermission("openchat.commands.version")) {
                    OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                    return true;
                }

                // Send the current plugin version to the player
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("version", OpenChat.Instance.getVersion());
                OpenChat.Instance.sendCommandReply(sender, "Commands.Version.Current", parameters);

                // Check if the plugin is up-to-date
                OpenChat.Instance.isUpToDate().thenAccept(upToDate -> {
                    if (upToDate) {
                        OpenChat.Instance.sendCommandReply(sender, "Commands.Version.UpToDate");
                    } else {
                        OpenChat.Instance.sendCommandReply(sender, "Commands.Version.Outdated", Map.of("link", OpenChat.Instance.getDownloadUrl()));
                    }
                }).exceptionally(e -> {
                    _logger.error("Failed to determine update status: " + e.getMessage());
                    return null;
                });
                return true;
            }
            case "clear": {
                // Check if the player has permission to use the clear command
                if (!sender.hasPermission("openchat.commands.clear")) {
                    OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                    return true;
                }

                // Clear the chat for all online players except those with bypass permission
                for (var onlinePlayer : OpenChat.Instance.getServer().getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("openchat.bypass.clear")) continue;
                    for (int i = 0; i < 300; i++) {
                        onlinePlayer.sendMessage(Component.text("\n\n\n"));
                    }
                    OpenChat.Instance.sendLocalizedMsg(onlinePlayer, "Commands.Clear.Done", Map.of("player", sender.getName()));
                }

                OpenChat.Instance.sendCommandReply(sender, "Commands.Clear.Done", Map.of("player", sender.getName()));
                return true;
            }
        }

        // Send an error message if the subcommand is invalid
        OpenChat.Instance.sendCommandReply(sender, "Commands.InvalidArguments");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        switch (args.length) {
            case 0:
            case 1: {
                return List.of("help", "version", "clear");
            }
            case 2: {
                String subCommand = args[0].toLowerCase();
                switch (subCommand) {
                    case "help":
                    case "?": {
                        return List.of("1", "5", "10");
                    }
                    default:
                        return List.of();
                }
            }
            default:
                return List.of();
        }
    }

    /**
     * Displays the help menu to the sender.
     *
     * @param sender The sender requesting the help menu.
     * @param page   The page number of the help menu to display.
     */
    private void help(CommandSender sender, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        // Ensure the page number is within valid bounds
        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        // Send the help menu title and info
        OpenChat.Instance.sendCommandReply(sender, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        OpenChat.Instance.sendCommandReply(sender, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;

        // Display up to 15 subcommands per page
        for (int i = 0; i < 15; i++) {
            int index = itemIndex + (page - 1) * 15;
            if (index >= _subCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommandData subCommand = _subCommands.get(index);
            if (!subCommand.hasPermission(sender)) {
                i--;
                continue;
            }

            subCommand.send(OpenChat.Instance, sender, baseCommand);
        }

        // Display navigation buttons for the help menu
        String previousBtn, nextBtn, bottomMsg;
        if (sender instanceof Player player)
        {
            previousBtn = OpenChat.Instance.localize(player, "Commands.Help.PrevBtn");
            nextBtn = OpenChat.Instance.localize(player, "Commands.Help.NextBtn");
            bottomMsg = OpenChat.Instance.localize(player, "Commands.Help.Bottom", Map.of(
                            "current_page", page,
                            "max_page", maxPage
                    ));
        }
        else {
            previousBtn = OpenChat.Instance.localize("Commands.Help.PrevBtn");
            nextBtn = OpenChat.Instance.localize("Commands.Help.NextBtn");
            bottomMsg = OpenChat.Instance.localize("Commands.Help.Bottom", Map.of(
                    "current_page", page,
                    "max_page", maxPage
            ));
        }

        Map<String, Component> bottomParams = new HashMap<>();
        if (page > 1)
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page - 1))));
        else
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true));

        if (!reachedEnd && maxPage >= page + 1)
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page + 1))));
        else
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        sender.sendMessage(bottomComp);
    }
}
