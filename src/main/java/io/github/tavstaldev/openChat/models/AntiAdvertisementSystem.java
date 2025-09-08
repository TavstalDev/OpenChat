package io.github.tavstaldev.openChat.models;

import io.github.tavstaldev.openChat.OpenChat;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiAdvertisementSystem {
    private final Pattern adPattern;
    private final Set<String> whitelist;

    public AntiAdvertisementSystem() {
        adPattern = Pattern.compile(
                OpenChat.Config().getString("antiAdvertisement.regex", "(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,6}|\\b(?:https?://)?(?:www\\.)?[a-z0-9.-]+\\.[a-z]{2,6}\\b|\\b(?:discord\\.gg|discordapp\\.com/invite)/[a-zA-Z0-9]+\\b"),
                Pattern.CASE_INSENSITIVE
        );
        this.whitelist = new HashSet<>();
        for (String entry : OpenChat.Config().getStringList("antiAdvertisement.whitelist")) {
            this.whitelist.add(entry.toLowerCase());
        }
    }

    public boolean containsAdvertisement(String message) {
        Matcher matcher = adPattern.matcher(message);
        while (matcher.find()) {
            String foundAd = matcher.group().toLowerCase();
            if (!whitelist.contains(foundAd)) {
                return true;
            }
        }
        return false;
    }
}