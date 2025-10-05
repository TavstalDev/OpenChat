package io.github.tavstaldev.openChat.database;

import io.github.tavstaldev.openChat.models.PlayerData;

import java.util.Optional;
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


    void addIgnoredPlayer(UUID playerId, UUID ignoredPlayerId);

    void removeIgnoredPlayer(UUID playerId, UUID ignoredPlayerId);

    boolean isPlayerIgnored(UUID playerId, UUID ignoredPlayerId);
}
