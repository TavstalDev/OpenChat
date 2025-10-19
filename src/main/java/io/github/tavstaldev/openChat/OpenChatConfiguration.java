package io.github.tavstaldev.openChat;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;

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

    // Anti-Spam
    public boolean antiSpamEnabled;
    public int antiSpamChatDelay, antiSpamCommandDelay, antiSpamMaxDuplicates, antiSpamMaxCommandDuplicates;
    public Set<String> antiSpamCommandWhitelist;
    public Set<String> antiSpamExecuteCommand;
    public String antiSpamExemptPermission;

    // Anti-Advertisement
    public boolean antiAdvertisementEnabled;
    public String antiAdvertisementRegex;
    public Set<String> antiAdvertisementWhitelist;
    public Set<String> antiAdvertisementExecuteCommand;
    public String antiAdvertisementExemptPermission;

    // Anti-Caps
    public boolean antiCapsEnabled;
    public int antiCapsMinLength, antiCapsPercentage;
    public Set<String> antiCapsExecuteCommand;
    public String antiCapsExemptPermission;

    // Anti-Swear
    public boolean antiSwearEnabled;
    // Character mapping and bad words are not stored here, since they are only called
    // when initializing the AntiSwearSystem class, so storing them here would be redundant.
    public Set<String> antiSwearExecuteCommand;
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

    // Mentions
    public boolean mentionsEnabled;
    public String mentionsDefaultDisplay, mentionsDefaultPreference, mentionsDefaultSound;
    public double mentionsVolume, mentionsPitch;
    public int mentionsCooldown, mentionsLimitPerMessage;
    public boolean mentionsAllowSelfMention;

    @Override
    protected void loadDefaults() {
        // General
        locale = resolveGet("locale", "eng");
        usePlayerLocale = resolveGet("usePlayerLocale", true);
        checkForUpdates = resolveGet("checkForUpdates", true);
        debug = resolveGet("debug", false);
        prefix = resolveGet("prefix", "&bOpen&3Chat &8»");

        // Storage
        storageType = resolveGet("storage.type", "sqlite");
        storageFilename = resolveGet("storage.filename", "database");
        storageHost = resolveGet("storage.host", "localhost");
        storagePort = resolveGet("storage.port", 3306);
        storageDatabase = resolveGet("storage.database", "minecraft");
        storageUsername = resolveGet("storage.username", "root");
        storagePassword = resolveGet("storage.password", "ascent");
        storageTablePrefix = resolveGet("storage.tablePrefix", "openchat");

        // Anti-Spam
        antiSpamEnabled = resolveGet("antiSpam.enabled", true);
        antiSpamChatDelay = resolveGet("antiSpam.chatDelay", 2);
        antiSpamMaxDuplicates= resolveGet("antiSpam.maxDuplicates", 3);
        antiSpamCommandDelay = resolveGet("antiSpam.commandDelay", 2);
        antiSpamMaxCommandDuplicates= resolveGet("antiSpam.maxCommandDuplicates", 3);
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
        antiSpamExecuteCommand =  new LinkedHashSet<>(resolveGet("antiSpam.executeCommand", List.of("kick {player} Please do not spam")));


        // Anti-Advertisement
        antiAdvertisementEnabled = resolveGet("antiAdvertisement.enabled", true);
        antiAdvertisementRegex = resolveGet("antiAdvertisement.regex", "(?i)\\b((?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|\\b(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+(?:[a-z]{2,}))\\b");
        antiAdvertisementWhitelist = new LinkedHashSet<>(resolveGet("antiAdvertisement.whitelist", List.of(
                "minecraft.com",
                "discord.gg/minecraft"
        )));
        antiAdvertisementExemptPermission = resolveGet("antiAdvertisement.exemptPermission", "openchat.bypass.antiadvertisement");
        antiAdvertisementExecuteCommand = new LinkedHashSet<>(resolveGet("antiAdvertisement.executeCommand", List.of("kick {player} Please do not advertise")));

        // Anti-Caps
        antiCapsEnabled = resolveGet("antiCaps.enabled", true);
        antiCapsMinLength = resolveGet("antiCaps.minLength", 10);
        antiCapsPercentage = resolveGet("antiCaps.percentage", 70);
        antiCapsExemptPermission = resolveGet("antiCaps.exemptPermission", "openchat.bypass.anticaps");
        antiCapsExecuteCommand = new LinkedHashSet<>(resolveGet("antiCaps.executeCommand", List.of("kick {player} Please do not spam")));

        // OP-Protection
        opProtectionEnabled = resolveGet("opProtection.enabled", false);
        opProtectionOperators = new LinkedHashSet<>(resolveGet("opProtection.operators", List.of(
                "Steve",
                "Alex"
        )));

        // Private Messaging
        privateMessagingEnabled = resolveGet("privateMessaging.enabled", true);
        privateMessagingSocialSpyEnabled = resolveGet("privateMessaging.socialSpyEnabled", true);
        privateMessagingSocialSpyPermission = resolveGet("privateMessaging.socialSpyPermission", "openchat.socialspy");
        privateMessagingVanishBypassPermission = resolveGet("privateMessaging.vanishBypassPermission", "openchat.bypass.vanish");

        // Custom Chat
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

        // Mentions
        mentionsEnabled = resolveGet("mentions.enabled", true);
        mentionsDefaultDisplay = resolveGet("mentions.defaultDisplay", "ALL");
        mentionsDefaultPreference = resolveGet("mentions.defaultPreference", "ALWAYS");
        mentionsDefaultSound = resolveGet("mentions.defaultSound", "ENTITY_PLAYER_LEVELUP");
        mentionsVolume = resolveGet("mentions.volume", 1.0);
        mentionsPitch = resolveGet("mentions.pitch", 1.0);
        mentionsCooldown = resolveGet("mentions.mentionCooldown", 3);
        mentionsLimitPerMessage = resolveGet("mentions.maxMentionsPerMessage", 3);
        mentionsAllowSelfMention = resolveGet("mentions.allowSelfMention", true);

        // Command Blocker
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


        // Tab completion
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
        }

        // Anti-swear
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
        antiSwearExecuteCommand = new LinkedHashSet<>(resolveGet("antiSwear.executeCommand", List.of("kick {player} Please do not swear")));
    }
}
