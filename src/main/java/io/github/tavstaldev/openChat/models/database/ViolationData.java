package io.github.tavstaldev.openChat.models.database;

import java.util.UUID;

public class ViolationData {
    private final UUID id;
    private final UUID uuid;

    private final EViolationType type;

    private final String details;

    private final long timestamp;

    public ViolationData(UUID id, UUID uuid, EViolationType type, String details, long timestamp) {
        this.id = id;
        this.uuid = uuid;
        this.type = type;
        this.details = details;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public EViolationType getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
