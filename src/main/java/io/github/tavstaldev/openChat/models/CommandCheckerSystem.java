package io.github.tavstaldev.openChat.models;

import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A system for managing command checks, including spam whitelisting, command blocking,
 * and tab completion groups for players.
 */
public class CommandCheckerSystem {
    @Nullable
    private final Pattern blockedPattern; // Regex pattern for blocked commands.
    @Nullable
    private final Pattern whitelistPattern; // Regex pattern for whitelisted commands.

    private HashMap<String, TabGroup> tabGroups = new HashMap<>(); // Map of tab completion groups.

    /**
     * Constructs a new CommandCheckerSystem instance.
     * Initializes the whitelist and blocked command patterns, and sets up tab completion groups.
     */
    public CommandCheckerSystem() {
        // Combine all whitelist entries into a single regex pattern.
        StringBuilder combinedPattern = new StringBuilder();
        Set<String> entries = OpenChat.OCConfig().antiSpamCommandWhitelist;
        if (!entries.isEmpty()) {
            for (String entry : entries) {
                if (entry == null || entry.isEmpty()) continue;

                if (!combinedPattern.isEmpty()) {
                    combinedPattern.append("|");
                }
                String safeEntry = Pattern.quote(entry.trim());
                combinedPattern.append("^").append(safeEntry).append(".*");
            }
            if (!combinedPattern.isEmpty()) {
                OpenChat.Logger().Debug("Compiled command whitelist regex: (" + combinedPattern.toString() + ")");
                whitelistPattern = Pattern.compile("(" + combinedPattern.toString() + ")", Pattern.CASE_INSENSITIVE);
            }
            else
                whitelistPattern = null;
        }
        else
            whitelistPattern = null;

        combinedPattern = new StringBuilder();
        entries = OpenChat.OCConfig().commandBlockerCommands;
        if (!entries.isEmpty()) {
            for (String entry : entries) {
                if (entry == null || entry.isEmpty()) continue;

                if (!combinedPattern.isEmpty()) {
                    combinedPattern.append("|");
                }
                String safeEntry = Pattern.quote(entry.trim());
                combinedPattern.append("^").append(safeEntry).append(".*");
            }
            if (!combinedPattern.isEmpty()) {
                OpenChat.Logger().Debug("Compiled command blocker regex: (" + combinedPattern.toString() + ")");
                blockedPattern = Pattern.compile("(" + combinedPattern.toString() + ")", Pattern.CASE_INSENSITIVE);
            }
            else
                blockedPattern = null;
        }
        else
            blockedPattern = null;

        // Initialize tab completion groups from configuration.
        var groupSection = OpenChat.OCConfig().getConfigurationSection("tabCompletion.entries");
        if (groupSection == null)
            return;
        for (var group : groupSection.getKeys(false)) {
            int priority = groupSection.getInt(group + ".priority", 0);
            @Nullable String extendGroup = groupSection.getString(group + ".extend", null);
            Set<String> commands = groupSection.getStringList(group + ".commands").stream().map(String::toLowerCase).collect(Collectors.toSet());
            if (extendGroup != null && tabGroups.containsKey(extendGroup)) {
                commands.addAll(tabGroups.get(extendGroup).getCommands());
            }
            tabGroups.put(group, new TabGroup(priority, extendGroup, commands));
        }
        tabGroups = tabGroups.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().getPriority(), e1.getValue().getPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new
                ));
    }

    /**
     * Checks if a command is in the spam whitelist.
     *
     * @param command The command to check.
     * @return True if the command is whitelisted, false otherwise.
     */
    public boolean isSpamWhitelisted(String command) {
        if (whitelistPattern == null) {
            return false;
        }
        OpenChat.Logger().Debug("Checking command against whitelist: " + command);
        return whitelistPattern.matcher(command).matches();
    }

    /**
     * Checks if a command is in the blocked list.
     *
     * @param command The command to check.
     * @return True if the command is blocked, false otherwise.
     */
    public boolean isBlocked(String command) {
        if (blockedPattern == null) {
            return false;
        }
        OpenChat.Logger().Debug("Checking command against blocked list: " + command);
        return blockedPattern.matcher(command).matches();
    }

    /**
     * Filters the tab completions for a player based on their permissions and tab group.
     *
     * @param player      The player requesting tab completions.
     * @param completions The collection of available tab completions.
     */
    public void getTabCompletions(Player player, Collection<String> completions) {
        var config = OpenChat.OCConfig();
        if (player.isOp() || player.hasPermission("*") || player.hasPermission(config.tabCompletionExemptPermission))
            return;

        TabGroup group = null;
        for (var g : tabGroups.values()) {
            if (player.hasPermission(String.format("openchat.tabgroup.%s", g.getPriority()))) {
                group = g;
                break;
            }
        }
        if (group == null)
        {
            group = tabGroups.get("default");
            if (group == null) {
                OpenChat.Logger().Warn("No default tab completion group found, and the player " + player.getName() + " does not belong to any group.");
                return;
            }
        }

        Set<String> commands = new HashSet<>();
        for (String cmd : group.getCommands()) {
            if (cmd.startsWith("/"))
                cmd = cmd.substring(1);
            commands.add(cmd.toLowerCase());
        }
        completions.removeIf(x -> !commands.contains(x));
    }
}