package io.github.tavstaldev.openChat.util;

import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.models.ViolationAction;
import io.github.tavstaldev.openChat.models.database.EViolationType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
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
    public static void handleViolationAsync(Player player, EViolationType type, String details, Set<ViolationAction> actionSet) {
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

            // Schedule the execution of commands on the main server thread
            Bukkit.getScheduler().runTask(OpenChat.Instance, () ->
                    commandsToRun.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd))
            );
        });
    }
}