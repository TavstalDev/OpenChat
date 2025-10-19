package io.github.tavstaldev.openChat.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.OpenChatConfiguration;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.models.PlayerCache;
import io.github.tavstaldev.openChat.util.MentionUtils;
import io.github.tavstaldev.openChat.util.PlayerUtil;
import io.github.tavstaldev.openChat.util.VanishUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener for handling chat-related events in the OpenChat plugin.
 * Implements various anti-abuse mechanisms such as anti-spam, anti-advertisement,
 * anti-capitalization, and anti-swearing.
 */
public class ChatEventListener implements Listener {
    private final PluginLogger _logger = OpenChat.logger().withModule(ChatEventListener.class);
    private final Pattern minecraftUsernamePattern = Pattern.compile("([a-zA-Z0-9_]{3,16})(?![a-zA-Z0-9_])");
    private final Pattern legacyPattern = Pattern.compile("(?i)[&§]([0-9a-fk-or])");
    private final Pattern hexPattern = Pattern.compile("(?i)[&§]#([A-Fa-f0-9]{6})");

    /**
     * Constructor for ChatEventListener.
     * Registers the event listener with the Bukkit plugin manager.
     *
     * @param plugin The plugin instance to register the listener for.
     */
    public ChatEventListener(Plugin plugin) {
        _logger.debug("Registering chat event listener...");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        _logger.debug("Event listener registered.");
    }

