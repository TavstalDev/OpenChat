package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.models.database.PlayerData;
import io.github.tavstaldev.openChat.util.StringUtil;
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

import java.util.*;

/**
 * Handles the `/openchatadmin` command and its subcommands.
 * Implements both `CommandExecutor` and `TabCompleter` interfaces.
 */
public class CommandChatAdmin implements CommandExecutor, TabCompleter {
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandChatAdmin.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "openchatadmin";
    private final Map<String, String> colorCodes = Map.ofEntries(
            Map.entry("black", "#000000"),
            Map.entry("dark_blue", "#0000AA"),
            Map.entry("dark_green", "#00AA00"),
            Map.entry("dark_aqua", "#00AAAA"),
            Map.entry("dark_red", "#AA0000"),
            Map.entry("dark_purple", "#AA00AA"),
            Map.entry("gold", "#FFAA00"),
            Map.entry("gray", "#AAAAAA"),
            Map.entry("dark_gray", "#555555"),
            Map.entry("blue", "#5555FF"),
            Map.entry("green", "#55FF55"),
            Map.entry("aqua", "#55FFFF"),
            Map.entry("red", "#FF5555"),
            Map.entry("light_purple", "#FF55FF"),
            Map.entry("yellow", "#FFFF55"),
            Map.entry("white", "#FFFFFF")
    );

