package io.github.tavstaldev.openChat.models;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;

    private byte channel;

    private boolean whisperEnabled;

    private String mentionSound;

    private EMentionDisplay mentionDisplay;

    private EMentionPreference mentionPreference;

    public PlayerData(UUID uuid, byte channel, boolean whisperEnabled, String mentionSound, EMentionDisplay mentionDisplay, EMentionPreference mentionPreference) {
        this.uuid = uuid;
        this.channel = channel;
        this.whisperEnabled = whisperEnabled;
        this.mentionSound = mentionSound;
        this.mentionDisplay = mentionDisplay;
        this.mentionPreference = mentionPreference;
    }

    public UUID getUuid() {
        return uuid;
    }

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public boolean isWhisperEnabled() {
        return whisperEnabled;
    }

    public void setWhisperEnabled(boolean whisperEnabled) {
        this.whisperEnabled = whisperEnabled;
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
