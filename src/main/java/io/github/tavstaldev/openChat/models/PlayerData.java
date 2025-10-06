package io.github.tavstaldev.openChat.models;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;

    private boolean publicChatDisabled;

    private boolean whisperEnabled;

    private boolean socialSpyEnabled;

    private String mentionSound;

    private EMentionDisplay mentionDisplay;

    private EMentionPreference mentionPreference;

    public PlayerData(UUID uuid, boolean publicChatDisabled, boolean whisperEnabled, boolean socialSpyEnabled, String mentionSound, EMentionDisplay mentionDisplay, EMentionPreference mentionPreference) {
        this.uuid = uuid;
        this.publicChatDisabled = publicChatDisabled;
        this.whisperEnabled = whisperEnabled;
        this.socialSpyEnabled = socialSpyEnabled;
        this.mentionSound = mentionSound;
        this.mentionDisplay = mentionDisplay;
        this.mentionPreference = mentionPreference;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isPublicChatDisabled() {
        return publicChatDisabled;
    }

    public void setPublicChatDisabled(boolean publicChatDisabled) {
        this.publicChatDisabled = publicChatDisabled;
    }

    public boolean isWhisperEnabled() {
        return whisperEnabled;
    }

    public void setWhisperEnabled(boolean whisperEnabled) {
        this.whisperEnabled = whisperEnabled;
    }

    public boolean isSocialSpyEnabled() {
        return socialSpyEnabled;
    }

    public void setSocialSpyEnabled(boolean socialSpyEnabled) {
        this.socialSpyEnabled = socialSpyEnabled;
    }

    public String getMentionSound() {
        return mentionSound;
    }

    public void setMentionSound(String mentionSound) {
        this.mentionSound = mentionSound;
    }

    public EMentionDisplay getMentionDisplay() {
        return mentionDisplay;
    }

    public void setMentionDisplay(EMentionDisplay mentionDisplay) {
        this.mentionDisplay = mentionDisplay;
    }

    public EMentionPreference getMentionPreference() {
        return mentionPreference;
    }

    public void setMentionPreference(EMentionPreference mentionPreference) {
        this.mentionPreference = mentionPreference;
    }
}
