package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.OfflinePlayer;
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

public class CommandChatAdmin implements CommandExecutor, TabCompleter {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandChatAdmin.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "openchatadmin";
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP
            add(new SubCommandData("help", "openchat.commands.chatadmin", Map.of(
                    "syntax", "Commands.Admin.Help.Syntax",
                    "description", "Commands.Admin.Help.Desc"
            )));
            // RELOAD
            add(new SubCommandData("reload", "openchat.commands.chatadmin", Map.of(
                    "syntax", "",
                    "description", "Commands.Reload.Desc"
            )));
            // GREETING
            add(new SubCommandData("greeting", "openchat.commands.chatadmin", Map.of(
                    "syntax", "Commands.Admin.Greeting.Syntax",
                    "description", "Commands.Admin.Greeting.Desc"
            )));
        }
    };

    public CommandChatAdmin() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
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
            case "reload": {
                // Check if the player has permission to use the reload command
                if (!sender.hasPermission("openchat.commands.reload")) {
                    OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                    return true;
                }

                // Reload the plugin configuration
                OpenChat.Instance.reload();
                OpenChat.Instance.sendCommandReply(sender, "Commands.Reload.Done");
                return true;
            }
            case "greeting": {
                if (!sender.hasPermission("openchat.commands.chatadmin")) {
                    OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                    return true;
                }

                if (args.length < 4) {
                    OpenChat.Instance.sendCommandReply(sender, "Commands.InvalidArguments");
                    return true;
                }

                OfflinePlayer targetPlayer = OpenChat.Instance.getServer().getOfflinePlayer(args[2]);
                if (!targetPlayer.hasPlayedBefore()) {
                    OpenChat.Instance.sendCommandReply(sender, "General.PlayerNotFound", Map.of(
                            "player", args[2]
                    ));
                    return true;
                }
                String targetPlayerName = targetPlayer.getName();
                if (targetPlayerName == null)
                    targetPlayerName = args[2];

                var playerDataOpt = OpenChat.database().getPlayerData(targetPlayer.getUniqueId());
                if (playerDataOpt.isEmpty()) {
                    OpenChat.Instance.sendCommandReply(sender, "General.Error");
                    return true;
                }

                boolean isJoin;
                switch (args[3].toLowerCase()) {
                    case "join": {
                        isJoin = true;
                        break;
                    }
                    case "leave":
                    case "quit": {
                        isJoin = false;
                        break;
                    }
                    default: {
                        OpenChat.Instance.sendCommandReply(sender, "Commands.InvalidArguments");
                        return true;
                    }
                }

                String subCommand = args[1].toLowerCase();
                switch (subCommand) {
                    case "set": {
                        if (args.length < 5) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Greeting.SetUsage");
                            return true;
                        }

                        String message = String.join(" ", args).substring(args[0].length() + args[1].length() + args[2].length() + args[3].length() + 4);
                        if (!message.contains("{player}")) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.CustomGreeting.Set.NoPlayerPlaceholder");
                            return true;
                        }

                        if (message.length() > 128) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.CustomGreeting.Set.MessageTooLong",
                                    Map.of("length", "128")
                            );
                            return true;
                        }

                        // Check for swear words in the message
                        if (OpenChat.antiSwearSystem().containsSwearWord(message)) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.CustomGreeting.Set.SwearWordDetected");
                            return true;
                        }

                        // Check for advertising in the message
                        if (OpenChat.advertisementSystem().containsAdvertisement(message)) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.CustomGreeting.Set.AdvertisingDetected");
                            return true;
                        }

                        var playerData = playerDataOpt.get();
                        if (isJoin)
                            playerData.setCustomJoinMessage(message);
                        else
                            playerData.setCustomLeaveMessage(message);
                        OpenChat.database().updatePlayerData(playerData);
                        OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Greeting.Success", Map.of("player", targetPlayerName));
                        if (targetPlayer.isOnline())
                            OpenChat.Instance.sendLocalizedMsg(targetPlayer.getPlayer(), "Commands.Admin.Greeting.SuccessOther");
                        return  true;
                    }
                    case "get": {
                        var playerData = playerDataOpt.get();
                        if (isJoin)
                        {
                            if (playerData.getCustomJoinMessage() == null)
                                OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Greeting.None", Map.of("player", targetPlayerName));
                            else
                                OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Greeting.Get", Map.of("message", playerData.getCustomJoinMessage()));
                        }
                        else {
                            if (playerData.getCustomLeaveMessage() == null)
                                OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Greeting.None", Map.of("player", targetPlayerName));
                            else
                                OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Greeting.Get", Map.of("message", playerData.getCustomLeaveMessage()));
                        }
                        return  true;
                    }
                    case "clear": {
                        var playerData = playerDataOpt.get();
                        if (isJoin)
                            playerData.setCustomJoinMessage(null);
                        else
                            playerData.setCustomLeaveMessage(null);
                        OpenChat.database().updatePlayerData(playerData);
                        OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Greeting.Success", Map.of("player", targetPlayerName));
                        if (targetPlayer.isOnline())
                            OpenChat.Instance.sendLocalizedMsg(targetPlayer.getPlayer(), "Commands.Admin.Greeting.SuccessOther");
                        return  true;
                    }
                }
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
                return List.of("help", "reload", "greeting");
            }
            case 2: {
                String subCommand = args[0].toLowerCase();
                switch (subCommand) {
                    case "help":
                    case "?": {
                        return List.of("1", "5", "10");
                    }
                    case "greeting": {
                        return List.of("set", "get", "clear");
                    }
                    default:
                        return List.of();
                }
            }
            case 3: {
                String subCommand = args[0].toLowerCase();
                if (subCommand.equals("greeting")) {
                    return null; // Allow player names to be tab-completed by the server
                }
                return List.of();
            }
            case 4: {
                String subCommand = args[0].toLowerCase();
                if (subCommand.equals("greeting")) {
                    return List.of("join", "leave");
                }
                return List.of();
            }
            case 5: {
                String subCommand = args[0].toLowerCase();
                if (subCommand.equals("greeting")) {
                    String type = args[1].toLowerCase();
                    if (type.equals("set"))
                        return List.of("<message>");
                }
                return List.of();
            }
            default:
                return List.of();
        }
    }

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
