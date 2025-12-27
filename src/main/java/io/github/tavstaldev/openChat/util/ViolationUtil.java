package io.github.tavstaldev.openChat.util;

import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.models.ViolationAction;
import io.github.tavstaldev.openChat.models.database.EViolationType;
import io.github.tavstaldev.openChat.models.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ViolationUtil {

    /**
     * Handles a violation asynchronously by logging it in the database and executing
     * appropriate actions based on the number of active violations of the specified type.
     *
     * @param player      the player who committed the violation
     * @param type        the type of violation
     * @param details     additional details about the violation
     * @param actionSet   the set of violation actions to evaluate and execute
     */
    public static void handleViolationAsync(@NotNull Player player, @NotNull EViolationType type, @NotNull String details, Set<ViolationAction> actionSet) {
        // Run the violation handling logic asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(OpenChat.Instance, () -> {
            // Log the violation in the database
            OpenChat.database().addViolation(player.getUniqueId(), type, details);

            // Retrieve the number of active violations of the specified type
            int violations = OpenChat.database().getActiveViolationsByType(player.getUniqueId(), type).orElse(Set.of()).size();

            // Prepare a set of commands to execute based on the violation actions
            Set<String> commandsToRun = new HashSet<>();
            String playerName = player.getName();

            // Evaluate each action and add its command to the set if it should be executed
            for (var action : actionSet) {
                if (!action.shouldExecute(violations))
                    continue;

                commandsToRun.add(action.getCommand().replace("{player}", playerName));
            }

            String logMessageKey;
            String highlightedDetails;
            // TODO: Try make a better system for highlighting to prevent running regex multiple times
            switch (type) {
                case ADVERTISEMENT: {
                    logMessageKey = "Logging.AntiAd";
                    highlightedDetails = OpenChat.advertisementSystem().highlight(details).resultMessage;
                    break;
                }
                case SPAM_REPETITION:
                case SPAM_DELAY: {
                    logMessageKey = "Logging.AntiSpam";
                    highlightedDetails = details;
                    break;
                }
                case CURSE_WORDS: {
                    logMessageKey = "Logging.AntiSwear";
                    highlightedDetails = OpenChat.antiSwearSystem().highlight(details).resultMessage;
                    break;
                }
                case CAPS_LOCK: {
                    logMessageKey = "Logging.AntiCaps";
                    highlightedDetails = details;
                    break;
                }
                default: {
                    logMessageKey = null;
                    highlightedDetails = details;
                    break;
                }
            }

            Set<Player> logRecipients = new HashSet<>();
            Map<String, Object> args = new HashMap<>();
            if (logMessageKey != null) {
                args.put("player", playerName);
                args.put("message", highlightedDetails);

                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!target.hasPermission("openchat.notify.violation")) {
                        continue;
                    }

                    PlayerData targetData = OpenChat.database().getPlayerData(target.getUniqueId()).orElse(null);
                    if (targetData == null)
                        continue;

                    if (type == EViolationType.ADVERTISEMENT && !targetData.isAntiAdLogsEnabled() || type == EViolationType.CURSE_WORDS && !targetData.isAntiSwearLogsEnabled() ||
                            (type == EViolationType.SPAM_DELAY || type == EViolationType.SPAM_REPETITION) && !targetData.isAntiSpamLogsEnabled()) {
                        continue;
                    }

                    logRecipients.add(target);
                }
            }

            // Schedule the execution of commands on the main server thread
            Bukkit.getScheduler().runTask(OpenChat.Instance, () -> {
                logRecipients.forEach(p -> OpenChat.Instance.sendCommandReply(p, logMessageKey, args));
                commandsToRun.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            });
        });
    }
}