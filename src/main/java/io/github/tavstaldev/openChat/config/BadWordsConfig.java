package io.github.tavstaldev.openChat.config;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.openChat.OpenChat;

import java.util.List;

public class BadWordsConfig extends ConfigurationBase {

    public BadWordsConfig() {
        super(OpenChat.Instance, "badwords.yml", null);
    }

    @Override
    public void loadDefaults() {
        resolveGet("whitelist", new String[]{
                "hello",
                "shuttle"
        });
        resolveComment("whitelist", List.of(
                "List of words that are allowed even if they match the blacklist.",
                "This is useful for words that contain blacklisted substrings but are not offensive."
        ));

        resolveGet("blacklist", new String[]{
                "asshole",
                "bitch",
                "bastard",
                "cunt",
                "damn",
                "dick",
                "fuck",
                "hell",
                "piss",
                "pussy",
                "retard",
                "shit",
                "slut",
                "twat",
                "whore",
                "chink",
                "dyke",
                "faggot",
                "gook",
                "kike",
                "nigger",
                "spic",
                "tranny",
                "wetback",
                "blowjob",
                "cock",
                "ejaculate",
                "jerkoff",
                "masturbate",
                "orgasm",
                "penis",
                "semen",
                "vagina",
        });
        resolveComment("blacklist", List.of(
                "List of blacklisted words that will be filtered from chat.",
                "Add any words you want to block here.",
                "Be cautious when adding words to avoid over-filtering.",
                "Do not forget adjusting the anti-swear characterMapping, it is used to",
                "make the filter more effective against common obfuscation techniques.",
                "So you only need to add the base words here."
        ));
    }
}
