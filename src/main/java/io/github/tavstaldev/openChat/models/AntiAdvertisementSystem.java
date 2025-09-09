package io.github.tavstaldev.openChat.models;

import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.OpenChatConfiguration;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiAdvertisementSystem {
    private final Pattern adPattern;
    private final Pattern whitelistPattern;

    public AntiAdvertisementSystem() {
        OpenChatConfiguration _config = (OpenChatConfiguration) OpenChat.Config();
        adPattern = Pattern.compile(
                _config.antiAdvertisementRegex,
                Pattern.CASE_INSENSITIVE
        );

        String combinedWhitelistRegex = "(" + String.join("|", _config.antiAdvertisementWhitelist) + ")";
        whitelistPattern = Pattern.compile(combinedWhitelistRegex, Pattern.CASE_INSENSITIVE);
    }

    public boolean containsAdvertisement(String message) {
        String sanitizedMessage = whitelistPattern.matcher(message).replaceAll("");
        return adPattern.matcher(sanitizedMessage).matches();
    }
}