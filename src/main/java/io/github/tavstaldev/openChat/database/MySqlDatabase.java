package io.github.tavstaldev.openChat.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openChat.OpenChat;
import io.github.tavstaldev.openChat.config.GeneralConfig;
import io.github.tavstaldev.openChat.config.StorageConfig;
import io.github.tavstaldev.openChat.models.database.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MySqlDatabase implements IDatabase {
    private final PluginLogger _logger = OpenChat.logger().withModule(MySqlDatabase.class);
    private HikariDataSource _dataSource;
    private GeneralConfig generalConfig;
    private StorageConfig storageConfig;
    private final Cache<@NotNull UUID, PlayerData> _playerCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .build();
    private final Cache<@NotNull UUID, Set<UUID>> _ignoredPlayerCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();

    private final Cache<@NotNull UUID, Set<ViolationData>> _violationCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .build();
    private final Cache<@NotNull UUID, Set<ViolationData>> _violationActiveCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .build();
    //#region SQL Statements
    private String addPlayerDataSql;
    private String updatePlayerDataSql;
    private String removePlayerDataSql;
    private String getPlayerDataSql;
    // Ignored players
    private String addIgnoredPlayerSql;
    private String removeIgnoredPlayerSql;
    private String getIgnoredPlayersSql;
    // Violations
    private String addViolationSql;
    private String removeViolationSql;
    private String getViolationsSql;
    private String getActiveViolationsSql;
    //#endregion


    @Override
    public void load() {
        generalConfig = OpenChat.config();
        storageConfig = OpenChat.storageConfig();
        _dataSource = createDataSource();
        update();
    }

    @Override
    public void update() {
        addPlayerDataSql = String.format("INSERT INTO %s_players (PlayerId, PublicChatDisabled, WhisperEnabled, SocialSpyEnabled, AntiAdLogsEnabled, AntiSpamLogsEnabled, AntiSwearLogsEnabled, MessageColor, Sound, Display, Preference, CustomJoinMessage, CustomQuitMessage) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                storageConfig.tablePrefix);

        removePlayerDataSql = String.format("DELETE FROM %s_players WHERE PlayerId=? LIMIT 1;",
                storageConfig.tablePrefix);

        updatePlayerDataSql = String.format("UPDATE %s_players SET PublicChatDisabled=?, WhisperEnabled=?, SocialSpyEnabled=?," +
                        "AntiAdLogsEnabled=?, AntiSpamLogsEnabled=?, AntiSwearLogsEnabled=?, MessageColor=?," +
                        " Sound=?, Display=?, Preference=?, " +
                        "CustomJoinMessage=?, CustomQuitMessage=? " +
                        "WHERE PlayerId=? LIMIT 1;",
                storageConfig.tablePrefix);

        getPlayerDataSql = String.format("SELECT * FROM %s_players WHERE PlayerId=?;",
                storageConfig.tablePrefix);


        addIgnoredPlayerSql = String.format("INSERT INTO %s_ignores (PlayerId, IgnoredId) " +
                        "VALUES (?, ?);",
                storageConfig.tablePrefix);

        removeIgnoredPlayerSql = String.format("DELETE FROM %s_ignores WHERE PlayerId=? AND IgnoredId=? LIMIT 1;",
                storageConfig.tablePrefix);

        getIgnoredPlayersSql = String.format("SELECT * FROM %s_ignores WHERE PlayerId=?;",
                storageConfig.tablePrefix);

        addViolationSql = String.format("INSERT INTO %s_violations (Id, PlayerId, Type, Details, Timestamp) " +
                        "VALUES (?, ?, ?, ?, ?);",
                storageConfig.tablePrefix);

        removeViolationSql = String.format("DELETE FROM %s_violations WHERE Id=? LIMIT 1;",
                storageConfig.tablePrefix);

        getViolationsSql = String.format("SELECT * FROM %s_violations WHERE PlayerId=?;",
                storageConfig.tablePrefix);

        // second ? is current time in millis, minus the duration threshold until it is considered active
        getActiveViolationsSql = String.format("SELECT * FROM %s_violations WHERE PlayerId=? AND (?-Timestamp)<?;",
                storageConfig.tablePrefix);
    }

    @Override
    public void unload() {
        if (_dataSource != null) {
            if (!_dataSource.isClosed())
                _dataSource.close();
        }
    }

    private HikariDataSource createDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s",
                    storageConfig.host,
                    storageConfig.port,
                    storageConfig.database));
            config.setUsername(storageConfig.username);
            config.setPassword(storageConfig.password);
            config.setMaximumPoolSize(10); // Pool size defaults to 10
            config.setMaxLifetime(30000);
            return new HikariDataSource(config);
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened during the creation of database connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    @Override
    public void checkSchema() {
        try (Connection connection = _dataSource.getConnection()) {
            // Players table
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s_players (" +
                            "PlayerId VARCHAR(36) PRIMARY KEY, " +
                            "PublicChatDisabled BOOLEAN NOT NULL," +
                            "WhisperEnabled BOOLEAN NOT NULL," +
                            "SocialSpyEnabled BOOLEAN NOT NULL," +
                            "AntiAdLogsEnabled BOOLEAN NOT NULL," +
                            "AntiSpamLogsEnabled BOOLEAN NOT NULL," +
                            "AntiSwearLogsEnabled BOOLEAN NOT NULL," +
                            "MessageColor VARCHAR(7)," +
                            "Sound VARCHAR(200) NOT NULL, " +
                            "Display VARCHAR(32) NOT NULL, " +
                            "Preference VARCHAR(32) NOT NULL, " +
                            "CustomJoinMessage VARCHAR(128), " +
                            "CustomQuitMessage VARCHAR(128));",
                    storageConfig.tablePrefix);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();

            // Ignores table
            sql = String.format("CREATE TABLE IF NOT EXISTS %s_ignores (" +
                            "PlayerId VARCHAR(36) NOT NULL, " +
                            "IgnoredId VARCHAR(36) NOT NULL, " +
                            "PRIMARY KEY (PlayerId, IgnoredId));",
                    storageConfig.tablePrefix
            );
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();

            // Violations table
            sql = String.format("CREATE TABLE IF NOT EXISTS %s_violations (" +
                            "Id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "PlayerId VARCHAR(36) NOT NULL, " +
                            "Type VARCHAR(32) NOT NULL, " +
                            "Details VARCHAR(255) NOT NULL, " +
                            "Timestamp BIGINT NOT NULL);",
                    storageConfig.tablePrefix
            );
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    //#region Player Data
    @Override
    public void addPlayerData(UUID playerId) {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(addPlayerDataSql)) {
                statement.setString(1, playerId.toString());
                statement.setBoolean(2, false);
                statement.setBoolean(3, true);
                statement.setBoolean(4, false);
                statement.setBoolean(5, false);
                statement.setBoolean(6, false);
                statement.setBoolean(7, false);
                statement.setString(8, null);
                statement.setString(9, generalConfig.mentionsDefaultSound);
                statement.setString(10, generalConfig.mentionsDefaultDisplay);
                statement.setString(11, generalConfig.mentionsDefaultPreference);
                statement.setString(12, null);
                statement.setString(13, null);
                statement.executeUpdate();
            }

            _playerCache.put(playerId, new PlayerData(playerId, false, true, false,
                    false, false, false, null,
                    generalConfig.mentionsDefaultSound,
                    EMentionDisplay.valueOf(generalConfig.mentionsDefaultDisplay),
                    EMentionPreference.valueOf(generalConfig.mentionsDefaultPreference),
                    null, null));
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while adding player data...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void updatePlayerData(PlayerData newData) {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(updatePlayerDataSql)) {
                statement.setBoolean(1, newData.isPublicChatDisabled());
                statement.setBoolean(2, newData.isWhisperEnabled());
                statement.setBoolean(3, newData.isSocialSpyEnabled());
                statement.setBoolean(4, newData.isAntiAdLogsEnabled());
                statement.setBoolean(5, newData.isAntiSpamLogsEnabled());
                statement.setBoolean(6, newData.isAntiSwearLogsEnabled());
                statement.setString(7, newData.getMessageColor());
                statement.setString(8, newData.getMentionSound());
                statement.setString(9, newData.getMentionDisplay().name());
                statement.setString(10, newData.getMentionPreference().name());
                statement.setString(11, newData.getCustomJoinMessage());
                statement.setString(12,  newData.getCustomLeaveMessage());
                statement.setString(13, newData.getUuid().toString());
                statement.executeUpdate();
            }

            _playerCache.put(newData.getUuid(), newData);
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void removePlayerData(UUID playerId) {
        try (Connection connection = _dataSource.getConnection()) {
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

        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(getPlayerDataSql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = new PlayerData(
                                UUID.fromString(result.getString("PlayerId")),
                                result.getBoolean("PublicChatDisabled"),
                                result.getBoolean("WhisperEnabled"),
                                result.getBoolean("SocialSpyEnabled"),
                                result.getBoolean("AntiAdLogsEnabled"),
                                result.getBoolean("AntiSpamLogsEnabled"),
                                result.getBoolean("AntiSwearLogsEnabled"),
                                result.getString("MessageColor"),
                                result.getString("Sound"),
                                EMentionDisplay.valueOf(result.getString("Display")),
                                EMentionPreference.valueOf(result.getString("Preference")),
                                result.getString("CustomJoinMessage"),
                                result.getString("CustomQuitMessage")
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
    public boolean isPublicChatDisabled(UUID playerId) {
        var data = _playerCache.getIfPresent(playerId);
        if (data != null) {
            return data.isPublicChatDisabled();
        }
        return getPlayerData(playerId).map(PlayerData::isPublicChatDisabled).orElse(false);
    }

    @Override
    public boolean isSocialSpyEnabled(Player player) {
        var playerId = player.getUniqueId();
        var data = _playerCache.getIfPresent(playerId);
        if (data != null) {
            return data.isSocialSpyEnabled() && player.hasPermission("openchat.socialspy");
        }
        return getPlayerData(playerId).map(PlayerData::isSocialSpyEnabled).orElse(false) && player.hasPermission("openchat.socialspy");
    }
    //#endregion

    //#region Ignored Players
    @Override
    public void addIgnoredPlayer(UUID playerId, UUID ignoredPlayerId) {
        try (Connection connection = _dataSource.getConnection()) {
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
        try (Connection connection = _dataSource.getConnection()) {
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
        try (Connection connection = _dataSource.getConnection()) {
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
    //#endregion

    //#region Violations

    @Override
    public void addViolation(@NotNull UUID playerId, @NotNull EViolationType type, @NotNull String details) {
        try (Connection connection = _dataSource.getConnection()) {
            UUID violationId = UUID.randomUUID();
            long timestamp = System.currentTimeMillis();
            try (PreparedStatement statement = connection.prepareStatement(addViolationSql)) {
                statement.setString(1, violationId.toString());
                statement.setString(2, playerId.toString());
                statement.setString(3, type.name());
                statement.setString(4, details);
                statement.setLong(5, timestamp);
                statement.executeUpdate();
            }

            var newViolation = new ViolationData(violationId, playerId, type, details, timestamp);

            // Add to whole cache
            var violationSet = _violationCache.getIfPresent(playerId);
            if (violationSet != null) {
                violationSet.add(newViolation);
            }
            else {
                _violationCache.put(playerId, Set.of(newViolation));
            }

            // Add to active cache
            var activeViolationSet = _violationActiveCache.getIfPresent(playerId);
            if (activeViolationSet != null) {
                activeViolationSet.add(newViolation);
            }
            else {
                _violationActiveCache.put(playerId, Set.of(newViolation));
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while adding violation...\n%s", ex));
        }
    }

    @Override
    public void removeViolation(UUID violationId, UUID playerId) {
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(removeViolationSql)) {
                statement.setString(1, violationId.toString());
                statement.executeUpdate();
            }

            var violationSet = _violationCache.getIfPresent(playerId);
            if (violationSet != null) {
                violationSet.removeIf(v -> v.getId().equals(violationId));
            }

            var activeViolationSet = _violationActiveCache.getIfPresent(playerId);
            if (activeViolationSet != null) {
                activeViolationSet.removeIf(v -> v.getId().equals(violationId));
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while removing violation...\n%s", ex));
        }
    }

    @Override
    public Optional<Set<ViolationData>> getViolations(UUID playerId) {
        var data = _violationCache.getIfPresent(playerId);
        if (data != null) {
            return Optional.of(data);
        }

        data = new HashSet<>();
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(getViolationsSql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        data.add(new ViolationData(
                                UUID.fromString(result.getString("Id")),
                                UUID.fromString(result.getString("PlayerId")),
                                EViolationType.valueOf(result.getString("Type")),
                                result.getString("Details"),
                                result.getLong("Timestamp")
                        ));
                    }
                }
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while finding violations...\n%s", ex.getMessage()));
            return Optional.empty();
        }

        _violationCache.put(playerId, data);
        return Optional.of(data);
    }

    @Override
    public Optional<Set<ViolationData>> getActiveViolations(UUID playerId) {
        var data = _violationActiveCache.getIfPresent(playerId);
        if (data != null) {
            return Optional.of(data);
        }

        data = new HashSet<>();
        try (Connection connection = _dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(getActiveViolationsSql)) {
                statement.setString(1, playerId.toString());
                statement.setLong(2, System.currentTimeMillis());
                statement.setLong(3, OpenChat.moderationConfig().violationDurationMilliseconds);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        data.add(new ViolationData(
                                UUID.fromString(result.getString("Id")),
                                UUID.fromString(result.getString("PlayerId")),
                                EViolationType.valueOf(result.getString("Type")),
                                result.getString("Details"),
                                result.getLong("Timestamp")
                        ));
                    }
                }
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while finding active violations...\n%s", ex.getMessage()));
            return Optional.empty();
        }

        _violationActiveCache.put(playerId, data);
        return Optional.of(data);
    }

    @Override
    public Optional<Set<ViolationData>> getActiveViolationsByType(UUID playerId, EViolationType type) {
        var data = _violationActiveCache.getIfPresent(playerId);
        if (data != null) {
            return Optional.of(data.stream()
                    .filter(x -> x.getType() == type)
                    .collect(Collectors.toSet())
            );
        }

        return getActiveViolations(playerId).map(violations ->
            violations.stream()
                    .filter(x -> x.getType() == type)
                    .collect(Collectors.toSet())
        );
    }

    //#endregion
}