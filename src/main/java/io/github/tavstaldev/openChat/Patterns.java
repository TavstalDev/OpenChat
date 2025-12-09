package io.github.tavstaldev.openChat;

import java.util.regex.Pattern;

public class Patterns {
    public static final Pattern minecraftUsernamePattern = Pattern.compile("([a-zA-Z0-9_]{3,16})(?![a-zA-Z0-9_])");
    public static final Pattern legacyPattern = Pattern.compile("(?i)[&§]([0-9a-fk-or])");
    public static final Pattern hexPattern = Pattern.compile("(?i)[&§]#([A-Fa-f0-9]{6})");
    public static final Pattern emojiPattern = Pattern.compile(":[a-zA-Z0-9_]+:");
}