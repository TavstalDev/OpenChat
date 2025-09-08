package io.github.tavstaldev.openChat.models;

import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class PlayerCache {
    private final Player _player;
    private String lastChatMessage = "";
    private int chatSpamCount = 0;
    public LocalDateTime chatMessageDelay;
    private String lastCommand = "";
    private int commandSpamCount = 0;
    public LocalDateTime commandDelay;

    public PlayerCache(Player player) {
        this._player = player;
        // Make sure the player can chat/command immediately after joining
        chatMessageDelay = LocalDateTime.now().minusHours(1);
        commandDelay = LocalDateTime.now().minusHours(1);
    }

    public String getLastChatMessage() {
        return lastChatMessage;
    }

    public int getChatSpamCount() {
        return chatSpamCount;
    }

    public void setLastChatMessage(String message) {
        if (message.equalsIgnoreCase(lastChatMessage)) {
            chatSpamCount++;
        } else {
            this.lastChatMessage = message;
            chatSpamCount = 0;
        }
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public int getCommandSpamCount() {
        return commandSpamCount;
    }

    public void setLastCommand(String command) {
        if ( command.equalsIgnoreCase(lastCommand)) {
           commandSpamCount++;
        } else {
            this.lastCommand = command;
            commandSpamCount = 0;
        }
    }
}