    /**
     * Handles the AsyncPlayerChatEvent to apply various chat moderation features.
     *
     * @param event The chat event triggered when a player sends a message.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled())
            return;

        Player source = event.getPlayer(); // The player who sent the message.
        var sourceId = source.getUniqueId();
        PlayerCache cache = PlayerCacheManager.get(sourceId); // Retrieve the player's cache.
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.originalMessage()); // The raw chat message.
        cache.setLastChatMessage(rawMessage); // Store the last chat message in the cache.
        OpenChatConfiguration config = OpenChat.config(); // Retrieve the plugin configuration.

        // Debug log the received message to find false positives
        _logger.debug("Player " + source.getName() + " sent message: " + rawMessage);

        // Anti-spam
        if (config.antiSpamEnabled && !source.hasPermission(config.antiSpamExemptPermission)) {
            // Feature: Chat cooldown
            var chatCooldown = cache.getChatMessageDelay();
            if (LocalDateTime.now().isBefore(chatCooldown)) {
                event.setCancelled(true);
                // The +1 ensures that it doesn't display 0 seconds remaining when the cooldown is about to expire
                OpenChat.Instance.sendLocalizedMsg(source, "AntiSpam.ChatCooldown", Map.of("time", String.valueOf(chatCooldown.getSecond() - LocalDateTime.now().getSecond() + 1)));
                for (String cmd : config.antiSpamExecuteCommand) {
                    Bukkit.getScheduler().runTask(OpenChat.Instance, () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()))
                    );
                }
                return;
            }

            // Feature: Repeated messages
            // TODO: Improve repeated message detection to ignore minor differences (e.g., punctuation, spacing)
            if (config.antiSpamMaxDuplicates >= 1 && cache.getChatSpamCount() >= config.antiSpamMaxDuplicates) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiSpam.RepeatedMessages");
                for (String cmd : config.antiSpamExecuteCommand) {
                    Bukkit.getScheduler().runTask(OpenChat.Instance, () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()))
                    );
                }
                return;
            }
        }

        // Anti-advertisement
        if (config.antiAdvertisementEnabled && !source.hasPermission(config.antiAdvertisementExemptPermission)) {
            if (OpenChat.advertisementSystem().containsAdvertisement(rawMessage)) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiAd.AdvertisementDetected");
                for (String cmd : config.antiAdvertisementExecuteCommand) {
                    Bukkit.getScheduler().runTask(OpenChat.Instance, () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()))
                    );
                }
                return;
            }
        }


        // Anti-capitalization
        if (config.antiCapsEnabled && !source.hasPermission(config.antiCapsExemptPermission)) {
            double maxCapsPercentage = config.antiCapsPercentage / 100.0; // Maximum allowed percentage of capital letters.
            if (rawMessage.length() >= config.antiCapsMinLength) { // Check if the message meets the minimum length.
                long capsCount = rawMessage.chars().filter(Character::isUpperCase).count(); // Count uppercase letters.
                double capsPercentage = (double) capsCount / rawMessage.length(); // Calculate the percentage of uppercase letters.
                if (capsPercentage > maxCapsPercentage) {
                    event.setCancelled(true);
                    OpenChat.Instance.sendLocalizedMsg(source, "AntiCaps.TooManyCaps");
                    for (String cmd : config.antiCapsExecuteCommand) {
                        Bukkit.getScheduler().runTask(OpenChat.Instance, () ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()))
                        );
                    }
                    return;
                }
            }
        }

        // Anti-swear
        if (config.antiSwearEnabled && !source.hasPermission(config.antiSwearExemptPermission) ) {
            if (OpenChat.antiSwearSystem().containsSwearWord(rawMessage)) {
                event.setCancelled(true);
                OpenChat.Instance.sendLocalizedMsg(source, "AntiSwear.WordDetected");
                for (String cmd : config.antiSwearExecuteCommand) {
                    Bukkit.getScheduler().runTask(OpenChat.Instance, () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", source.getName()))
                    );
                }
                return;
            }
        }

        int spamDelay = config.antiSpamChatDelay;
        if (spamDelay > 0)
            cache.setChatMessageDelay(LocalDateTime.now().plusSeconds(spamDelay));

        // Custom chat formatting & Mentions
        if (!config.customChatEnabled)
        {
            rawMessage = handleMentions(source, rawMessage, config);
            event.message(ChatUtils.translateColors(rawMessage, true));
            return;
        }
        String chatFormat = config.customChatFormat;
        boolean forceGlobal = false;
        if (config.customChatShoutEnabled && source.hasPermission(config.customChatShoutPermission) && rawMessage.startsWith(config.customChatShoutPrefix)) {
            chatFormat = config.customChatShoutFormat;
            rawMessage = rawMessage.substring(1); // Remove the shout prefix
            forceGlobal = true;
        } else if (config.customChatQuestionEnabled && source.hasPermission(config.customChatQuestionPermission) && rawMessage.startsWith(config.customChatQuestionPrefix)) {
            chatFormat = config.customChatQuestionFormat;
            rawMessage = rawMessage.substring(1); // Remove the question prefix
            forceGlobal = true;
        }

        // Remove recipient players who have ignored the sender or disabled public chat
        if (!(forceGlobal || source.hasPermission(config.customChatLocalChatExemptPermission))) {
            if (config.customChatLocalChatDistance > 0) {
                event.viewers().removeIf(recipient -> {
                    if (!(recipient instanceof Player recipientPlayer)) {
                        return false;
                    }

                    UUID recipientId = recipientPlayer.getUniqueId();
                    // 0. Never remove the sender themselves
                    if (recipientId.equals(sourceId)) {
                        return false;
                    }

                    // 1. Never remove if the recipient has social spy enabled
                    if (OpenChat.database().isSocialSpyEnabled(recipientPlayer)) {
                        return false;
                    }

                    // 2. Always remove if the recipient has ignored the sender
                    if (OpenChat.database().isPlayerIgnored(recipientId, sourceId)) {
                        return true;
                    }

                    // 3, Remove if not in the same world
                    if (!recipientPlayer.getWorld().getUID().equals(source.getWorld().getUID())) {
                        return true;
                    }

                    // 4. Remove if public chat is disabled
                    if (OpenChat.database().isPublicChatDisabled(recipientId)) {
                        return true;
                    }

                    // 4. Remove if public chat is disabled AND the recipient is outside the local chat distance
                    var distance = Math.abs(recipientPlayer.getLocation().distance(source.getLocation()));
                    return distance > config.customChatLocalChatDistance;
                });
            } else {
                event.viewers().removeIf(recipient -> {
                    if (!(recipient instanceof Player recipientPlayer)) {
                        return false;
                    }

                    UUID recipientId = recipientPlayer.getUniqueId();
                    // 0. Never remove the sender themselves
                    if (recipientId.equals(sourceId)) {
                        return false;
                    }

                    // 1. Never remove if the recipient has social spy enabled
                    if (OpenChat.database().isSocialSpyEnabled(recipientPlayer)) {
                        return false;
                    }

                    // 2. Remove if public chat is disabled
                    if (OpenChat.database().isPublicChatDisabled(recipientId)) {
                        return true;
                    }

                    // 3. Remove if the recipient has ignored the sender
                    return OpenChat.database().isPlayerIgnored(recipientPlayer.getUniqueId(), sourceId);
                });
            }
        }

        chatFormat = PlaceholderAPI.setPlaceholders(source, chatFormat);
        if (!source.hasPermission(config.customChatHexRichTextPermission)) {
            rawMessage = hexPattern.matcher(rawMessage).replaceAll("");
        }
        if (!source.hasPermission(config.customChatLegacyRichTextPermission)) {
            rawMessage = legacyPattern.matcher(rawMessage).replaceAll("");
        }

        // Mentions
        rawMessage = handleMentions(source, rawMessage, config);

        // Other replacements are handled by PlaceholderAPI above
        chatFormat = chatFormat.replace("{player}", source.getName())
                .replace("{displayname}", PlayerUtil.getPlayerPlainDisplayName(source))
                .replace("{message}", rawMessage);

        // Escape any other literal '%' signs
        chatFormat = chatFormat.replace("%", "%%");
        String finalChatFormat = chatFormat;
        event.renderer((renderSource, sourceDisplayName, message, viewer) -> ChatUtils.translateColors(finalChatFormat, true));
        //event.message(ChatUtils.translateColors(rawMessage, true));
    }

    /**
     * Handles mentions in chat messages.
     * Replaces mentions with formatted text and notifies mentioned players.
     *
     * @param source The player who sent the message.
     * @param rawMessage The raw chat message.
     * @param config The plugin configuration.
     * @return The modified message with mentions handled.
     */
    private String handleMentions(Player source, String rawMessage, OpenChatConfiguration config) {
        if (!config.mentionsEnabled) {
            return rawMessage;
        }

        int mentionCount = 0;
        StringBuilder newMessage = new StringBuilder();
        int lastAppendPosition = 0;
        final int maxMentionCount = config.mentionsLimitPerMessage;
        final boolean allowSelfMention = config.mentionsAllowSelfMention;
        Matcher matcher = minecraftUsernamePattern.matcher(rawMessage);
        while (matcher.find() && mentionCount < maxMentionCount) {
            String mentionName = matcher.group(0);
            Player mentionedPlayer = Bukkit.getPlayerExact(mentionName);
            if (mentionedPlayer == null)
                continue;

            if (mentionedPlayer.getUniqueId() == source.getUniqueId() && !allowSelfMention)
                continue;

            if (mentionedPlayer.getGameMode() == org.bukkit.GameMode.SPECTATOR)
                continue;

            if (VanishUtil.isVanished(mentionedPlayer))
                continue;

            if (!MentionUtils.mentionPlayer(mentionedPlayer, source))
                continue;

            newMessage.append(rawMessage, lastAppendPosition, matcher.start());
            String replacement = "<yellow>@" + mentionName + "</yellow>";
            newMessage.append(replacement);

            lastAppendPosition = matcher.end();
            mentionCount++;
        }

        if (lastAppendPosition < rawMessage.length()) {
            newMessage.append(rawMessage, lastAppendPosition, rawMessage.length());
        }
        return newMessage.toString();
    }
}
