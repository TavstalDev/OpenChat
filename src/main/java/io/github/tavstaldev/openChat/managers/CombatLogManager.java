package io.github.tavstaldev.openChat.managers;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Manages interactions with the CombatLogX plugin to determine if a player is in combat.
 * Implements the ICombatManager interface.
 */
public class CombatLogManager implements ICombatManager {
    // Logger instance for logging messages related to this class.
    private final PluginLogger _logger = OpenChat.logger().withModule(CombatLogManager.class);

    // Reference to the CombatLogX API.
    private ICombatLogX combatLogXAPI;

    // Tracks whether the CombatLogX plugin has been initialized.
    private boolean isInitialized = false;

    /**
     * Checks if a given player is currently in combat.
     *
     * @param player The player to check.
     * @return True if the player is in combat, false otherwise.
     */
    @Override
    public boolean isPlayerInCombat(Player player) {
        try {
            // If the CombatLogX API is not yet initialized, attempt to initialize it.
            if (combatLogXAPI == null) {
                if (!isInitialized) {
                    isInitialized = true;

                    // Attempt to retrieve the CombatLogX plugin from the Bukkit plugin manager.
                    Plugin plugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
                    if (plugin instanceof ICombatLogX) {
                        combatLogXAPI = (ICombatLogX) plugin;
                    } else {
                        // Fallback if CombatLogX is not found or incompatible.
                        return false;
                    }
                } else {
                    // If initialization has already been attempted, return false.
                    return false;
                }
            }

            // Use the CombatLogX API to check if the player is in combat.
            var combatManager = combatLogXAPI.getCombatManager();
            if (combatManager.canBypass(player)) {
                // If the player can bypass combat checks, return false.
                return false;
            }
            return combatManager.isInCombat(player);
        } catch (Exception ex) {
            // Log any errors that occur during the combat check.
            _logger.error("Error checking combat status for player " + player.getName() + ": " + ex.getMessage());
            return false;
        }
    }
}