package me.thevace.standalone.database.api.service;

import com.zaxxer.hikari.HikariDataSource;

import java.rmi.Remote;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface DatabaseService {

    void executeQuery(String query, Object... args) throws SQLException;
    void executeUpdate(String query, Consumer<Integer> updateResult, Object... args) throws SQLException;
    void executeQuery(String query, Consumer<ResultSet> resultCallback, Object... args) throws SQLException;
    void addArguments(PreparedStatement preparedStatement, Object... args) throws SQLException;

    boolean isDataSourceClosed();
    boolean tryClose();

    Object fetchObject(String query, String arg, String selection) throws SQLException;

    HikariDataSource getDataSource();

    <T> CompletableFuture<T> schedule(Callable<T> callable);


}
