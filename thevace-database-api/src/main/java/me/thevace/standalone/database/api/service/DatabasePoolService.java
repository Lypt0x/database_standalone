package me.thevace.standalone.database.api.service;

import me.thevace.standalone.database.model.DatabaseModel;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface DatabasePoolService {

    List<DatabaseService> getDatabases();

    CompletableFuture<DatabaseService> addDatabase(final DatabaseModel databaseModel);

    Optional<DatabaseService> getDatabase(final String providerName);

    void removeDatabase(final String providerName);

    boolean isDatabasePresent(final String providerName);
    boolean isDatabasePresent(final Predicate<? super DatabaseService> databaseServicePredicate);

}
