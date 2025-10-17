package io.github.tavstaldev.openChat.models;

import java.util.UUID;

/**
 * Represents the data associated with a player in the OpenChat system.
 */
public class PlayerData {
    private final UUID uuid; // Unique identifier for the player.

    private boolean publicChatDisabled; // Indicates if the player has disabled public chat.

    private boolean whisperEnabled; // Indicates if the player has enabled whisper functionality.

    private boolean socialSpyEnabled; // Indicates if the player has enabled social spy functionality.

    private String mentionSound; // The sound to play when the player is mentioned.

    private EMentionDisplay mentionDisplay; // The display style for mentions.

    private EMentionPreference mentionPreference; // The player's preference for handling mentions.

    /**
     * Constructs a new PlayerData instance.
     *
     * @param uuid               The unique identifier for the player.
     * @param publicChatDisabled Whether public chat is disabled for the player.
     * @param whisperEnabled     Whether whisper functionality is enabled for the player.
     * @param socialSpyEnabled   Whether social spy functionality is enabled for the player.
     * @param mentionSound       The sound to play when the player is mentioned.
     * @param mentionDisplay     The display style for mentions.
     * @param mentionPreference  The player's preference for handling mentions.
     */
    public PlayerData(UUID uuid, boolean publicChatDisabled, boolean whisperEnabled, boolean socialSpyEnabled, String mentionSound, EMentionDisplay mentionDisplay, EMentionPreference mentionPreference) {
        this.uuid = uuid;
        this.publicChatDisabled = publicChatDisabled;
        this.whisperEnabled = whisperEnabled;
        this.socialSpyEnabled = socialSpyEnabled;
        this.mentionSound = mentionSound;
        this.mentionDisplay = mentionDisplay;
        this.mentionPreference = mentionPreference;
    }

    /**
     * Gets the unique identifier for the player.
     *
     * @return The player's UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Checks if public chat is disabled for the player.
     *
     * @return True if public chat is disabled, false otherwise.
     */
    public boolean isPublicChatDisabled() {
        return publicChatDisabled;
    }

    /**
     * Sets whether public chat is disabled for the player.
     *
     * @param publicChatDisabled True to disable public chat, false to enable it.
     */
    public void setPublicChatDisabled(boolean publicChatDisabled) {
        this.publicChatDisabled = publicChatDisabled;
    }

    /**
     * Checks if whisper functionality is enabled for the player.
     *
     * @return True if whisper is enabled, false otherwise.
     */
    public boolean isWhisperEnabled() {
        return whisperEnabled;
    }

    /**
     * Sets whether whisper functionality is enabled for the player.
     *
     * @param whisperEnabled True to enable whisper, false to disable it.
     */
    public void setWhisperEnabled(boolean whisperEnabled) {
        this.whisperEnabled = whisperEnabled;
    }

    /**
     * Checks if social spy functionality is enabled for the player.
     *
     * @return True if social spy is enabled, false otherwise.
     */
    public boolean isSocialSpyEnabled() {
        return socialSpyEnabled;
    }

    /**
     * Sets whether social spy functionality is enabled for the player.
     *
     * @param socialSpyEnabled True to enable social spy, false to disable it.
     */
    public void setSocialSpyEnabled(boolean socialSpyEnabled) {
        this.socialSpyEnabled = socialSpyEnabled;
    }

    /**
     * Gets the sound to play when the player is mentioned.
     *
     * @return The mention sound.
     */
    public String getMentionSound() {
        return mentionSound;
    }

    /**
     * Sets the sound to play when the player is mentioned.
     *
     * @param mentionSound The mention sound.
     */
    public void setMentionSound(String mentionSound) {
        this.mentionSound = mentionSound;
    }

    /**
     * Gets the display style for mentions.
     *
     * @return The mention display style.
     */
    public EMentionDisplay getMentionDisplay() {
        return mentionDisplay;
    }

    /**
     * Sets the display style for mentions.
     *
     * @param mentionDisplay The mention display style.
     */
    public void setMentionDisplay(EMentionDisplay mentionDisplay) {
        this.mentionDisplay = mentionDisplay;
    }

    /**
     * Gets the player's preference for handling mentions.
     *
     * @return The mention preference.
     */
    public EMentionPreference getMentionPreference() {
        return mentionPreference;
    }

    /**
     * Sets the player's preference for handling mentions.
     *
     * @param mentionPreference The mention preference.
     */
    public void setMentionPreference(EMentionPreference mentionPreference) {
        this.mentionPreference = mentionPreference;
    }
}
