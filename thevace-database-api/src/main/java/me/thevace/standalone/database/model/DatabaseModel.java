package me.thevace.standalone.database.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DatabaseModel {

    private final String provider;

    private final String hostname, username, password, database;
    private final int port;

    private final Map<String, Object> properties;

    public DatabaseModel(
            final String provider, final String hostname, final String username,
            final String password, final String database,
            final int port, final Map<String, Object> propertyMap) {
        this.provider = provider;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.database = database;
        this.port = port;
        this.properties = propertyMap;
    }

    public String getProvider() {
        return this.provider;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getDatabase() {
        return this.database;
    }

    public int getPort() {
        return this.port;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public static Optional<DatabaseModel> getDatabase(final String providerName,
                                                   final List<DatabaseModel> databaseModelList) {
        return databaseModelList.stream()
                .filter(itemModel -> itemModel.provider.equals(providerName))
                .findFirst();
    }

}
