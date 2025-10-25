package io.github.tavstaldev.openChat.models.database;

/**
 * Enum representing different mention options for notifications.
 * These options determine how notifications are displayed to the user.
 */
public enum EMentionDisplay {
    /** Notification is displayed only in the chat. */
    ONLY_CHAT,

    /** Notification is displayed only in the action bar. */
    ONLY_ACTIONBAR,

    /** Notification is played only as a sound. */
    ONLY_SOUND,

    /** Notification is displayed in both the chat and the action bar. */
    CHAT_AND_ACTIONBAR,

    /** Notification is displayed in the chat and played as a sound. */
    CHAT_AND_SOUND,

    /** Notification is displayed in the action bar and played as a sound. */
    ACTIONBAR_AND_SOUND,

    /** Notification is displayed in the chat, action bar, and played as a sound. */
    ALL
}