package io.github.tavstaldev.openChat;

import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.github.tavstaldev.openChat.commands.*;
import io.github.tavstaldev.openChat.database.IDatabase;
import io.github.tavstaldev.openChat.database.MySqlDatabase;
import io.github.tavstaldev.openChat.database.SqlLiteDatabase;
import io.github.tavstaldev.openChat.events.*;
import io.github.tavstaldev.openChat.managers.CombatLogManager;
import io.github.tavstaldev.openChat.managers.CombatManager;
import io.github.tavstaldev.openChat.managers.ICombatManager;
import io.github.tavstaldev.openChat.models.AntiAdvertisementSystem;
import io.github.tavstaldev.openChat.models.AntiSwearSystem;
import io.github.tavstaldev.openChat.models.CommandCheckerSystem;
import io.github.tavstaldev.openChat.tasks.CacheCleanTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Main class for the OpenChat plugin.
 * Extends the PluginBase class and provides core functionality such as configuration management,
 * event registration, command handling, and plugin lifecycle management.
 */
public final class OpenChat extends PluginBase {
    public static OpenChat Instance; // Singleton instance of the plugin.
    private IDatabase database; // Database manager for handling player data storage.
    private ICombatManager CombatManager; // Combat manager for handling combat-related features.
    private IPlaceholderManager placeholderManager; // Placeholder manager
    private AntiAdvertisementSystem advertisementSystem; // System for detecting advertisements in chat.
    private AntiSwearSystem antiSwearSystem; // System for detecting swear words in chat.
    private CommandCheckerSystem commandCheckerSystem; // System for checking commands.
    private OpEventListener opEventListener; // Listener for operator-related events.
    private CacheCleanTask cacheCleanTask; // Task for cleaning player caches.

    public static IDatabase database() {
        return Instance.database;
    }

    public static IPlaceholderManager placeholderManager() {
        return Instance.placeholderManager;
    }

    public static ICombatManager combatManager() {
        return Instance.CombatManager;
    }

    /**
     * Retrieves the plugin's custom logger.
     *
     * @return The PluginLogger instance.
     */
    public static PluginLogger logger() {
        return Instance.getCustomLogger();
    }

    /**
     * Retrieves the plugin's translator for handling localizations.
     *
     * @return The PluginTranslator instance.
     */
    public static PluginTranslator translator() {
        return Instance.getTranslator();
    }

    /**
     * Retrieves the custom OpenChat configuration.
     *
     * @return The OpenChatConfiguration instance.
     */
    public static OpenChatConfiguration config() {
        return (OpenChatConfiguration) Instance._config;
    }

    /**
     * Retrieves the AntiAdvertisementSystem instance.
     *
     * @return The AntiAdvertisementSystem instance.
     */
    public static AntiAdvertisementSystem advertisementSystem() {
        return Instance.advertisementSystem;
    }

    /**
     * Retrieves the AntiSwearSystem instance.
     *
     * @return The AntiSwearSystem instance.
     */
    public static AntiSwearSystem antiSwearSystem() {
        return Instance.antiSwearSystem;
    }

    public static CommandCheckerSystem commandCheckerSystem() {
        return Instance.commandCheckerSystem;
    }

    /**
     * Constructor for the OpenChat plugin.
     * Sets the URL for the latest release of the plugin.
     */
    public OpenChat() {
        super(false, "https://github.com/TavstalDev/OpenChat/releases/latest");
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
        _logger.info(String.format("Loading %s...", getProjectName()));

        // Check for compatibility with the Minecraft version.
        if (VersionUtils.isLegacy()) {
            _logger.error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Hook into PlaceholderAPI
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            _logger.error("PlaceholderAPI is not installed... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            _logger.ok("Found PlaceholderAPI and hooked into it...");
        }

        // Initialize Combat Manager
        Plugin combatLogPlugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
        if (combatLogPlugin != null && combatLogPlugin.isEnabled()) {
            CombatManager = new CombatLogManager();
            getLogger().info("Successfully hooked into CombatLogX!");
        } else {
            CombatManager = new CombatManager();
            _logger.warn("CombatLogX plugin not found or not enabled. Combat management features will be disabled.");
        }

        // Generate the default configuration file.
        saveDefaultConfig();

        // Load localizations.
        if (!_translator.load()) {
            _logger.error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Create Database
        String databaseType = config().storageType;
        if (databaseType == null)
            databaseType = "sqlite";
        switch (databaseType.toLowerCase()) {
            case "mysql":
            case "mariadb": {
                database = new MySqlDatabase();
                break;
            }
            case "sqlite":
            default: {
                database = new SqlLiteDatabase();
                break;
            }
        }
        database.load();
        database.checkSchema();

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
        _logger.debug("Registering commands...");
        new CommandChat();
        new CommandIgnore();
        new CommandUnignore();
        new CommandMentions();
        new CommandChatToggle();
        new CommandSocialSpy(); // social spy is used outside private messaging as well
        if (config().customGreetingEnabled) {
            new CommandCustomGreeting();
        }
        if (config().privateMessagingEnabled) {
            new CommandReply();
            new CommandWhisper();
            new CommandWhisperToggle();
        }

        // Register cache cleanup task.
        if (cacheCleanTask != null && !cacheCleanTask.isCancelled())
            cacheCleanTask.cancel();
        cacheCleanTask = new CacheCleanTask(); // Runs every 5 minutes
        cacheCleanTask.runTaskTimerAsynchronously(this, 0, 5 * 60 * 20);

        _logger.ok(String.format("%s has been successfully loaded.", getProjectName()));

        // Check for plugin updates if enabled in the configuration.
        if (getConfig().getBoolean("checkForUpdates", true)) {
            isUpToDate().thenAccept(upToDate -> {
                if (upToDate) {
                    _logger.ok("Plugin is up to date!");
                } else {
                    _logger.warn("A new version of the plugin is available: " + getDownloadUrl());
                }
            }).exceptionally(e -> {
                _logger.error("Failed to determine update status: " + e.getMessage());
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
        if (cacheCleanTask != null && !cacheCleanTask.isCancelled())
            cacheCleanTask.cancel();
        _logger.info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    /**
     * Reloads the plugin's configuration and systems.
     * Reloads localizations, configuration, and reinitializes the advertisement and swear word systems.
     */
    public void reload() {
        _logger.info(String.format("Reloading %s...", getProjectName()));
        _logger.debug("Reloading localizations...");
        _translator.load();
        _logger.debug("Localizations reloaded.");
        _logger.debug("Reloading configuration...");
        this._config.load();
        _logger.debug("Configuration reloaded.");

        // Reinitialize systems
        advertisementSystem = new AntiAdvertisementSystem();
        antiSwearSystem = new AntiSwearSystem();
        commandCheckerSystem = new CommandCheckerSystem();
        opEventListener.updateAllowedOperators();

        // Restart cache cleanup task
        if (cacheCleanTask != null && !cacheCleanTask.isCancelled())
            cacheCleanTask.cancel();
        cacheCleanTask = new CacheCleanTask(); // Runs every 5 minutes
        cacheCleanTask.runTaskTimerAsynchronously(this, 0, 5 * 60 * 20);

        // Update database
        database.update();

        _logger.ok(String.format("%s has been successfully reloaded.", getProjectName()));
    }
}
