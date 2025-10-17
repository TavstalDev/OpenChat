package io.github.tavstaldev.openChat.tasks;

import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

/**
 * A task that periodically cleans up the player cache by removing expired entries.
 * This task is executed as a BukkitRunnable.
 */
public class CacheCleanTask extends BukkitRunnable {

    /**
     * Executes the cache cleaning logic.
     * <br/>
     * - Skips execution if there are no players marked for removal.
     * - Iterates through the marked players and removes their cache if:
     *   - The player's chat message delay has expired.
     *   - The player's command delay has expired.
     *   - The player's mention cooldown has expired.
     * - Unmarks players for removal after processing.
     */
    @Override
    public void run() {
        // Check if there are any players marked for removal
        if (PlayerCacheManager.isMarkedForRemovalEmpty())
            return;

        // Iterate through the set of players marked for removal
        for (var playerId : PlayerCacheManager.getMarkedForRemovalSet()) {
            var playerCache = PlayerCacheManager.get(playerId);

            // If the player's cache is null, unmark them for removal and continue
            if (playerCache == null) {
                PlayerCacheManager.unmarkForRemoval(playerId);
                continue;
            }

            var date = LocalDateTime.now();

            // Skip removal if any of the player's delays or cooldowns are still active
            if (playerCache.getChatMessageDelay().isAfter(date))
                continue;

            if (playerCache.getCommandDelay().isAfter(date))
                continue;

            if (playerCache.getMentionCooldown().isAfter(date))
                continue;

            // Remove the player's cache and unmark them for removal
            PlayerCacheManager.remove(playerId);
            PlayerCacheManager.unmarkForRemoval(playerId);
        }
    }
}
