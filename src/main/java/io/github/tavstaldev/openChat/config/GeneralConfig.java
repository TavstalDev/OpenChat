package io.github.tavstaldev.openChat.config;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.openChat.OpenChat;

import java.util.HashMap;
import java.util.List;

public class GeneralConfig extends ConfigurationBase {

    public GeneralConfig() {
        super(OpenChat.Instance, "config.yml", null);
    }

    public String locale;
    public String  prefix;
    public boolean debug;
    public boolean usePlayerLocale;
    public boolean checkForUpdates;

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
    public boolean customChatEnableGroupFormats;
    public HashMap<String, String> customChatGroupFormats;
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
    public boolean customGreetingIgnoreVanished;
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
    public void loadDefaults() {
        locale = resolveGet("locale", "eng");
        resolveComment("locale", List.of(
                "The language locale to use for messages.",
                "If 'usePlayerLocale' is enabled, this will be the default locale for players without a set locale."
        ));
        usePlayerLocale = resolveGet("usePlayerLocale", true);
        resolveComment("usePlayerLocale", List.of(
                "If enabled, the plugin will attempt to use each player's preferred locale for messages.",
                "Players without a set or supported locale will default to the 'locale' setting."
        ));
        checkForUpdates = resolveGet("checkForUpdates", true);
        resolveComment("checkForUpdates", List.of(
                "If enabled, the plugin will check for updates on startup and notify server operators if a new version is available."
        ));
        debug = resolveGet("debug", false);
        resolveComment("debug", List.of(
                "If enabled, the plugin will output additional debug information to the console.",
                "Useful for troubleshooting issues."
        ));
        prefix = resolveGet("prefix", "&bOpen&3Chat &8»");
        resolveComment("prefix", List.of(
                "The prefix used in chat messages sent by the plugin.",
                "Supports legacy and miniMessage formatting."
        ));

        //#region Private Messaging
        privateMessagingEnabled = resolveGet("privateMessaging.enabled", true);
        resolveComment("privateMessaging.enabled", List.of(
                "Enables or disables the private messaging feature.",
                "When disabled, the plugin does not attempt to register private messaging commands."
        ));
        privateMessagingSocialSpyEnabled = resolveGet("privateMessaging.socialSpyEnabled", true);
        resolveComment("privateMessaging.socialSpyEnabled", List.of(
                "Enables or disables the social spy feature, allowing certain players to monitor private messages.",
                "Players with the appropriate permission can see private messages sent between other players."
        ));
        privateMessagingSocialSpyPermission = resolveGet("privateMessaging.socialSpyPermission", "openchat.socialspy");
        resolveComment("privateMessaging.socialSpyPermission", List.of(
                "The permission node required to use the social spy feature.",
                "Players with this permission can monitor private messages."
        ));
        privateMessagingVanishBypassPermission = resolveGet("privateMessaging.vanishBypassPermission", "openchat.bypass.vanish");
        resolveComment("privateMessaging.vanishBypassPermission", List.of(
                "The permission node that allows players to receive private messages even when they are vanished.",
                "Useful for staff members who need to stay hidden while still being reachable."
        ));
        //#endregion

        //#region Custom Chat
        customChatEnabled = resolveGet("customChat.enabled", false);
        resolveComment("customChat.enabled", List.of(
                "Enables or disables the custom chat feature.",
                "When disabled, the plugin will not modify chat messages."
        ));
        customChatLocalChatDistance = resolveGet("customChat.localChatDistance", 200);
        resolveComment("customChat.localChatDistance", List.of(
                "The distance (in blocks) for local chat.",
                "Players within this distance will see each other's messages when local chat is enabled.",
                "Set to 0 to disable local chat functionality."
        ));
        customChatLocalChatExemptPermission = resolveGet("customChat.localChatExemptPermission", "openchat.bypass.localchat");
        resolveComment("customChat.localChatExemptPermission", List.of(
                "The permission node that allows players to bypass local chat distance restrictions.",
                "Players with this permission can see all local chat messages regardless of distance."
        ));
        customChatFormat = resolveGet("customChat.format", "<{player}> {message}");
        resolveComment("customChat.format", List.of(
                "The format used for chat messages.",
                "Supports placeholders: {player} for the player's name and {message} for the chat message.",
                "Supports legacy and miniMessage formatting."
        ));
        customChatShoutEnabled = resolveGet("customChat.shoutEnabled", true);
        resolveComment("customChat.shoutEnabled", List.of(
                "Enables or disables the shout feature in chat.",
                "When enabled, players can use a specific prefix to send messages that are broadcasted to all players."
        ));
        customChatShoutFormat = resolveGet("customChat.shoutFormat", "[SHOUT] <{player}> {message}");
        resolveComment("customChat.shoutFormat", List.of(
                "The format used for shout messages.",
                "Supports placeholders: {player} for the player's name and {message} for the chat message.",
                "Supports legacy and miniMessage formatting."
        ));
        customChatShoutPermission = resolveGet("customChat.shoutPermission", "openchat.chat.shout");
        resolveComment("customChat.shoutPermission", List.of(
                "The permission node required to use the shout feature in chat.",
                "Players without this permission will not be able to send shout messages."
        ));
        customChatShoutPrefix = resolveGet("customChat.shoutPrefix", "!");
        resolveComment("customChat.shoutPrefix", List.of(
                "The prefix players must use at the start of their message to send a shout.",
                "For example, if set to '!', a message starting with '!' will be treated as a shout."
        ));
        customChatQuestionEnabled = resolveGet("customChat.questionEnabled", true);
        resolveComment("customChat.questionEnabled", List.of(
                "Enables or disables the question feature in chat.",
                "When enabled, players can use a specific prefix to send questions that are highlighted in chat."
        ));
        customChatQuestionFormat = resolveGet("customChat.questionFormat", "[QUESTION] <{player}> {message}");
        resolveComment("customChat.questionFormat", List.of(
                "The format used for question messages.",
                "Supports placeholders: {player} for the player's name and {message} for the chat message.",
                "Supports legacy and miniMessage formatting."
        ));
        customChatQuestionPermission = resolveGet("customChat.questionPermission", "openchat.chat.question");
        resolveComment("customChat.questionPermission", List.of(
                "The permission node required to use the question feature in chat.",
                "Players without this permission will not be able to send question messages."
        ));
        customChatQuestionPrefix = resolveGet("customChat.questionPrefix", "?");
        resolveComment("customChat.questionPrefix", List.of(
                "The prefix players must use at the start of their message to send a question.",
                "For example, if set to '?', a message starting with '?' will be treated as a question."
        ));
        customChatLegacyRichTextPermission = resolveGet("customChat.legacyRichTextPermission", "openchat.chat.color");
        resolveComment("customChat.legacyRichTextPermission", List.of(
                "The permission node required to use legacy color codes in chat messages.",
                "Players with this permission can use '&' or '§' followed by a color code to format their messages."
        ));
        customChatHexRichTextPermission = resolveGet("customChat.hexRichTextPermission", "openchat.chat.hexcolor");
        resolveComment("customChat.hexRichTextPermission", List.of(
                "The permission node required to use hex color codes in chat messages.",
                "Players with this permission can use hex color codes (e.g., #FF5733) to format their messages."
        ));
        //#endregion

        //#region Custom Greeting
        customGreetingEnabled = resolveGet("customGreeting.enabled", false);
        resolveComment("customGreeting.enabled", List.of(
                "Enables or disables the custom greeting feature.",
                "When enabled, custom join and leave messages will be used."
        ));
        customGreetingIgnoreVanished = resolveGet("customGreeting.ignoreVanished", true);
        resolveComment("customGreeting.ignoreVanished", List.of(
                "If enabled, players who are vanished will not trigger join or leave messages.",
                "Useful for staff members who want to stay hidden."
        ));
        customGreetingOverrideJoinMessage = resolveGet("customGreeting.overrideJoinMessage", false);
        resolveComment("customGreeting.overrideJoinMessage", List.of(
                "If enabled, the custom join message will replace the default Minecraft join message."
        ));
        customGreetingJoinMessage = resolveGet("customGreeting.joinMessage", "&8(&a+&8) &a{player}");
        resolveComment("customGreeting.joinMessage", List.of(
                "The custom join message format.",
                "Supports the placeholder {player} for the player's name.",
                "Supports legacy and miniMessage formatting."
        ));
        customGreetingOverrideLeaveMessage = resolveGet("customGreeting.overrideLeaveMessage", false);
        resolveComment("customGreeting.overrideLeaveMessage", List.of(
                "If enabled, the custom leave message will replace the default Minecraft leave message."
        ));
        customGreetingLeaveMessage = resolveGet("customGreeting.leaveMessage", "&8(&c-&8) &c{player}");
        resolveComment("customGreeting.leaveMessage", List.of(
                "The custom leave message format.",
                "Supports the placeholder {player} for the player's name.",
                "Supports legacy and miniMessage formatting."
        ));
        //#endregion

        //#region Custom Motds
        customMotdsEnabled = resolveGet("customMotds.enabled", true);
        resolveComment("customMotds.enabled", List.of(
                "Enables or disables the custom MOTD feature.",
                "When enabled, the server will display custom messages of the day to players upon joining."
        ));
        customMotds = resolveGet("customMotds.motds", List.of(
                // Default MOTD 1
                "&bWelcome to the server, {player}!\n&aEnjoy your stay and have fun!",
                // Default MOTD 2
                "&aRemember to check out our website at &n<hover:show_text:'<aqua>Click on me to visit the website.'><click:open_url:'www.example.com'>www.example.com</click></hover>&r&a!\n&aWe have tons of resources and information there.",
                // Default MOTD 3
                "&eJoin our Discord server for the latest news: &ndiscord.gg/example&r&e!\n&eConnect with the community and make new friends!"
        ));
        resolveComment("customMotds.motds", List.of(
                "List of custom messages of the day (MOTDs) displayed to players upon joining.",
                "Supports the placeholder {player} for the player's name.",
                "Supports legacy and miniMessage formatting."
        ));
        //#endregion

        //#region Mentions
        mentionsEnabled = resolveGet("mentions.enabled", true);
        resolveComment("mentions.enabled", List.of(
                "Enables or disables the mentions feature.",
                "When enabled, players can mention others in chat using '@username'."
        ));
        mentionsDefaultDisplay = resolveGet("mentions.defaultDisplay", "ALL");
        resolveComment("mentions.defaultDisplay", List.of(
                "The default display setting for mentions.",
                "Options: ALL, ONLY_CHAT, ONLY_ACTIONBAR, ONLY_SOUND, CHAT_AND_ACTIONBAR, CHAT_AND_SOUND, ACTIONBAR_AND_SOUND"
        ));
        mentionsDefaultPreference = resolveGet("mentions.defaultPreference", "ALWAYS");
        resolveComment("mentions.defaultPreference", List.of(
                "The default preference for receiving mentions.",
                "Options: ALWAYS, NEVER, NEVER_IN_COMBAT, SILENT_IN_COMBAT"
        ));
        mentionsDefaultSound = resolveGet("mentions.defaultSound", "ENTITY_PLAYER_LEVELUP");
        resolveComment("mentions.defaultSound", List.of(
                "The default sound played when a player is mentioned.",
                "Use valid sound names from the Minecraft sound list."
        ));
        mentionsVolume = resolveGet("mentions.volume", 1.0);
        resolveComment("mentions.volume", List.of(
                "The volume at which the mention sound is played.",
                "Value range: 0.0 (mute) to 1.0 (full volume) or higher."
        ));
        mentionsPitch = resolveGet("mentions.pitch", 1.0);
        resolveComment("mentions.pitch", List.of(
                "The pitch at which the mention sound is played.",
                "Value range: 0.5 (lower pitch) to 2.0 (higher pitch)."
        ));
        mentionsCooldown = resolveGet("mentions.mentionCooldown", 3);
        resolveComment("mentions.mentionCooldown", List.of(
                "The cooldown time (in seconds) between mentions for a player.",
                "Prevents spamming mentions in chat."
        ));
        mentionsLimitPerMessage = resolveGet("mentions.maxMentionsPerMessage", 3);
        resolveComment("mentions.maxMentionsPerMessage", List.of(
                "The maximum number of mentions allowed per chat message.",
                "Helps to reduce spam and excessive notifications."
        ));
        mentionsAllowSelfMention = resolveGet("mentions.allowSelfMention", false);
        resolveComment("mentions.allowSelfMention", List.of(
                "If enabled, players can mention themselves in chat.",
                "When mentioned, they will receive the mention notification."
        ));
        //#endregion
    }
}
