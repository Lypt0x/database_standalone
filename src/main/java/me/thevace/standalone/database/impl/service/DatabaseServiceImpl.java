package me.thevace.standalone.database.impl.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.thevace.standalone.database.api.service.DatabaseService;
import me.thevace.standalone.database.model.DatabaseModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class DatabaseServiceImpl implements DatabaseService {

    private final DatabaseModel databaseModel;
    private HikariConfig hikariConfig;

    private HikariDataSource hikariDataSource;

    private final ExecutorService executorService;

    @Inject
    public DatabaseServiceImpl (
            @Assisted final DatabaseModel databaseModel) {
        this.databaseModel = databaseModel;
        this.executorService = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setNameFormat("TheVace-Database-" + databaseModel.getProvider())
                .setUncaughtExceptionHandler((thread, throwable) -> {
                    System.err.println("Exception caught in thread " + thread.getName() + ": ");
                    throwable.printStackTrace();
                    System.err.println("Provider --> " + this.databaseModel.getProvider());
            }).build()
        );
    }


    public CompletableFuture<Boolean> load(final Map<String, Object> hikariPropertiesMap) {
        if(this.hikariDataSource == null || !(this.hikariDataSource.isClosed())) {
            return this.schedule(() -> {
                this.hikariConfig = new HikariConfig();
                this.hikariConfig.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s",
                        this.databaseModel.getHostname(), this.databaseModel.getPort(),
                        this.databaseModel.getDatabase()));

                this.hikariConfig.setUsername(this.databaseModel.getUsername());
                this.hikariConfig.setPassword(this.databaseModel.getPassword());

                hikariPropertiesMap.forEach(this.hikariConfig::addDataSourceProperty);

                this.hikariDataSource = new HikariDataSource(this.hikariConfig);
                return true;
            });
        }

        return CompletableFuture.completedFuture(false);
    }

    public HikariConfig getHikariConfig() {
        return hikariConfig;
    }

    public void unload() {
        this.hikariDataSource.close();
        this.hikariDataSource = null;
    }

    @Override
    public void executeQuery(String query, Object... args) throws SQLException {
        try(final Connection connection = this.hikariDataSource.getConnection()) {
            try(final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                addArguments(preparedStatement, args);
                preparedStatement.execute();
            }
        }
    }

    @Override
    public void executeUpdate(String query, Consumer<Integer> updateResult, Object... args) throws SQLException {
        try(final Connection connection = this.hikariDataSource.getConnection()) {
            try(final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                addArguments(preparedStatement, args);
                updateResult.accept(preparedStatement.executeUpdate());
            }
        }
    }

    @Override
    public void executeQuery(String query, Consumer<ResultSet> resultCallback, Object... args) throws SQLException {
        try(final Connection connection = this.hikariDataSource.getConnection()) {
            try(final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                addArguments(preparedStatement, args);
                resultCallback.accept(preparedStatement.executeQuery());
            }
        }
    }

    @Override
    public Object fetchObject(String query, String arg, String selection) throws SQLException {
        try(final Connection connection = this.hikariDataSource.getConnection()) {
            try(final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                addArguments(preparedStatement, arg);
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    if(resultSet.next()) return resultSet.getObject(selection);
                }
            }
        }
        return null;
    }

    @Override
    public void addArguments(PreparedStatement preparedStatement, Object... args) throws SQLException {
        int position = 1;
        for (final Object arg : args) {
            preparedStatement.setObject(position, arg);
            position++;
        }
    }

    @Override
    public boolean isDataSourceClosed() {
        return this.hikariDataSource == null || this.hikariDataSource.isClosed();
    }

    @Override
    public boolean tryClose() {
        try {
            this.hikariDataSource.getConnection().close();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public HikariDataSource getDataSource() {
        return this.hikariDataSource;
    }

    @Override
    public <T> CompletableFuture<T> schedule(Callable<T> callable) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        this.executorService.execute(() -> {
            try {
                completableFuture.complete(callable.call());
            } catch (Exception exception) {
                completableFuture.completeExceptionally(exception);
            }
        });

        return completableFuture;
    }

    public DatabaseModel getDatabaseModel() {
        return databaseModel;
    }
}
