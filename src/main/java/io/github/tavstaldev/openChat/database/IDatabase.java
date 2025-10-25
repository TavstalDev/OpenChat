package io.github.tavstaldev.openChat.database;

import io.github.tavstaldev.openChat.models.database.EViolationType;
import io.github.tavstaldev.openChat.models.database.PlayerData;
import io.github.tavstaldev.openChat.models.database.ViolationData;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IDatabase {
    void load();

    void update();

    void unload();

    void checkSchema();


    void addPlayerData(UUID playerId);

    void updatePlayerData(PlayerData newData);

    void removePlayerData(UUID playerId);

    Optional<PlayerData> getPlayerData(UUID playerId);

    boolean isPublicChatDisabled(UUID playerId);

    boolean isSocialSpyEnabled(Player player);


    void addIgnoredPlayer(UUID playerId, UUID ignoredPlayerId);

    void removeIgnoredPlayer(UUID playerId, UUID ignoredPlayerId);

    boolean isPlayerIgnored(UUID playerId, UUID ignoredPlayerId);

    void addViolation(UUID playerId, EViolationType type, String details);
    void removeViolation(UUID violationId, UUID playerId);
    Optional<Set<ViolationData>> getViolations(UUID playerId);
    Optional<Set<ViolationData>> getActiveViolations(UUID playerId);
    Optional<Set<ViolationData>> getActiveViolationsByType(UUID playerId, EViolationType type);
}
