package io.github.tavstaldev.openChat;

import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.github.tavstaldev.openChat.commands.CommandChat;
import io.github.tavstaldev.openChat.events.*;
import io.github.tavstaldev.openChat.models.AntiAdvertisementSystem;
import io.github.tavstaldev.openChat.models.CommandCheckerSystem;
import io.github.tavstaldev.openChat.models.AntiSwearSystem;
import io.github.tavstaldev.openChat.tasks.CacheCleanTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Main class for the OpenChat plugin.
 * Extends the PluginBase class and provides core functionality such as configuration management,
 * event registration, command handling, and plugin lifecycle management.
 */
public final class OpenChat extends PluginBase {
    public static OpenChat Instance; // Singleton instance of the plugin.
    private AntiAdvertisementSystem advertisementSystem; // System for detecting advertisements in chat.
    private AntiSwearSystem antiSwearSystem; // System for detecting swear words in chat.
    private CommandCheckerSystem commandCheckerSystem; // System for checking commands.
    private OpEventListener opEventListener; // Listener for operator-related events.
    private CacheCleanTask cacheCleanTask; // Task for cleaning player caches.

    /**
     * Retrieves the plugin's custom logger.
     *
     * @return The PluginLogger instance.
     */
    public static PluginLogger Logger() {
        return Instance.getCustomLogger();
    }

    /**
     * Retrieves the plugin's translator for handling localizations.
     *
     * @return The PluginTranslator instance.
     */
    public static PluginTranslator Translator() {
        return Instance.getTranslator();
    }

    /**
     * Retrieves the plugin's configuration file.
     *
     * @return The FileConfiguration instance.
     */
    public static FileConfiguration Config() {
        return Instance.getConfig();
    }

    /**
     * Retrieves the custom OpenChat configuration.
     *
     * @return The OpenChatConfiguration instance.
     */
    public static OpenChatConfiguration OCConfig() {
        return (OpenChatConfiguration) Instance._config;
    }

    /**
     * Retrieves the AntiAdvertisementSystem instance.
     *
     * @return The AntiAdvertisementSystem instance.
     */
    public static AntiAdvertisementSystem AdvertisementSystem() {
        return Instance.advertisementSystem;
    }

    /**
     * Retrieves the AntiSwearSystem instance.
     *
     * @return The AntiSwearSystem instance.
     */
    public static AntiSwearSystem AntiSwearSystem() {
        return Instance.antiSwearSystem;
    }

    public static CommandCheckerSystem CommandCheckerSystem() {
        return Instance.commandCheckerSystem;
    }

    /**
     * Constructor for the OpenChat plugin.
     * Sets the URL for the latest release of the plugin.
     */
    public OpenChat() {
        super("https://github.com/TavstalDev/OpenChat/releases/latest");
    }

    /**
     * Called when the plugin is enabled.
     * Initializes the plugin, loads configurations, registers events and commands, and checks for updates.
     */
    @Override
    public void onEnable() {
        Instance = this;
        _config = new OpenChatConfiguration();
        _translator = new PluginTranslator(this, new String[]{"eng", "hun"});
        _logger.Info(String.format("Loading %s...", getProjectName()));

        // Check for compatibility with the Minecraft version.
        if (VersionUtils.isLegacy()) {
            _logger.Error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Generate the default configuration file.
        saveDefaultConfig();

        // Load localizations.
        if (!_translator.Load()) {
            _logger.Error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register event listeners.
        new PlayerEventListener(this);
        new ChatEventListener(this);
        new ItemEventListener(this);
        new BlockEventListener(this);
        new CommandEventListener(this);
        opEventListener = new OpEventListener(this);
        opEventListener.updateAllowedOperators();

        // Initialize systems for advertisement and swear word detection.
        advertisementSystem = new AntiAdvertisementSystem();
        antiSwearSystem = new AntiSwearSystem();
        commandCheckerSystem = new CommandCheckerSystem();

        // Register commands.
        _logger.Debug("Registering commands...");
        var command = getCommand("openchat");
        if (command != null) {
            command.setExecutor(new CommandChat());
        }

        // Register cache cleanup task.
        if (cacheCleanTask != null && !cacheCleanTask.isCancelled())
            cacheCleanTask.cancel();
        cacheCleanTask = new CacheCleanTask(); // Runs every 5 minutes
        cacheCleanTask.runTaskTimer(this, 0, 5 * 60 * 20);

        _logger.Ok(String.format("%s has been successfully loaded.", getProjectName()));

        // Check for plugin updates if enabled in the configuration.
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

    /**
     * Called when the plugin is disabled.
     * Logs a message indicating the plugin has been unloaded.
     */
    @Override
    public void onDisable() {
        _logger.Info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    /**
     * Reloads the plugin's configuration and systems.
     * Reloads localizations, configuration, and reinitializes the advertisement and swear word systems.
     */
    public void reload() {
        _logger.Info(String.format("Reloading %s...", getProjectName()));
        _logger.Debug("Reloading localizations...");
        _translator.Load();
        _logger.Debug("Localizations reloaded.");
        _logger.Debug("Reloading configuration...");
        this._config.load();
        _logger.Debug("Configuration reloaded.");

        advertisementSystem = new AntiAdvertisementSystem();
        antiSwearSystem = new AntiSwearSystem();
        commandCheckerSystem = new CommandCheckerSystem();
        opEventListener.updateAllowedOperators();
        _logger.Ok(String.format("%s has been successfully reloaded.", getProjectName()));
    }
}
