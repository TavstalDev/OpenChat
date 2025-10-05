package io.github.tavstaldev.openChat.tasks;

import io.github.tavstaldev.openChat.managers.PlayerCacheManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

public class CacheCleanTask extends BukkitRunnable {
    @Override
    public void run() {
        if (PlayerCacheManager.isMarkedForRemovalEmpty())
            return;

        for (var playerId : PlayerCacheManager.getMarkedForRemovalSet()) {
            var playerCache = PlayerCacheManager.get(playerId);
            if (playerCache == null)
            {
                PlayerCacheManager.unmarkForRemoval(playerId);
                continue;
            }

            var date = LocalDateTime.now();
            if (playerCache.getChatMessageDelay().isAfter(date))
                continue;

            if (playerCache.getCommandDelay().isAfter(date))
                continue;

            if (playerCache.getMentionCooldown().isAfter(date))
                continue;

            PlayerCacheManager.remove(playerId);
            PlayerCacheManager.unmarkForRemoval(playerId);
        }
    }
}
