package io.github.tavstaldev.openChat.models;

import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

/**
 * Represents a cache for storing player-specific data related to chat and command usage.
 * Tracks the last chat message, command, spam counts, and delays for a player.
 */
public class PlayerCache {
    private final Player _player; // The player associated with this cache.
    private String lastChatMessage = ""; // The last chat message sent by the player.
    private int chatSpamCount = 0; // The number of consecutive duplicate chat messages.
    public LocalDateTime chatMessageDelay; // The timestamp of the last allowed chat message.
    private String lastCommand = ""; // The last command executed by the player.
    private int commandSpamCount = 0; // The number of consecutive duplicate commands.
    public LocalDateTime commandDelay; // The timestamp of the last allowed command.

    /**
     * Constructs a PlayerCache for the specified player.
     * Initializes chat and command delays to allow immediate usage after joining.
     *
     * @param player The player associated with this cache.
     */
    public PlayerCache(Player player) {
        this._player = player;
        // Make sure the player can chat/command immediately after joining
        chatMessageDelay = LocalDateTime.now().minusHours(1);
        commandDelay = LocalDateTime.now().minusHours(1);
    }

    /**
     * Retrieves the last chat message sent by the player.
     *
     * @return The last chat message.
     */
    public String getLastChatMessage() {
        return lastChatMessage;
    }

    /**
     * Retrieves the number of consecutive duplicate chat messages sent by the player.
     *
     * @return The chat spam count.
     */
    public int getChatSpamCount() {
        return chatSpamCount;
    }

    /**
     * Updates the last chat message sent by the player.
     * Resets the spam count if the message is different, otherwise increments it.
     *
     * @param message The new chat message.
     */
    public void setLastChatMessage(String message) {
        if (message.equalsIgnoreCase(lastChatMessage)) {
            chatSpamCount++;
        } else {
            this.lastChatMessage = message;
            chatSpamCount = 0;
        }
    }

    /**
     * Retrieves the last command executed by the player.
     *
     * @return The last command.
     */
    public String getLastCommand() {
        return lastCommand;
    }

    /**
     * Retrieves the number of consecutive duplicate commands executed by the player.
     *
     * @return The command spam count.
     */
    public int getCommandSpamCount() {
        return commandSpamCount;
    }

    /**
     * Updates the last command executed by the player.
     * Resets the spam count if the command is different, otherwise increments it.
     *
     * @param command The new command.
     */
    public void setLastCommand(String command) {
        if (command.equalsIgnoreCase(lastCommand)) {
            if (OpenChat.CommandCheckerSystem().isSpamWhitelisted(command))
                return;
            commandSpamCount++;
        } else {
            this.lastCommand = command;
            commandSpamCount = 0;
        }
    }
}