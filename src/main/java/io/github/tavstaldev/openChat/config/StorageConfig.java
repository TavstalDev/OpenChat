package io.github.tavstaldev.openChat.config;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.openChat.OpenChat;

import java.util.List;

public class StorageConfig extends ConfigurationBase {

    public StorageConfig() {
        super(OpenChat.Instance, "storage.yml", null);
    }

    public String type, filename, host, database, username, password, tablePrefix;
    public int port;

    @Override
    public void loadDefaults() {
        type = resolveGet("storage.type", "sqlite");
        resolveComment("storage.type", List.of(
                "Type of storage to use for the chat data.",
                "Supported types are 'sqlite' and 'mysql'."
        ));
        filename = resolveGet("storage.filename", "database");
        resolveComment("storage.filename", List.of(
                "Filename for SQLite database (without extension).",
                "This setting is only used if 'storage.type' is set to 'sqlite'."
        ));
        host = resolveGet("storage.host", "localhost");
        resolveComment("storage.host", List.of(
                "Hostname or IP address of the MySQL server."
        ));
        port = resolveGet("storage.port", 3306);
        resolveComment("storage.port", List.of(
                "Port number of the MySQL server."
        ));
        database = resolveGet("storage.database", "minecraft");
        resolveComment("storage.database", List.of(
                "Name of the MySQL database to use."
        ));
        username = resolveGet("storage.username", "root");
        resolveComment("storage.username", List.of(
                "Username for connecting to the MySQL database."
        ));
        password = resolveGet("storage.password", "ascent");
        resolveComment("storage.password", List.of(
                "Password for connecting to the MySQL database."
        ));
        tablePrefix = resolveGet("storage.tablePrefix", "openchat");
        resolveComment("storage.tablePrefix", List.of(
                "Prefix to use for all database table names.",
                "This can be useful if you want to run multiple instances of OpenChat in the same database."
        ));
    }
}
