package io.github.tavstaldev.openChat.config;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.models.ViolationAction;

import java.util.*;

public class ModerationConfig extends ConfigurationBase {

    public ModerationConfig() {
        super(OpenChat.Instance, "moderation.yml", null);
    }

    // Violations
    public long violationDurationMilliseconds;

    // Anti-Spam
    public boolean antiSpamEnabled, antiSpamRegexEnabled, antiSpamRegexCancel, antiSpamEmojis;
    public double antiSpamMessageSimilarityThreshold, antiSpamCommandSimilarityThreshold, antiSpamRegexCancelThreshold;
    public int antiSpamChatDelay, antiSpamCommandDelay, antiSpamMaxDuplicates, antiSpamMaxCommandDuplicates;
    public Set<String> antiSpamCommandWhitelist, antiSpamEmojiWhitelist;
    public Set<ViolationAction> antiSpamDelayViolationActions, antiSpamSimilarityViolationActions;
    public String antiSpamExemptPermission, antiSpamRegex, antiSpamEmojiExemptPermission;

    // Anti-Advertisement
    public boolean antiAdvertisementEnabled;
    public String antiAdvertisementRegex;
    public Set<String> antiAdvertisementWhitelist;
    public Set<ViolationAction> antiAdvertisementViolationActions;
    public String antiAdvertisementExemptPermission;

    // Anti-Caps
    public boolean antiCapsEnabled;
    public int antiCapsMinLength, antiCapsPercentage;
    public Set<ViolationAction> antiCapsViolationActions;
    public String antiCapsExemptPermission;

    // Anti-Swear
    public boolean antiSwearEnabled;
    // Character mapping and bad words are not stored here, since they are only called
    // when initializing the AntiSwearSystem class, so storing them here would be redundant.
    public Set<ViolationAction> antiSwearViolationActions;
    public String antiSwearExemptPermission;

    // Command Blocker
    public boolean commandBlockerEnabled;
    public boolean commandBlockerEnableBypass;
    public String commandBlockerBypassPermission;
    public Set<String> commandBlockerCommands;

    // Tab completion
    public boolean tabCompletionEnabled;
    public String tabCompletionExemptPermission;

    // OP-Protection
    public boolean opProtectionEnabled;
    public Set<String> opProtectionOperators;

