package io.github.tavstaldev.openChat.managers;

import org.bukkit.entity.Player;

/**
 * A basic implementation of the ICombatManager interface.
 * This class provides a placeholder implementation for checking
 * if a player is in combat.
 */
public class CombatManager implements ICombatManager {
    /**
     * Checks if a given player is currently in combat.
     *
     * @param player The player to check.
     * @return Always returns false as this is a placeholder implementation.
     */
    @Override
    public boolean isPlayerInCombat(Player player) {
        return false;
    }
}