package io.github.tavstaldev.openChat.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.OpenChatConfiguration;
import io.github.tavstaldev.openChat.models.EMentionDisplay;
import io.github.tavstaldev.openChat.models.EMentionPreference;
import io.github.tavstaldev.openChat.models.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SqlLiteDatabase implements IDatabase {
    private final PluginLogger _logger = OpenChat.logger().withModule(SqlLiteDatabase.class);
    private OpenChatConfiguration _config;
    private final Cache<@NotNull UUID, PlayerData> _playerCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();
    private final Cache<@NotNull UUID, Set<UUID>> _ignoredPlayerCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();
    //#region SQL Statements
    private String addPlayerDataSql;
    private String updatePlayerDataSql;
    private String removePlayerDataSql;
    private String getPlayerDataSql;
    private String addIgnoredPlayerSql;
    private String removeIgnoredPlayerSql;
    private String getIgnoredPlayersSql;
    //#endregion


    @Override
    public void load() {
        _config = OpenChat.config();
        update();
    }

    @Override
    public void update() {
        addPlayerDataSql = String.format("INSERT INTO %s_players (PlayerId, Channel, WhisperEnabled, Sound, Display, Preference) " +
                        "VALUES (?, ?, ?, ?, ?, ?);",
                _config.storageTablePrefix);

        removePlayerDataSql = String.format("DELETE FROM %s_players WHERE PlayerId=?;",
                _config.storageTablePrefix);

        updatePlayerDataSql = String.format("UPDATE %s_players SET Channel=?, WhisperEnabled=?, Sound=?, Display=?, Preference=? " +
                        "WHERE PlayerId=?;",
                _config.storageTablePrefix);

        getPlayerDataSql = String.format("SELECT * FROM %s_players WHERE PlayerId=?;",
                _config.storageTablePrefix);


        addIgnoredPlayerSql = String.format("INSERT INTO %s_ignores (PlayerId, IgnoredId) " +
                        "VALUES (?, ?);",
                _config.storageTablePrefix);

        removeIgnoredPlayerSql = String.format("DELETE FROM %s_ignores WHERE PlayerId=? AND IgnoredId=?;",
                _config.storageTablePrefix);

        getIgnoredPlayersSql = String.format("SELECT * FROM %s_ignores WHERE PlayerId=?;",
                _config.storageTablePrefix);
    }

    @Override
    public void unload() { /* ignored */ }

    public Connection CreateConnection() {
        try {
            if (_config == null)
                _config = OpenChat.config();
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(String.format("jdbc:sqlite:plugins/OpenChat/%s.db", _config.storageFilename));
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while creating db connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    @Override
    public void checkSchema() {
        try (Connection connection = CreateConnection()) {
            // Players table
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s_players (" +
                            "PlayerId VARCHAR(36) PRIMARY KEY, " +
                            "Channel TINYINT NOT NULL," +
                            "WhisperEnabled BOOLEAN NOT NULL," +
                            "Sound VARCHAR(200) NOT NULL, " +
                            "Display VARCHAR(32) NOT NULL, " +
                            "Preference VARCHAR(32) NOT NULL);",
                    _config.storageTablePrefix);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();

            sql = String.format("CREATE TABLE IF NOT EXISTS %s_ignores (" +
                            "PlayerId VARCHAR(36) NOT NULL, " +
                            "IgnoredId VARCHAR(36) NOT NULL, " +
                            "PRIMARY KEY (PlayerId, IgnoredId));",
                    _config.storageTablePrefix
            );
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void addPlayerData(UUID playerId) {
        try (Connection connection = CreateConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(addPlayerDataSql)) {
                statement.setString(1, playerId.toString());
                statement.setByte(2, (byte)0);
                statement.setBoolean(3, true);
                statement.setString(4, _config.mentionsDefaultSound);
                statement.setString(5, _config.mentionsDefaultDisplay);
                statement.setString(6, _config.mentionsDefaultPreference);
                statement.executeUpdate();
            }

            _playerCache.put(playerId, new PlayerData(playerId, (byte)0, true,
                    _config.mentionsDefaultSound,
                    EMentionDisplay.valueOf(_config.mentionsDefaultDisplay),
                    EMentionPreference.valueOf(_config.mentionsDefaultPreference)));
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while adding player data...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void updatePlayerData(PlayerData newData) {
        try (Connection connection = CreateConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(updatePlayerDataSql)) {
                statement.setByte(1, newData.getChannel());
                statement.setBoolean(2, newData.isWhisperEnabled());
                statement.setString(3, newData.getMentionSound());
                statement.setString(4, newData.getMentionDisplay().name());
                statement.setString(5, newData.getMentionPreference().name());
                statement.setString(6, newData.getUuid().toString());
                statement.executeUpdate();
            }

            _playerCache.put(newData.getUuid(), newData);
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void removePlayerData(UUID playerId) {
        try (Connection connection = CreateConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(removePlayerDataSql)) {
                statement.setString(1, playerId.toString());
                statement.executeUpdate();
            }

            _playerCache.invalidate(playerId);
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while removing player data...\n%s", ex.getMessage()));
        }
    }

    @Override
    public Optional<PlayerData> getPlayerData(UUID playerId) {
        var data = _playerCache.getIfPresent(playerId);
        if (data != null) {
            return Optional.of(data);
        }

        try (Connection connection = CreateConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(getPlayerDataSql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = new PlayerData(
                                UUID.fromString(result.getString("PlayerId")),
                                result.getByte("Channel"),
                                result.getBoolean("WhisperEnabled"),
                                result.getString("Sound"),
                                EMentionDisplay.valueOf(result.getString("Display")),
                                EMentionPreference.valueOf(result.getString("Preference"))
                        );
                    }
                }
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while finding player data...\n%s", ex.getMessage()));
            return Optional.empty();
        }

        if (data != null) {
            _playerCache.put(playerId, data);
        }
        return Optional.ofNullable(data);
    }

    @Override
    public void addIgnoredPlayer(UUID playerId, UUID ignoredPlayerId) {
        try (Connection connection = CreateConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(addIgnoredPlayerSql)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, ignoredPlayerId.toString());
                statement.executeUpdate();
            }

            Set<UUID> ignoredSet = _ignoredPlayerCache.getIfPresent(playerId);
            if (ignoredSet != null) {
                ignoredSet.add(ignoredPlayerId);
            } else {
                _ignoredPlayerCache.put(playerId, Set.of(ignoredPlayerId));
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while adding ignore data...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void removeIgnoredPlayer(UUID playerId, UUID ignoredPlayerId) {
        try (Connection connection = CreateConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(removeIgnoredPlayerSql)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, ignoredPlayerId.toString());
                statement.executeUpdate();
            }

            Set<UUID> ignoredSet = _ignoredPlayerCache.getIfPresent(playerId);
            if (ignoredSet != null) {
                ignoredSet.remove(ignoredPlayerId);
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened during the deletion of ignore tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public boolean isPlayerIgnored(UUID playerId, UUID ignoredPlayerId) {
        var data = _ignoredPlayerCache.getIfPresent(playerId);
        if (data != null) {
            return data.contains(ignoredPlayerId);
        }

        data = new HashSet<>();
        try (Connection connection = CreateConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(getIgnoredPlayersSql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data.add(UUID.fromString(result.getString("IgnoredId")));
                    }
                }
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while finding ignore data...\n%s", ex.getMessage()));
            return false;
        }

        _ignoredPlayerCache.put(playerId, data);
        return data.contains(ignoredPlayerId);
    }
}