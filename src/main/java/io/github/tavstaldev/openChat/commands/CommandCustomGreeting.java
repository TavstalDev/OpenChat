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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandCustomGreeting implements CommandExecutor {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandCustomGreeting.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "customgreeting";
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP
            add(new SubCommandData("help", "openchat.commands.customgreeting.help", Map.of(
                    "syntax", "",
                    "description", "Commands.Help.Desc"
            )));
            // SET
            add(new SubCommandData("version", "openchat.commands.customgreeting.set", Map.of(
                    "syntax", "Commands.CustomGreeting.Set.Syntax",
                    "description", "Commands.CustomGreeting.Set.Desc"
            )));
            // GET
            add(new SubCommandData("reload", "openchat.commands.customgreeting.get", Map.of(
                    "syntax", "Commands.CustomGreeting.Get.Syntax",
                    "description", "Commands.CustomGreeting.Get.Desc"
            )));
            // CLEAR
            add(new SubCommandData("clear", "openchat.commands.customgreeting.clear", Map.of(
                    "syntax", "",
                    "description", "Commands.CustomGreeting.Clear.Desc"
            )));
        }
    };

    public CommandCustomGreeting() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // Handle commands sent from the console
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }

        Player player = (Player) sender;

        // Handle subcommands based on the first argument
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    // Check if the player has permission to use the help command
                    if (!player.hasPermission("openchat.commands.customgreeting.help")) {
                        OpenChat.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    // Parse the page number for the help command
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            OpenChat.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    help(player, page);
                    return true;
                }
                case "set": {
                    // Check if the player has permission to use the set command
                    if (!player.hasPermission("openchat.commands.customgreeting.set")) {
                        OpenChat.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length < 3) {
                        OpenChat.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
                        return true;
                    }

                    String type = args[1].toLowerCase();
                    switch (type) {
                        case "join":
                        case "connect": {
                            var playerDataOpt = OpenChat.database().getPlayerData(player.getUniqueId());
                            if (playerDataOpt.isEmpty()) {
                                OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
                                return true;
                            }

                            String message = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
                            if (!message.contains("{player}")) {
                                OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Set.NoPlayerPlaceholder");
                                return true;
                            }

                            var playerData = playerDataOpt.get();
                            playerData.setCustomJoinMessage(message);
                            OpenChat.database().updatePlayerData(playerData);
                            OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Set.Success");
                            break;
                        }
                        case "leave":
                        case "left":
                        case "disconnect":
                        case "quit": {
                            var playerDataOpt = OpenChat.database().getPlayerData(player.getUniqueId());
                            if (playerDataOpt.isEmpty()) {
                                OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
                                return true;
                            }

                            String message = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
                            if (!message.contains("{player}")) {
                                OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Set.NoPlayerPlaceholder");
                                return true;
                            }

                            var playerData = playerDataOpt.get();
                            playerData.setCustomLeaveMessage(message);
                            OpenChat.database().updatePlayerData(playerData);
                            OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Set.Success");
                            break;
                        }
                        default: {
                            OpenChat.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
                            return true;
                        }
                    }

                    return true;
                }
                case "get": {
                    // Check if the player has permission to use the get command
                    if (!player.hasPermission("openchat.commands.customgreeting.get")) {
                        OpenChat.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        OpenChat.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
                        return true;
                    }

                    String type = args[1].toLowerCase();
                    switch (type) {
                        case "join":
                        case "connect": {
                            var playerDataOpt = OpenChat.database().getPlayerData(player.getUniqueId());
                            if (playerDataOpt.isEmpty()) {
                                OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
                                return true;
                            }

                            if (playerDataOpt.get().getCustomJoinMessage() == null) {
                                OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Get.None");
                            }
                            else {
                                OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Get.Current", Map.of(
                                        "message", playerDataOpt.get().getCustomJoinMessage()
                                ));
                            }
                            break;
                        }
                        case "leave":
                        case "left":
                        case "disconnect":
                        case "quit": {
                            var playerDataOpt = OpenChat.database().getPlayerData(player.getUniqueId());
                            if (playerDataOpt.isEmpty()) {
                                OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
                                return true;
                            }

                            if (playerDataOpt.get().getCustomLeaveMessage() == null) {
                                OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Get.None");
                            }
                            else {
                                OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Get.Current", Map.of(
                                        "message", playerDataOpt.get().getCustomLeaveMessage()
                                ));
                            }
                            break;
                        }
                        default: {
                            OpenChat.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
                            return true;
                        }
                    }

                    return true;
                }
                case "clear": {
                    // Check if the player has permission to use the clear command
                    if (!player.hasPermission("openchat.commands.customgreeting.clear")) {
                        OpenChat.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        OpenChat.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
                        return true;
                    }

                    String type = args[1].toLowerCase();
                    switch (type) {
                        case "join":
                        case "connect": {
                            var playerDataOpt = OpenChat.database().getPlayerData(player.getUniqueId());
                            if (playerDataOpt.isEmpty()) {
                                OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
                                return true;
                            }

                            var playerData = playerDataOpt.get();
                            playerData.setCustomJoinMessage(null);
                            OpenChat.database().updatePlayerData(playerData);
                            OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Clear.Success");
                            break;
                        }
                        case "leave":
                        case "left":
                        case "disconnect":
                        case "quit": {
                            var playerDataOpt = OpenChat.database().getPlayerData(player.getUniqueId());
                            if (playerDataOpt.isEmpty()) {
                                OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
                                return true;
                            }

                            var playerData = playerDataOpt.get();
                            playerData.setCustomLeaveMessage(null);
                            OpenChat.database().updatePlayerData(playerData);
                            OpenChat.Instance.sendLocalizedMsg(player, "Commands.CustomGreeting.Clear.Success");
                            break;
                        }
                        default: {
                            OpenChat.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
                            return true;
                        }
                    }
                    return true;
                }
            }

            // Send an error message if the subcommand is invalid
            OpenChat.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
            return true;
        }

        // Default to the help command if no arguments are provided
        if (!player.hasPermission("openchat.commands.customgreeting")) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }
        help(player, 1);
        return true;
    }

    private void help(Player player, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        // Ensure the page number is within valid bounds
        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        // Send the help menu title and info
        OpenChat.Instance.sendLocalizedMsg(player, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        OpenChat.Instance.sendLocalizedMsg(player, "Commands.Help.Info");

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
            if (!subCommand.hasPermission(player)) {
                i--;
                continue;
            }

            subCommand.send(OpenChat.Instance, player, baseCommand);
        }

        // Display navigation buttons for the help menu
        String previousBtn = OpenChat.Instance.localize(player, "Commands.Help.PrevBtn");
        String nextBtn = OpenChat.Instance.localize(player, "Commands.Help.NextBtn");
        String bottomMsg = OpenChat.Instance.localize(player, "Commands.Help.Bottom")
                .replace("%current_page%", String.valueOf(page))
                .replace("%max_page%", String.valueOf(maxPage));

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
        player.sendMessage(bottomComp);
    }
}
