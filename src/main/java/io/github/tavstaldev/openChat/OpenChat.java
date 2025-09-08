package io.github.tavstaldev.openChat;

import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.github.tavstaldev.openChat.events.ChatEventListener;
import io.github.tavstaldev.openChat.events.PlayerEventListener;
import io.github.tavstaldev.openChat.models.AntiAdvertisementSystem;
import io.github.tavstaldev.openChat.models.AntiSwearSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public final class OpenChat extends PluginBase {
    public static OpenChat Instance;
    private AntiAdvertisementSystem advertisementSystem;
    private AntiSwearSystem antiSwearSystem;

    public static PluginLogger Logger() {
        return Instance.getCustomLogger();
    }

    public static PluginTranslator Translator() {
        return Instance.getTranslator();
    }

    public static FileConfiguration Config() {
        return Instance.getConfig();
    }
    public static AntiAdvertisementSystem AdvertisementSystem() {
        return Instance.advertisementSystem;
    }
    public static AntiSwearSystem AntiSwearSystem() {
        return Instance.antiSwearSystem;
    }

    public OpenChat() {
        super("https://github.com/TavstalDev/OpenChat/releases/latest");
    }

    @Override
    public void onEnable() {
        Instance = this;
        _config = new OpenChatConfiguration();
        _translator = new PluginTranslator(this, new String[]{"eng", "hun"});
        _logger.Info(String.format("Loading %s...", getProjectName()));

        if (VersionUtils.isLegacy()) {
            _logger.Error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Generate config file
        saveDefaultConfig();

        // Load Localizations
        if (!_translator.Load()) {
            _logger.Error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Events
        new PlayerEventListener(this);
        new ChatEventListener(this);

        advertisementSystem = new AntiAdvertisementSystem();
        antiSwearSystem = new AntiSwearSystem();

        _logger.Ok(String.format("%s has been successfully loaded.", getProjectName()));
        if (getConfig().getBoolean("checkForUpdates", true)) {
            isUpToDate().thenAccept(upToDate -> {
                if (upToDate) {
                    _logger.Ok("Plugin is up to date!");
                } else {
                    _logger.Warn("A new version of the plugin is available: " + getDownloadUrl());
                }
            }).exceptionally(e -> {
                _logger.Error("Failed to determine update status: " + e.getMessage());
                return null;
            });
        }
    }

    @Override
    public void onDisable() {
        _logger.Info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    public void reload() {
        _logger.Info(String.format("Reloading %s...", getProjectName()));
        _logger.Debug("Reloading localizations...");
        _translator.Load();
        _logger.Debug("Localizations reloaded.");
        _logger.Debug("Reloading configuration...");
        this.reloadConfig();
        _logger.Debug("Configuration reloaded.");
    }
}