package io.github.tavstaldev.openChat;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.openChat.models.ViolationAction;

import java.util.*;

public class OpenChatConfiguration extends ConfigurationBase {

    public OpenChatConfiguration() {
        super(OpenChat.Instance, "config.yml", null);
    }

    // General
    public String locale, prefix;
    public boolean usePlayerLocale, checkForUpdates, debug;

    // Storage
    public String storageType, storageFilename, storageHost, storageDatabase, storageUsername, storagePassword, storageTablePrefix;
    public int storagePort;

    // Violations
    public long violationDurationMilliseconds;

    // Anti-Spam
    public boolean antiSpamEnabled;
    public double antiSpamMessageSimilarityThreshold, antiSpamCommandSimilarityThreshold;
    public int antiSpamChatDelay, antiSpamCommandDelay, antiSpamMaxDuplicates, antiSpamMaxCommandDuplicates;
    public Set<String> antiSpamCommandWhitelist;
    public Set<ViolationAction> antiSpamDelayViolationActions, antiSpamSimilarityViolationActions;
    public String antiSpamExemptPermission;

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

    // Private Messaging
    public boolean privateMessagingEnabled;
    public boolean privateMessagingSocialSpyEnabled;
    public String privateMessagingSocialSpyPermission;
    public String privateMessagingVanishBypassPermission;

    // Custom Chat
    public boolean customChatEnabled;
    public int customChatLocalChatDistance;
    public String customChatLocalChatExemptPermission;
    public String customChatFormat;
    public String customChatShoutFormat;
    public String customChatQuestionFormat;
    public boolean customChatShoutEnabled;
    public String customChatShoutPermission;
    public String customChatShoutPrefix;
    public boolean customChatQuestionEnabled;
    public String customChatQuestionPermission;
    public String customChatQuestionPrefix;
    public String customChatLegacyRichTextPermission;
    public String customChatHexRichTextPermission;

    // Custom Greeting
    public boolean customGreetingEnabled;
    public boolean customGreetingOverrideJoinMessage;
    public String customGreetingJoinMessage;
    public boolean customGreetingOverrideLeaveMessage;
    public String customGreetingLeaveMessage;

    // Custom Motds
    public boolean customMotdsEnabled;
    public List<String> customMotds;

    // Mentions
    public boolean mentionsEnabled;
    public String mentionsDefaultDisplay, mentionsDefaultPreference, mentionsDefaultSound;
    public double mentionsVolume, mentionsPitch;
    public int mentionsCooldown, mentionsLimitPerMessage;
    public boolean mentionsAllowSelfMention;

