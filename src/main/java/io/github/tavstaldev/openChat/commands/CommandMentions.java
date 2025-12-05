package io.github.tavstaldev.openChat.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.models.database.EMentionDisplay;
import io.github.tavstaldev.openChat.models.database.EMentionPreference;
import io.github.tavstaldev.openChat.util.SoundUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandMentions implements CommandExecutor, TabCompleter {
    /** Logger instance for logging messages related to CommandMentions. */
    private final PluginLogger _logger = OpenChat.logger().withModule(CommandMentions.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "mentions";

    public static final List<SubCommandData> subCommands = List.of(
            // HELP
            new SubCommandData("help", "", Map.of(
                    "syntax", "",
                    "description", "Commands.Help.Desc"
            )),
            // SET SOUND
            new SubCommandData("sound", "", Map.of(
                    "syntax", "Commands.Sound.Syntax",
                    "description", "Commands.Sound.Desc"
            )),
            // SET DISPLAY
            new SubCommandData("display", "", Map.of(
                    "syntax", "Commands.Display.Syntax",
                    "description", "Commands.Display.Desc"
            )),
            // SET PREFERENCE
            new SubCommandData("preference", "", Map.of(
                    "syntax", "Commands.Preference.Syntax",
                    "description", "Commands.Preference.Desc"
            )),
            // IGNORE
            new SubCommandData("ignore", "", Map.of(
                    "syntax", "Commands.Ignore.Syntax",
                    "description", "Commands.Ignore.Desc"
            )),
            // UNIGNORE
            new SubCommandData("unignore", "", Map.of(
                    "syntax", "Commands.Unignore.Syntax",
                    "description", "Commands.Unignore.Desc"
            ))
    );

    public CommandMentions() {
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
        if (!player.hasPermission("openchat.commands.mentions")) {
            OpenChat.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        if (args.length == 0) {
            help(player, 1);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
            case "?": {
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (Exception ex) {
                        OpenChat.Instance.sendLocalizedMsg(player, "Commands.InvalidPage");
                        return true;
                    }
                }

                help(player, page);
                return true;
            }
            case "sound": {
                if (args.length < 2) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Mentions.Sound.Usage");
                    return true;
                }

                Optional<Sound> sound = SoundUtils.getSound(args[1]);
                if (sound.isEmpty()) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Mentions.Sound.Invalid", Map.of("value", args[1]));
                    return true;
                }

                var playerId = player.getUniqueId();
                String soundName = sound.get().name().asString();
                var dataOpt = OpenChat.database().getPlayerData(playerId);
                if (dataOpt.isEmpty()) {
                    OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
                    _logger.error("Player data not found for " + player.getName());
                    return true;
                }
                var data = dataOpt.get();
                data.setMentionSound(soundName);
                OpenChat.database().updatePlayerData(data);
                OpenChat.Instance.sendLocalizedMsg(player, "Commands.Sound.Set", Map.of(
                        "value", soundName
                ));
                return true;
            }
            case "display": {
                if (args.length < 2) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Mentions.Display.Usage");
                    return true;
                }

                EMentionDisplay display;
                try {
                    display = EMentionDisplay.valueOf(args[1]);
                } catch (Exception ignored) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Mentions.Display.Invalid", Map.of("value", args[1]));
                    return true;
                }

                var playerId = player.getUniqueId();
                var dataOpt = OpenChat.database().getPlayerData(playerId);
                if (dataOpt.isEmpty()) {
                    OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
                    _logger.error("Player data not found for " + player.getName());
                    return true;
                }
                var data = dataOpt.get();
                data.setMentionDisplay(display);
                OpenChat.database().updatePlayerData(data);
                OpenChat.Instance.sendLocalizedMsg(player, "Commands.Mentions.Display.Set", Map.of(
                        "value", display.toString()
                ));
                return true;
            }
            case "preference": {
                if (args.length < 2) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Mentions.Preference.Usage");
                    return true;
                }

                EMentionPreference preference;
                try {
                    preference = EMentionPreference.valueOf(args[1]);
                } catch (Exception ignored) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Mentions.Preference.Invalid", Map.of("value", args[1]));
                    return true;
                }

                var playerId = player.getUniqueId();
                var dataOpt = OpenChat.database().getPlayerData(playerId);
                if (dataOpt.isEmpty()) {
                    OpenChat.Instance.sendLocalizedMsg(player, "General.Error");
                    _logger.error("Player data not found for " + player.getName());
                    return true;
                }
                var data = dataOpt.get();
                data.setMentionPreference(preference);
                OpenChat.database().updatePlayerData(data);
                OpenChat.Instance.sendLocalizedMsg(player, "Commands.Mentions.Preference.Set", Map.of(
                        "value", preference.toString()
                ));
                return true;
            }
            case "ignore": {
                if (args.length < 2) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.Usage");
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (!target.hasPlayedBefore()) {
                    OpenChat.Instance.sendLocalizedMsg(player, "General.PlayerNotFound", Map.of("player", args[1]));
                    return true;
                }

                var playerId = player.getUniqueId();
                var targetId = target.getUniqueId();

                if (playerId == targetId) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.Self");
                    return true;
                }

                if (OpenChat.database().isPlayerIgnored(playerId, targetId)) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.AlreadyEnabled", Map.of("player", args[1]));
                    return true;
                }

                OpenChat.database().addIgnoredPlayer(playerId, targetId);
                OpenChat.Instance.sendLocalizedMsg(player, "Commands.Ignore.Enabled", Map.of("player", args[1]));
                return true;
            }
            case "unignore": {
                if (args.length < 2) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.Usage");
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (!target.hasPlayedBefore()) {
                    OpenChat.Instance.sendLocalizedMsg(player, "General.PlayerNotFound", Map.of("player", args[1]));
                    return true;
                }

                var playerId = player.getUniqueId();
                var targetId = target.getUniqueId();

                if (playerId == targetId) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.Self");
                    return true;
                }

                if (!OpenChat.database().isPlayerIgnored(playerId, targetId)) {
                    OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.AlreadyDisabled", Map.of("player", args[1]));
                    return true;
                }

                OpenChat.database().removeIgnoredPlayer(playerId, targetId);
                OpenChat.Instance.sendLocalizedMsg(player, "Commands.Unignore.Disabled", Map.of("player", args[1]));
                return true;
            }
        }

        help(player, 1);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        switch (args.length) {
            case 0:
            case 1:
                return subCommands.stream()
                        .map(subCmd -> subCmd.command)
                        .toList();
            case 2: {
                String subCommand = args[0].toLowerCase();
                switch (subCommand) {
                    case "sound":
                        return new ArrayList<>(SoundUtils.getAllSoundNames());
                    case "display":
                        return List.of("all", "actionbar_and_sound", "chat_and_actionbar", "chat_and_sound", "only_chat", "only_actionbar", "only_sound");
                    case "preference":
                        return List.of("always", "never_in_combat", "silent_in_combat", "never");
                    case "ignore":
                    case "unignore":
                    default:
                        return null; // Let Bukkit handle player name completions
                }
            }
            default:
                return List.of();
        }
    }

    private void help(CommandSender sender, int page) {
        int maxPage = 1 + (subCommands.size() / 15);

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
            if (index >= subCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommandData subCommand = subCommands.get(index);
            if (!subCommand.hasPermission(sender)) {
                i--;
                continue;
            }

            subCommand.send(OpenChat.Instance, sender, baseCommand);
        }

        // Display navigation buttons for the help menu
        String previousBtn, nextBtn, bottomMsg;
        if (sender instanceof Player player) {
            previousBtn = OpenChat.Instance.localize(player, "Commands.Help.PrevBtn");
            nextBtn = OpenChat.Instance.localize(player, "Commands.Help.NextBtn");
            bottomMsg = OpenChat.Instance.localize(player, "Commands.Help.Bottom", Map.of(
                    "current_page", String.valueOf(page),
                    "max_page", String.valueOf(maxPage))
            );
        } else {
            previousBtn = OpenChat.Instance.localize("Commands.Help.PrevBtn");
            nextBtn = OpenChat.Instance.localize("Commands.Help.NextBtn");
            bottomMsg = OpenChat.Instance.localize("Commands.Help.Bottom", Map.of(
                    "current_page", String.valueOf(page),
                    "max_page", String.valueOf(maxPage))
            );
        }

        Map<String, Component> bottomParams = new HashMap<>();
        bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true)
                .clickEvent(page > 1 ? ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page - 1)) : null));
        bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true)
                .clickEvent(!reachedEnd && maxPage >= page + 1 ? ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page + 1)) : null));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        sender.sendMessage(bottomComp);
    }

}
