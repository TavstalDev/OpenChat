package io.github.tavstaldev.openChat.database;

import io.github.tavstaldev.openChat.models.database.EViolationType;
import io.github.tavstaldev.openChat.models.database.PlayerData;
import io.github.tavstaldev.openChat.models.database.ViolationData;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Interface representing the database operations for the OpenChat plugin.
 * Provides methods for managing player data, violations, and other database-related tasks.
 */
public interface IDatabase {

    /**
     * Loads the database, initializing any necessary connections or resources.
     */
    void load();

    /**
     * Updates the database sql statements
     */
    void update();

    /**
     * Unloads the database, releasing any resources or connections.
     */
    void unload();

    /**
     * Checks the database schema to ensure it matches the expected structure.
     */
    void checkSchema();

    /**
     * Adds a new player to the database.
     *
     * @param playerId The UUID of the player to add.
     */
    void addPlayerData(UUID playerId);

    /**
     * Updates the data of an existing player in the database.
     *
     * @param newData The updated player data.
     */
    void updatePlayerData(PlayerData newData);

    /**
     * Removes a player from the database.
     *
     * @param playerId The UUID of the player to remove.
     */
    void removePlayerData(UUID playerId);

    /**
     * Retrieves the data of a player from the database.
     *
     * @param playerId The UUID of the player.
     * @return An Optional containing the player's data, or empty if not found.
     */
    Optional<PlayerData> getPlayerData(UUID playerId);

    /**
     * Checks if public chat is disabled for a specific player.
     *
     * @param playerId The UUID of the player.
     * @return True if public chat is disabled, false otherwise.
     */
    boolean isPublicChatDisabled(UUID playerId);

    /**
     * Checks if the Social Spy feature is enabled for a specific player.
     *
     * @param player The player to check.
     * @return True if Social Spy is enabled, false otherwise.
     */
    boolean isSocialSpyEnabled(Player player);

    /**
     * Adds a player to another player's ignore list.
     *
     * @param playerId       The UUID of the player adding the ignore.
     * @param ignoredPlayerId The UUID of the player to be ignored.
     */
    void addIgnoredPlayer(UUID playerId, UUID ignoredPlayerId);

    /**
     * Removes a player from another player's ignore list.
     *
     * @param playerId       The UUID of the player removing the ignore.
     * @param ignoredPlayerId The UUID of the player to be removed from the ignore list.
     */
    void removeIgnoredPlayer(UUID playerId, UUID ignoredPlayerId);

    /**
     * Checks if a player is ignored by another player.
     *
     * @param playerId       The UUID of the player checking the ignore.
     * @param ignoredPlayerId The UUID of the player being checked.
     * @return True if the player is ignored, false otherwise.
     */
    boolean isPlayerIgnored(UUID playerId, UUID ignoredPlayerId);

    /**
     * Adds a violation to a player's record.
     *
     * @param playerId The UUID of the player.
     * @param type     The type of violation.
     * @param details  Additional details about the violation.
     */
    void addViolation(UUID playerId, EViolationType type, String details);

    /**
     * Removes a violation from a player's record.
     *
     * @param violationId The UUID of the violation to remove.
     * @param playerId    The UUID of the player.
     */
    void removeViolation(UUID violationId, UUID playerId);

    /**
     * Retrieves all violations for a specific player.
     *
     * @param playerId The UUID of the player.
     * @return An Optional containing a set of all violations, or empty if none exist.
     */
    Optional<Set<ViolationData>> getViolations(UUID playerId);

    /**
     * Retrieves all active violations for a specific player.
     *
     * @param playerId The UUID of the player.
     * @return An Optional containing a set of active violations, or empty if none exist.
     */
    Optional<Set<ViolationData>> getActiveViolations(UUID playerId);

    /**
     * Retrieves all active violations of a specific type for a player.
     *
     * @param playerId The UUID of the player.
     * @param type     The type of violations to retrieve.
     * @return An Optional containing a set of active violations of the specified type, or empty if none exist.
     */
    Optional<Set<ViolationData>> getActiveViolationsByType(UUID playerId, EViolationType type);
}