    // List of subcommands with their metadata
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP subcommand
            add(new SubCommandData("help", "openchat.commands.chatadmin", Map.of(
                    "syntax", "Commands.Admin.Help.Syntax",
                    "description", "Commands.Admin.Help.Desc"
            )));
            // RELOAD subcommand
            add(new SubCommandData("reload", "openchat.commands.chatadmin.reload", Map.of(
                    "syntax", "",
                    "description", "Commands.Reload.Desc"
            )));
            // GREETING subcommand
            add(new SubCommandData("greeting", "openchat.commands.chatadmin.greeting", Map.of(
                    "syntax", "Commands.Admin.Greeting.Syntax",
                    "description", "Commands.Admin.Greeting.Desc"
            )));
            // CHATCOLOR subcommand
            add(new SubCommandData("chatcolor", "openchat.commands.chatadmin.chatcolor", Map.of(
                    "syntax", "Commands.Admin.ChatColor.Syntax",
                    "description", "Commands.Admin.ChatColor.Desc"
            )));
            // LOG toggle subcommand
            add(new SubCommandData("log", "openchat.commands.chatadmin.log", Map.of(
                    "syntax", "Commands.Admin.Log.Syntax",
                    "description", "Commands.Admin.Log.Desc"
            )));
        }
    };

    /**
     * Constructor for the CommandChatAdmin class.
     * Initializes the command executor and tab completer for the `/openchatadmin` command.
     */
    public CommandChatAdmin() {
        var command = OpenChat.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
        command.setExecutor(this);
    }

    /**
     * Handles the execution of the `/openchatadmin` command.
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
            help(sender, 1);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
            case "?": {
                // Handle the help subcommand
                if (!sender.hasPermission("openchat.commands.chatadmin")) {
                    OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                    return true;
                }

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
                // Handle the reload subcommand
                if (!sender.hasPermission("openchat.commands.chatadmin.reload")) {
                    OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                    return true;
                }

                OpenChat.Instance.reload();
                OpenChat.Instance.sendCommandReply(sender, "Commands.Reload.Done");
                return true;
            }
            case "greeting": {
                // Handle the greeting subcommand
                if (!sender.hasPermission("openchat.commands.chatadmin.greeting")) {
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

                        if (OpenChat.antiSwearSystem().containsSwearWord(message)) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.CustomGreeting.Set.SwearWordDetected");
                            return true;
                        }

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
            case "chatcolor": {
                if (!sender.hasPermission("openchat.commands.chatadmin.chatcolor")) {
                    OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                    return true;
                }

                if (args.length < 3 || args.length > 4) {
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

                String subCommand = args[1].toLowerCase();
                switch (subCommand) {
                    case "set": {
                        if (args.length < 4) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.InvalidArguments");
                            return true;
                        }

                        String color = args[3].toLowerCase();
                        boolean isHex = StringUtil.isValidHexColor(color);

                        if (!(colorCodes.containsKey(color) || isHex)) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.InvalidColor", Map.of(
                                    "value", color
                            ));
                            return true;
                        }
                        String hexColor = isHex ? color : colorCodes.get(color);

                        var playerData = playerDataOpt.get();
                        playerData.setMessageColor(hexColor);
                        OpenChat.database().updatePlayerData(playerData);
                        OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.ChatColor.Success", Map.of(
                                "player", targetPlayerName,
                                "color", hexColor
                        ));
                        if (targetPlayer.isOnline())
                            OpenChat.Instance.sendLocalizedMsg(targetPlayer.getPlayer(), "Commands.Admin.ChatColor.SuccessOther", Map.of("color", hexColor));
                        return true;
                    }
                    case "get": {
                        var playerData = playerDataOpt.get();
                        String messageColor = playerData.getMessageColor();
                        if (messageColor == null) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.ChatColor.None", Map.of("player", targetPlayerName));
                        } else {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.ChatColor.Get", Map.of(
                                    "player", targetPlayerName,
                                    "color", messageColor
                            ));
                        }
                        return true;
                    }
                    case "clear": {
                        var playerData = playerDataOpt.get();
                        playerData.setMessageColor(null);
                        OpenChat.database().updatePlayerData(playerData);
                        OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.ChatColor.Clear", Map.of("player", targetPlayerName));
                        if (targetPlayer.isOnline())
                            OpenChat.Instance.sendLocalizedMsg(targetPlayer.getPlayer(), "Commands.Admin.ChatColor.ClearOther");
                        return true;
                    }
                }
            }
            case "log": {
                if (!(sender instanceof Player player)) {
                    OpenChat.Instance.sendCommandReply(sender, "Commands.ConsoleCaller");
                    return true;
                }

                if (!sender.hasPermission("openchat.commands.chatadmin.log")) {
                    OpenChat.Instance.sendCommandReply(sender, "General.NoPermission");
                    return true;
                }

                if (args.length != 2) {
                    OpenChat.Instance.sendCommandReply(sender, "Commands.InvalidArguments");
                    return true;
                }

                String subCommand = args[1].toLowerCase();
                switch (subCommand) {
                    case "swear":
                    case "antiswear":
                    case "anti-swear": {
                        Optional<PlayerData> playerData = OpenChat.database().getPlayerData(player.getUniqueId());
                        if (playerData.isEmpty()) {
                            OpenChat.Instance.sendCommandReply(sender, "General.Error");
                            return true;
                        }

                        boolean isEnabled = playerData.get().isAntiSwearLogsEnabled();
                        playerData.get().setAntiSwearLogsEnabled(!isEnabled);
                        OpenChat.database().updatePlayerData(playerData.get());
                        if (isEnabled) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Log.Disabled.Swear");
                        } else {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Log.Enabled.Swear");
                        }
                        return true;
                    }
                    case "advertisement":
                    case "antiadvertisement":
                    case "antiad":
                    case "anti-advertisement":
                    case "anti-ad": {
                        Optional<PlayerData> playerData = OpenChat.database().getPlayerData(player.getUniqueId());
                        if (playerData.isEmpty()) {
                            OpenChat.Instance.sendCommandReply(sender, "General.Error");
                            return true;
                        }

                        boolean isEnabled = playerData.get().isAntiAdLogsEnabled();
                        playerData.get().setAntiAdLogsEnabled(!isEnabled);
                        OpenChat.database().updatePlayerData(playerData.get());
                        if (isEnabled) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Log.Disabled.Advertisement");
                        } else {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Log.Enabled.Advertisement");
                        }
                        return true;
                    }
                    case "spam":
                    case "antispam":
                    case "anti-spam": {
                        Optional<PlayerData> playerData = OpenChat.database().getPlayerData(player.getUniqueId());
                        if (playerData.isEmpty()) {
                            OpenChat.Instance.sendCommandReply(sender, "General.Error");
                            return true;
                        }

                        boolean isEnabled = playerData.get().isAntiSpamLogsEnabled();
                        playerData.get().setAntiSpamLogsEnabled(!isEnabled);
                        OpenChat.database().updatePlayerData(playerData.get());
                        if (isEnabled) {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Log.Disabled.Spam");
                        } else {
                            OpenChat.Instance.sendCommandReply(sender, "Commands.Admin.Log.Enabled.Spam");
                        }
                        return true;
                    }
                }
                break;
            }
        }

        OpenChat.Instance.sendCommandReply(sender, "Commands.InvalidArguments");
        return true;

    }

    /**
     * Provides tab completion suggestions for the `/openchatadmin` command.
     *
     * @param commandSender The sender of the command.
     * @param command       The command being executed.
     * @param label         The alias of the command used.
     * @param args          The arguments provided with the command.
     * @return A list of possible completions for the last argument, or null if no completions are available.
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        switch (args.length) {
            case 0:
            case 1: {
                return List.of("help", "reload", "greeting", "chatcolor", "log");
            }
            case 2: {
                String subCommand = args[0].toLowerCase();
                switch (subCommand) {
                    case "help":
                    case "?": {
                        return List.of("1", "5", "10");
                    }
                    case "greeting":
                    case "chatcolor": {
                        return List.of("set", "get", "clear");
                    }
                    case "log": {
                        return List.of("swear", "advertisement", "spam");
                    }
                    default:
                        return List.of();
                }
            }
            case 3: {
                String subCommand = args[0].toLowerCase();
                if (subCommand.equals("greeting") || subCommand.equals("chatcolor")) {
                    return null; // Allow player names to be tab-completed by the server
                }
                return List.of();
            }
            case 4: {
                String subCommand = args[0].toLowerCase();
                if (subCommand.equals("greeting")) {
                    return List.of("join", "leave");
                }
                else if (subCommand.equals("chatcolor")) {
                    return new ArrayList<>(colorCodes.keySet());
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

    /**
     * Displays the help menu for the `/openchatadmin` command.
     *
     * @param sender The sender of the command.
     * @param page   The page number to display.
     */
    private void help(CommandSender sender, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        OpenChat.Instance.sendCommandReply(sender, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        OpenChat.Instance.sendCommandReply(sender, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;

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