    @Override
    protected void loadDefaults() {
        Set<ViolationAction> violationActions;

        //#region General
        locale = resolveGet("locale", "eng");
        usePlayerLocale = resolveGet("usePlayerLocale", true);
        checkForUpdates = resolveGet("checkForUpdates", true);
        debug = resolveGet("debug", false);
        prefix = resolveGet("prefix", "&bOpen&3Chat &8»");
        //#endregion

        //#region Storage
        storageType = resolveGet("storage.type", "sqlite");
        storageFilename = resolveGet("storage.filename", "database");
        storageHost = resolveGet("storage.host", "localhost");
        storagePort = resolveGet("storage.port", 3306);
        storageDatabase = resolveGet("storage.database", "minecraft");
        storageUsername = resolveGet("storage.username", "root");
        storagePassword = resolveGet("storage.password", "ascent");
        storageTablePrefix = resolveGet("storage.tablePrefix", "openchat");
        //#endregion

        //#region Violations
        violationDurationMilliseconds = resolveGet("violations.ResetTime", 60) * 60 * 1000L;
        resolveComment("violations.ResetTime", List.of(
                "Time in minutes after which a player's violation count is reset.",
                "Logs are stored indefinitely, but violations older than this time will not be counted towards further actions.")
        );
        //#endregion

        //#region Anti-Spam
        antiSpamEnabled = resolveGet("antiSpam.enabled", true);
        antiSpamChatDelay = resolveGet("antiSpam.chatDelay", 2);
        antiSpamMaxDuplicates= resolveGet("antiSpam.maxDuplicates", 3);
        antiSpamCommandDelay = resolveGet("antiSpam.commandDelay", 2);
        antiSpamMaxCommandDuplicates= resolveGet("antiSpam.maxCommandDuplicates", 3);
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
        antiSpamExemptPermission = resolveGet("antiSpam.exemptPermission", "openchat.bypass.antispam");
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
        //#endregion

        //#region Anti-Advertisement
        antiAdvertisementEnabled = resolveGet("antiAdvertisement.enabled", true);
        antiAdvertisementRegex = resolveGet("antiAdvertisement.regex", "(?i)\\b((?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|\\b(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+(?:[a-z]{2,}))\\b");
        antiAdvertisementWhitelist = new LinkedHashSet<>(resolveGet("antiAdvertisement.whitelist", List.of(
                "minecraft.com",
                "discord.gg/minecraft"
        )));
        antiAdvertisementExemptPermission = resolveGet("antiAdvertisement.exemptPermission", "openchat.bypass.antiadvertisement");
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
        //#endregion
        //#endregion

        //#region Anti-Caps
        antiCapsEnabled = resolveGet("antiCaps.enabled", true);
        antiCapsMinLength = resolveGet("antiCaps.minLength", 10);
        antiCapsPercentage = resolveGet("antiCaps.percentage", 70);
        antiCapsExemptPermission = resolveGet("antiCaps.exemptPermission", "openchat.bypass.anticaps");
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
        //#endregion
        //#endregion

        //#region OP-Protection
        opProtectionEnabled = resolveGet("opProtection.enabled", false);
        opProtectionOperators = new LinkedHashSet<>(resolveGet("opProtection.operators", List.of(
                "Steve",
                "Alex"
        )));
        //#endregion

        //#region Private Messaging
        privateMessagingEnabled = resolveGet("privateMessaging.enabled", true);
        privateMessagingSocialSpyEnabled = resolveGet("privateMessaging.socialSpyEnabled", true);
        privateMessagingSocialSpyPermission = resolveGet("privateMessaging.socialSpyPermission", "openchat.socialspy");
        privateMessagingVanishBypassPermission = resolveGet("privateMessaging.vanishBypassPermission", "openchat.bypass.vanish");
        //#endregion

        //#region Custom Chat
        customChatEnabled = resolveGet("customChat.enabled", false);
        customChatLocalChatDistance = resolveGet("customChat.localChatDistance", 200);
        customChatLocalChatExemptPermission = resolveGet("customChat.localChatExemptPermission", "openchat.bypass.localchat");
        customChatFormat = resolveGet("customChat.format", "<{player}> {message}");
        customChatShoutEnabled = resolveGet("customChat.shoutEnabled", true);
        customChatShoutFormat = resolveGet("customChat.shoutFormat", "[SHOUT] <{player}> {message}");
        customChatShoutPermission = resolveGet("customChat.shoutPermission", "openchat.chat.shout");
        customChatShoutPrefix = resolveGet("customChat.shoutPrefix", "!");
        customChatQuestionEnabled = resolveGet("customChat.questionEnabled", true);
        customChatQuestionFormat = resolveGet("customChat.questionFormat", "[QUESTION] <{player}> {message}");
        customChatQuestionPermission = resolveGet("customChat.questionPermission", "openchat.chat.question");
        customChatQuestionPrefix = resolveGet("customChat.questionPrefix", "?");
        customChatLegacyRichTextPermission = resolveGet("customChat.legacyRichTextPermission", "openchat.chat.color");
        customChatHexRichTextPermission = resolveGet("customChat.hexRichTextPermission", "openchat.chat.hexcolor");
        //#endregion

        //#region Custom Greeting
        customGreetingEnabled = resolveGet("customGreeting.enabled", false);
        customGreetingOverrideJoinMessage = resolveGet("customGreeting.overrideJoinMessage", false);
        customGreetingJoinMessage = resolveGet("customGreeting.joinMessage", "&8(&a+&8) &a{player}");
        customGreetingOverrideLeaveMessage = resolveGet("customGreeting.overrideLeaveMessage", false);
        customGreetingLeaveMessage = resolveGet("customGreeting.leaveMessage", "&8(&c-&8) &c{player}");
        //#endregion

        //#region Custom Motds
        customMotdsEnabled = resolveGet("customMotds.enabled", true);
        customMotds = resolveGet("customMotds.motds", List.of(
                // Default MOTD 1
                "&bWelcome to the server, {player}!\n&aEnjoy your stay and have fun!",
                // Default MOTD 2
                "&aRemember to check out our website at &n<hover:show_text:'<aqua>Click on me to visit the website.'><click:open_url:'www.example.com'>www.example.com</click></hover>&r&a!\n&aWe have tons of resources and information there.",
                // Default MOTD 3
                "&eJoin our Discord server for the latest news: &ndiscord.gg/example&r&e!\n&eConnect with the community and make new friends!"
        ));
        //#endregion

        //#region Mentions
        mentionsEnabled = resolveGet("mentions.enabled", true);
        mentionsDefaultDisplay = resolveGet("mentions.defaultDisplay", "ALL");
        mentionsDefaultPreference = resolveGet("mentions.defaultPreference", "ALWAYS");
        mentionsDefaultSound = resolveGet("mentions.defaultSound", "ENTITY_PLAYER_LEVELUP");
        mentionsVolume = resolveGet("mentions.volume", 1.0);
        mentionsPitch = resolveGet("mentions.pitch", 1.0);
        mentionsCooldown = resolveGet("mentions.mentionCooldown", 3);
        mentionsLimitPerMessage = resolveGet("mentions.maxMentionsPerMessage", 3);
        mentionsAllowSelfMention = resolveGet("mentions.allowSelfMention", false);
        //#endregion

        //#region Command Blocker
        commandBlockerEnabled = resolveGet("commandBlocker.enabled", true);
        commandBlockerEnableBypass = resolveGet("commandBlocker.enableBypass", true);
        commandBlockerBypassPermission = resolveGet("commandBlocker.bypassPermission", "openchat.bypass.commandblocker");
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
        //#endregion

        //#region Tab completion
        tabCompletionEnabled = resolveGet("tabCompletion.enabled", true);
        tabCompletionExemptPermission = resolveGet("tabCompletion.exemptPermission", "openchat.bypass.tabcompletion");
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
        //#endregion

        //#region Anti-swear
        antiSwearEnabled = resolveGet("antiSwear.enabled", true);
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
        //#region Swear words
        resolveGet("antiSwear.swearWords", new String[]{
                "asshole",
                "bitch",
                "bastard",
                "cunt",
                "damn",
                "dick",
                "fuck",
                "hell",
                "piss",
                "pussy",
                "retard",
                "shit",
                "slut",
                "twat",
                "whore",
                "chink",
                "dyke",
                "faggot",
                "gook",
                "kike",
                "nigger",
                "spic",
                "tranny",
                "wetback",
                "blowjob",
                "cock",
                "ejaculate",
                "jerkoff",
                "masturbate",
                "orgasm",
                "penis",
                "semen",
                "vagina",
        });
        //#endregion
        //#region Swear whitelist
        resolveGet("antiSwear.whitelist", new String[]{
                "hello",
                "shuttle"
        });
        //#endregion
        antiSwearExemptPermission = resolveGet("antiSwear.exemptPermission", "openchat.bypass.antiswear");
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
        //#endregion
        //#endregion

        //#region Garbage Collection helper
        violationActions = null;
        characterMappings = null;
        //#endregion
    }
}
