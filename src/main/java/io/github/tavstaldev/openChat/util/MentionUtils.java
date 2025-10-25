package io.github.tavstaldev.openChat.util;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import io.github.tavstaldev.openChat.models.database.EMentionDisplay;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for handling player mentions in the OpenMentions plugin.
 * Provides methods for formatting mentions and notifying players.
 */
public class MentionUtils {
    /** Logger instance for logging messages related to MentionUtils. */
    private static final PluginLogger _logger = OpenChat.logger().withModule(MentionUtils.class);

    /**
     * Handles the mention of a player by another player.
     * Determines the player's mention preferences and sends the appropriate notification.
     *
     * @param player The player being mentioned.
     * @param mentioner The player who mentioned the target player.
     */
    public static boolean mentionPlayer(@NotNull Player player, Player mentioner) {
        var playerId = player.getUniqueId();
        var mentionerId = mentioner.getUniqueId();
        if (OpenChat.database().isPlayerIgnored(playerId, mentionerId))
            return true; // Player has ignored the mentioner, return true so the mentioner will not know that they are ignored

        var mentionerCache = PlayerCacheManager.get(mentionerId);
        if (mentionerCache.getMentionCooldown() != null && LocalDateTime.now().isBefore(mentionerCache.getMentionCooldown()))
            return false; // Do not notify

        var dataOpt = OpenChat.database().getPlayerData(playerId);
        if (dataOpt.isEmpty()) {
            _logger.error("Player data not found for " + player.getName());
            return false;
        }
        var data = dataOpt.get();
        switch (data.getMentionPreference())
        {
            case ALWAYS: {
                sendMention(player, data.getMentionSound(), data.getMentionDisplay(), false, mentioner);
                break;
            }
            case SILENT_IN_COMBAT: {
                sendMention(player, data.getMentionSound(), data.getMentionDisplay(), OpenChat.combatManager().isPlayerInCombat(player), mentioner);
                break;
            }
            case NEVER_IN_COMBAT: {
                if (OpenChat.combatManager().isPlayerInCombat(player))
                    break; // Player is in combat, do not mention
                sendMention(player, data.getMentionSound(), data.getMentionDisplay(), false, mentioner);
                break;
            }
            case NEVER: {
                // Do nothing, player has disabled mentions
                break;
            }
        }

        var cooldownTime = OpenChat.config().mentionsCooldown;
        if (cooldownTime < 1)
            return true;

        mentionerCache.setMentionCooldown(LocalDateTime.now().plusSeconds(cooldownTime));
        return true;
    }

    /**
     * Sends a mention notification to a player.
     * The notification can include chat messages, action bar messages, and sounds based on the player's preferences.
     *
     * @param player The player to notify.
     * @param soundKey The key of the sound to play.
     * @param display The display type for the mention notification.
     * @param isSilent Whether the notification should be silent (no sound).
     * @param mentioner The player who mentioned the target player.
     */
    private static void sendMention(Player player, String soundKey, EMentionDisplay display, boolean isSilent, Player mentioner) {
        String actionBarMessage = OpenChat.Instance.getTranslator().localize(player, "General.ActionBarMessage", Map.of("player", mentioner.getName()));
        float volume = (float)OpenChat.config().mentionsVolume;
        float pitch = (float)OpenChat.config().mentionsPitch;
        Sound sound;
        Optional<Sound> soundResult = SoundUtils.getSound(soundKey, volume, pitch);
        // Fallback sound if not found
        sound = soundResult.orElse(
                Sound.sound(Key.key("entity.player.levelup"),
                Sound.Source.MASTER,
                volume,
                pitch
        ));
        switch (display) {
            case ALL: {
                OpenChat.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                if (!isSilent)
                    player.playSound(sound);
                break;
            }
            case ONLY_CHAT: {
                OpenChat.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                break;
            }
            case ONLY_SOUND: {
                if (!isSilent)
                    player.playSound(sound);
                break;
            }
            case ONLY_ACTIONBAR: {
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                break;
            }
            case CHAT_AND_SOUND: {
                OpenChat.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                if (!isSilent)
                    player.playSound(sound);
                break;
            }
            case CHAT_AND_ACTIONBAR: {
                OpenChat.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                break;
            }
            case ACTIONBAR_AND_SOUND: {
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                if (!isSilent)
                    player.playSound(sound);
                break;
            }
        }
    }
}