    @Override
    public void loadDefaults() {
        Set<ViolationAction> violationActions;

        //#region Violations
        violationDurationMilliseconds = resolveGet("violations.ResetTime", 60) * 60 * 1000L;
        resolveComment("violations.ResetTime", List.of(
                "Time in minutes after which a player's violation count is reset.",
                "Logs are stored indefinitely, but violations older than this time will not be counted towards further actions.")
        );
        //#endregion

        //#region Anti-Spam
        antiSpamEnabled = resolveGet("antiSpam.enabled", true);
        resolveComment("antiSpam.enabled", List.of("Enables or disables the anti-spam system."));
        antiSpamRegexEnabled = resolveGet("antiSpam.regexEnabled", false);
        resolveComment("antiSpam.regexEnabled", List.of("Enables or disables regex to filter unwanted characters."));
        antiSpamRegex = resolveGet("antiSpam.regex", "(?i)[^aáÁbcdeéÉfghiíÍjklmnoóÓöÖőŐpqrstuúÚüŰűÜvwxyz0-9:)(<>;.*+?'\"+!%/=¸÷×$€|\\\\ ,-]");
        resolveComment("antiSpam.regex", List.of("Regex pattern to remove unwanted characters from messages before similarity checks.",
                "Prevents users from bypassing filters by converting letters to similar-looking characters."));
        antiSpamRegexCancel = resolveGet("antiSpam.regexCancel", false);
        resolveComment("antiSpam.regexCancel", List.of("If enabled, messages that exceed the regex cancel threshold will be cancelled entirely."));
        antiSpamRegexCancelThreshold = resolveGet("antiSpam.regexCancelThreshold", 0.5);
        resolveComment("antiSpam.regexCancelThreshold", List.of("Threshold (0.0 - 1.0) for cancelling messages based on regex filtering.",
                "If the ratio of removed characters to total characters exceeds this value, the message will be cancelled."));
        antiSpamChatDelay = resolveGet("antiSpam.chatDelay", 2);
        resolveComment("antiSpam.chatDelay", List.of("Minimum delay in seconds between consecutive chat messages from the same player."));
        antiSpamMaxDuplicates= resolveGet("antiSpam.maxDuplicates", 3);
        resolveComment("antiSpam.maxDuplicates", List.of("Maximum number of identical messages allowed within the violation duration."));
        antiSpamCommandDelay = resolveGet("antiSpam.commandDelay", 2);
        resolveComment("antiSpam.commandDelay", List.of("Minimum delay in seconds between consecutive commands from the same player."));
        antiSpamMaxCommandDuplicates= resolveGet("antiSpam.maxCommandDuplicates", 3);
        resolveComment("antiSpam.maxCommandDuplicates", List.of("Maximum number of identical commands allowed within the violation duration."));
        antiSpamMessageSimilarityThreshold = resolveGet("antiSpam.messageSimilarityThreshold", 0.8);
        resolveComment("antiSpam.messageSimilarityThreshold", List.of("Value between 0.0 and 1.0, where 1.0 is 100% identical messages."));
        antiSpamCommandSimilarityThreshold = resolveGet("antiSpam.commandSimilarityThreshold", 0.8);
        resolveComment("antiSpam.commandSimilarityThreshold", List.of("Value between 0.0 and 1.0, where 1.0 is 100% identical commands."));
        antiSpamCommandWhitelist = new LinkedHashSet<>(resolveGet("antiSpam.commandWhitelist", List.of(
                "/msg",
                "/tell",
                "/w",
                "/r",
                "/reply",
                "/t",
                "/me",
                "/whisper",
                "/warp",
                "/warps",
                "/home",
                "/spawn",
                "/joinqueue",
                "/leavequeue",
                "/kit",
                "/kits",
                "/party"
        )));
        resolveComment("antiSpam.commandWhitelist", List.of("List of commands that are exempt from anti-spam checks."));
        antiSpamExemptPermission = resolveGet("antiSpam.exemptPermission", "openchat.bypass.antispam");
        resolveComment("antiSpam.exemptPermission", List.of("Permission that exempts a player from anti-spam checks."));
        //#region Delay violation actions
        // Fill with default values if not present
        if (get("antiSpam.delayViolationActions") == null) {
            List<Map<String, Object>> defaultActions = new ArrayList<>();
            defaultActions.add(Map.of("operator", "==", "amount", 1, "command", "warn {player} Please do not send messages too quickly."));
            defaultActions.add(Map.of("operator", "==", "amount", 2, "command", "mute {player} 5m Please do not send messages too quickly."));
            defaultActions.add(Map.of("operator", ">=", "amount", 3, "command", "mute {player} 15m Please do not send messages too quickly."));
            resolve("antiSpam.delayViolationActions", defaultActions);
        }
        // Reset violation set
        violationActions = new LinkedHashSet<>();
        // Load from config
        for (Map<?, ?> actionMap : getMapList("antiSpam.delayViolationActions")) {
            ViolationAction action = ViolationAction.fromMap(actionMap);
            if (action != null) {
                violationActions.add(action);
            }
        }
        antiSpamDelayViolationActions = violationActions;
        resolveComment("antiSpam.delayViolationActions", List.of("Commands to execute when a player violates the chat or command delay. Use {player} to insert the player's name."));
        //#endregion
        //#region Similarity violation actions
        // Fill with default values if not present
        if (get("antiSpam.similarityViolationActions") == null) {
            List<Map<String, Object>> defaultActions = new ArrayList<>();
            defaultActions.add(Map.of("operator", "==", "amount", 1, "command", "warn {player} Please do not repeat yourself too much."));
            defaultActions.add(Map.of("operator", "==", "amount", 2, "command", "mute {player} 5m Please do not repeat yourself too much."));
            defaultActions.add(Map.of("operator", ">=", "amount", 3, "command", "mute {player} 15m Please do not repeat yourself too much."));
            resolve("antiSpam.similarityViolationActions", defaultActions);
        }
        // Reset violation set
        violationActions = new LinkedHashSet<>();
        // Load from config
        for (Map<?, ?> actionMap : getMapList("antiSpam.similarityViolationActions")) {
            ViolationAction action = ViolationAction.fromMap(actionMap);
            if (action != null) {
                violationActions.add(action);
            }
        }
        antiSpamSimilarityViolationActions = violationActions;
        resolveComment("antiSpam.similarityViolationActions", List.of("Commands to execute when a player exceeds the allowed duplicate messages or commands. Use {player} to insert the player's name."));
        //#endregion
        //#region Emojis
        antiSpamEmojis = resolveGet("antiSpam.emojis", true);
        resolveComment("antiSpam.emojis", List.of("Enables or disables emoji filtering in messages."));
        antiSpamEmojiExemptPermission = resolveGet("antiSpam.emojiExemptPermission", "openchat.bypass.antiemoji");
        resolveComment("antiSpam.emojiExemptPermission", List.of("Permission that exempts a player from emoji filtering."));
        antiSpamEmojiWhitelist = new LinkedHashSet<>(resolveGet("antiSpam.emojiWhitelist", List.of(
                ":amongus:",
                ":eyes:",
                ":joy:",
                ":joy_2:",
                ":angel:",
                ":stunned:",
                ":skull:",
                ":confused:",
                ":confused_2:",
                ":frozen:",
                ":contagious:",
                ":dislike:",
                ":sleeping:",
                ":elegant:",
                ":fachero:",
                ":fascinated:",
                ":angry:",
                ":wink:",
                ":scream:",
                ":like:",
                ":cry:",
                ":nerd:",
                ":nausea:",
                ":eyes_hearts:",
                ":concerned:",
                ":pull_tongue:",
                ":without_expresion:",
                ":smiling:",
                ":sad:",
                ":sad_2:",
                ":vomit:",
                ":heart:",
                ":broken_heart:",
                ":yellow_heart:",
                ":green_heart:",
                ":blue_heart:",
                ":purple_heart:",
                ":gif:",
                ":demon:",
                ":to_sigh:",
                ":sneeze:",
                ":gg:"
        )));
        resolveComment("antiSpam.emojiWhitelist", List.of("List of emojis that are allowed even when emoji filtering is enabled."));
        //#endregion
        //#endregion

        //#region Anti-Advertisement
        antiAdvertisementEnabled = resolveGet("antiAdvertisement.enabled", true);
        resolveComment("antiAdvertisement.enabled", List.of("Enables or disables the anti-advertisement system."));
        antiAdvertisementRegex = resolveGet("antiAdvertisement.regex", "(?i)\\b((?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|\\b(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+(?:[a-z]{2,}))\\b");
        resolveComment("antiAdvertisement.regex", List.of("Regex pattern to detect advertisements in chat messages."));
        antiAdvertisementWhitelist = new LinkedHashSet<>(resolveGet("antiAdvertisement.whitelist", List.of(
                "minecraft.com",
                "discord.gg/minecraft"
        )));
        resolveComment("antiAdvertisement.whitelist", List.of("List of domain substrings that are exempt from advertisement filtering."));
        antiAdvertisementExemptPermission = resolveGet("antiAdvertisement.exemptPermission", "openchat.bypass.antiadvertisement");
        resolveComment("antiAdvertisement.exemptPermission", List.of("Permission that exempts a player from anti-advertisement checks."));
        //#region Violation actions
        // Fill with default values if not present
        if (get("antiAdvertisement.violationActions") == null) {
            List<Map<String, Object>> defaultActions = new ArrayList<>();
            defaultActions.add(Map.of("operator", "==", "amount", 1, "command", "warn {player} Please do not advertise."));
            defaultActions.add(Map.of("operator", "==", "amount", 2, "command", "mute {player} 5m Please do not advertise."));
            defaultActions.add(Map.of("operator", ">=", "amount", 3, "command", "mute {player} 15m Please do not advertise."));
            resolve("antiAdvertisement.violationActions", defaultActions);
        }
        // Reset violation set
        violationActions = new LinkedHashSet<>();
        // Load from config
        for (Map<?, ?> actionMap : getMapList("antiAdvertisement.violationActions")) {
            ViolationAction action = ViolationAction.fromMap(actionMap);
            if (action != null) {
                violationActions.add(action);
            }
        }
        antiAdvertisementViolationActions = violationActions;
        resolveComment("antiAdvertisement.violationActions", List.of("Commands to execute when a player advertises in chat. Use {player} to insert the player's name."));
        //#endregion
        //#endregion

        //#region Anti-Caps
        antiCapsEnabled = resolveGet("antiCaps.enabled", true);
        resolveComment("antiCaps.enabled", List.of("Enables or disables the anti-caps system."));
        antiCapsMinLength = resolveGet("antiCaps.minLength", 10);
        resolveComment("antiCaps.minLength", List.of("Minimum message length for the anti-caps system to be applied."));
        antiCapsPercentage = resolveGet("antiCaps.percentage", 70);
        resolveComment("antiCaps.percentage", List.of("Percentage of capital letters in a message that triggers the anti-caps system."));
        antiCapsExemptPermission = resolveGet("antiCaps.exemptPermission", "openchat.bypass.anticaps");
        resolveComment("antiCaps.exemptPermission", List.of("Permission that exempts a player from anti-caps checks."));
        //#region Violation actions
        // Fill with default values if not present
        if (get("antiCaps.violationActions") == null) {
            List<Map<String, Object>> defaultActions = new ArrayList<>();
            defaultActions.add(Map.of("operator", "==", "amount", 1, "command", "warn {player} Please do not use excessive capital letters."));
            defaultActions.add(Map.of("operator", "==", "amount", 2, "command", "mute {player} 5m Please do not use excessive capital letters."));
            defaultActions.add(Map.of("operator", ">=", "amount", 3, "command", "mute {player} 15m Please do not use excessive capital letters."));
            resolve("antiCaps.violationActions", defaultActions);
        }
        // Reset violation set
        violationActions = new LinkedHashSet<>();
        // Load from config
        for (Map<?, ?> actionMap : getMapList("antiCaps.violationActions")) {
            ViolationAction action = ViolationAction.fromMap(actionMap);
            if (action != null) {
                violationActions.add(action);
            }
        }
        antiCapsViolationActions = violationActions;
        resolveComment("antiCaps.violationActions", List.of("Commands to execute when a player uses excessive capital letters. Use {player} to insert the player's name."));
        //#endregion
        //#endregion

        //#region OP-Protection
        opProtectionEnabled = resolveGet("opProtection.enabled", false);
        resolveComment("opProtection.enabled", List.of("Enables or disables the OP-protection system."));
        opProtectionOperators = new LinkedHashSet<>(resolveGet("opProtection.operators", List.of(
                "Steve",
                "Alex"
        )));
        resolveComment("opProtection.operators", List.of("List of player names that are allowed to be OPs on the server."));
        //#endregion

        //#region Command Blocker
        commandBlockerEnabled = resolveGet("commandBlocker.enabled", true);
        resolveComment("commandBlocker.enabled", List.of("Enables or disables the command blocker system."));
        commandBlockerEnableBypass = resolveGet("commandBlocker.enableBypass", true);
        resolveComment("commandBlocker.enableBypass", List.of("If enabled, players with the bypass permission can use blocked commands."));
        commandBlockerBypassPermission = resolveGet("commandBlocker.bypassPermission", "openchat.bypass.commandblocker");
        resolveComment("commandBlocker.bypassPermission", List.of("Permission that allows a player to bypass the command blocker."));
        commandBlockerCommands = new LinkedHashSet<>(resolveGet("commandBlocker.commands", List.of(
                "/?",
                "/version",
                "/ver",
                "/icanhasbukkit",
                "/about",
                "/pl",
                "/plugins",
                "/bukkit:pl",
                "/bukkit:plugins",
                "/bukkit:version",
                "/bukkit:ver",
                "/bukkit:about",
                "/bukkit:icanhasbukkit",
                "/bukkit: null",
                "/minecraft: null",
                "/minecraft:me",
                "/minecraft:tell",
                "/minecraft",
                "/minecraft:op",
                "/minecraft:pardon-ip",
                "/minecraft:pardon",
                "/calculate",
                "//calculate",
                "//eval",
                "/eval",
                "//evalaute",
                "/evaluate",
                "//solve",
                "/solve",
                "/reload",
                "/stop",
                "/op",
                "/execute",
                "/sudo",
                "/say",
                "/pt bc",
                "/bc"
        )));
        resolveComment("commandBlocker.commands", List.of(
                "List of commands that are blocked by the command blocker."
        ));
        //#endregion

        //#region Tab completion
        tabCompletionEnabled = resolveGet("tabCompletion.enabled", true);
        resolveComment("tabCompletion.enabled", List.of("Enables or disables the tab completion restriction system."));
        tabCompletionExemptPermission = resolveGet("tabCompletion.exemptPermission", "openchat.bypass.tabcompletion");
        resolveComment("tabCompletion.exemptPermission", List.of("Permission that exempts a player from tab completion restrictions."));
        if (get("tabCompletion.entries") == null) {
            var tabEntriesDefault = new LinkedHashMap<String, Object>();
            var defaultEntries = new LinkedHashMap<String, Object>();
            defaultEntries.put("priority", 0);
            defaultEntries.put("commands", List.of(
                    "/ah",
                    "/auction",
                    "/back",
                    "/bal",
                    "/balance",
                    "/commands",
                    "/cosmetics",
                    "/craft",
                    "/dailyquests",
                    "/delhome",
                    "/disposal",
                    "/discord",
                    "/emojis",
                    "/enchants",
                    "/enderchest",
                    "/feed",
                    "/fly",
                    "/heal",
                    "/help",
                    "/home",
                    "/ignore",
                    "/jobs",
                    "/kit",
                    "/kits",
                    "/leaderboards",
                    "/list",
                    "/msg",
                    "/msgtoggle",
                    "/near",
                    "/nick",
                    "/pay",
                    "/playerwarps",
                    "/ptime",
                    "/pwarp",
                    "/pweather",
                    "/pw",
                    "/quests",
                    "/realname",
                    "/recipe",
                    "/repair",
                    "/rewards",
                    "/rtp",
                    "/rules",
                    "/sellall",
                    "/sellhand",
                    "/serverguide",
                    "/sethome",
                    "/settings",
                    "/shop",
                    "/skills",
                    "/spawn",
                    "/spawners",
                    "/store",
                    "/suicide",
                    "/tags",
                    "/tpa",
                    "/tpaccept",
                    "/tpahere",
                    "/tpdeny",
                    "/tptoggle",
                    "/tutorial",
                    "/vote",
                    "/warp",
                    "/warps",
                    "/website",
                    "/worlds"
            ));

            var staffEntries = new LinkedHashMap<String, Object>();
            staffEntries.put("priority", 1);
            staffEntries.put("extend", "default");
            staffEntries.put("commands", List.of(
                    "/ban",
                    "/banip",
                    "/unban",
                    "/unbanip",
                    "/pardon",
                    "/pardonip",
                    "/banlist",
                    "/changereason",
                    "/check",
                    "/history",
                    "/kick",
                    "/mute"
            ));

            tabEntriesDefault.put("default", defaultEntries);
            tabEntriesDefault.put("staff", staffEntries);
            resolve("tabCompletion.entries", tabEntriesDefault);

            // Helping garbage collection
            defaultEntries = null;
            staffEntries = null;
            tabEntriesDefault = null;
        }
        resolveComment("tabCompletion.entries", List.of(
                "Defines sets of commands for tab completion restrictions.",
                "'priority' defines the order of application (lower numbers have higher priority).",
                "'extend' allows inheriting commands from another set.",
                "'commands' is the list of commands in this set."
        ));
        //#endregion

        //#region Anti-swear
        antiSwearEnabled = resolveGet("antiSwear.enabled", true);
        resolveComment("antiSwear.enabled", List.of("Enables or disables the anti-swear system."));
        //noinspection ExtractMethodRecommender
        Map<Character, String> characterMappings = new LinkedHashMap<>();
        characterMappings.put('a', "[aA@4]");
        characterMappings.put('á', "[áÁaA@4]");
        characterMappings.put('e', "[eE3]");
        characterMappings.put('i', "[iI1!íÍ]");
        characterMappings.put('o', "[oO0óÓ]");
        characterMappings.put('s', "[sS5$]");
        characterMappings.put('u', "[uUúÚ]");
        characterMappings.put('ü', "[üÜűŰuU]");
        characterMappings.put('t', "[tT7+]");
        characterMappings.put('g', "[gG9]");
        characterMappings.put('b', "[bB8]");
        characterMappings.put('z', "[zZ2]");
        resolveGet("antiSwear.characterMapping", characterMappings);
        resolveComment("antiSwear.characterMapping", List.of(
                "Character mappings used to detect obfuscated swear words.",
                "Each entry maps a character to a regex pattern that matches common substitutions."
        ));
        antiSwearExemptPermission = resolveGet("antiSwear.exemptPermission", "openchat.bypass.antiswear");
        resolveComment("antiSwear.exemptPermission", List.of("Permission that exempts a player from anti-swear checks."));
        //#region Violation actions
        // Fill with default values if not present
        if (get("antiSwear.violationActions") == null) {
            List<Map<String, Object>> defaultActions = new ArrayList<>();
            defaultActions.add(Map.of("operator", "==", "amount", 1, "command", "warn {player} Please watch your language."));
            defaultActions.add(Map.of("operator", "==", "amount", 2, "command", "mute {player} 5m Please watch your language."));
            defaultActions.add(Map.of("operator", ">=", "amount", 3, "command", "mute {player} 15m Please watch your language."));
            resolve("antiSwear.violationActions", defaultActions);
        }
        // Reset violation set
        violationActions = new LinkedHashSet<>();
        // Load from config
        for (Map<?, ?> actionMap : getMapList("antiSwear.violationActions")) {
            ViolationAction action = ViolationAction.fromMap(actionMap);
            if (action != null) {
                violationActions.add(action);
            }
        }
        antiSwearViolationActions = violationActions;
        resolveComment("antiSwear.violationActions", List.of("Commands to execute when a player uses swear words. Use {player} to insert the player's name."));
        //#endregion
        //#endregion

        //#region Garbage Collection helper
        violationActions = null;
        characterMappings = null;
        //#endregion
    }
}
