package io.github.tavstaldev.openChat.models;

/**
 * Enum representing the user's preference for receiving mention notifications.
 * These preferences determine when notifications are displayed.
 */
public enum EMentionPreference {
    /** Always receive mention notifications. */
    ALWAYS,

    /** Do not receive mention notifications while in combat. */
    NEVER_IN_COMBAT,

    /** Receive notifications silently while in combat. */
    SILENT_IN_COMBAT,

    /** Never receive mention notifications. */
    NEVER
}