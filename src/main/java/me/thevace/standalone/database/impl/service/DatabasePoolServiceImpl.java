package me.thevace.standalone.database.impl.service;

import com.google.inject.Inject;
import me.thevace.standalone.database.api.factory.DatabaseServiceFactory;
import me.thevace.standalone.database.api.service.DatabasePoolService;
import me.thevace.standalone.database.api.service.DatabaseService;
import me.thevace.standalone.database.model.DatabaseModel;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public final class DatabasePoolServiceImpl implements DatabasePoolService {

    private final List<DatabaseService> databaseServices;
    private final DatabaseServiceFactory databaseServiceFactory;

    @Inject
    public DatabasePoolServiceImpl(DatabaseServiceFactory databaseServiceFactory) {
        this.databaseServices = new ArrayList<>();
        this.databaseServiceFactory = databaseServiceFactory;
    }

    @Override
    public List<DatabaseService> getDatabases() {
        return this.databaseServices;
    }

    @Override
    public CompletableFuture<DatabaseService> addDatabase(final DatabaseModel databaseModel) {
        DatabaseServiceImpl databaseService = (DatabaseServiceImpl) this.databaseServiceFactory.create(databaseModel);

        CompletableFuture<Boolean> loadResult = databaseService.load(databaseModel.getProperties());
        CompletableFuture<DatabaseService> databaseServiceCompletableFuture = new CompletableFuture<>();

        loadResult.whenComplete((result, throwable) -> {
            if(throwable == null && result)
                databaseServiceCompletableFuture.complete(databaseService);
            else
                databaseServiceCompletableFuture.completeExceptionally(throwable);
        });

        System.out.println("added");
        this.databaseServices.add(databaseService);
        return databaseServiceCompletableFuture;
    }

    @Override
    public Optional<DatabaseService> getDatabase(String providerName) {
        return this.databaseServices.stream().filter(databaseService
                -> ((DatabaseServiceImpl)databaseService).getDatabaseModel().getProvider().equals(providerName))
                .findFirst();
    }

    @Override
    public void removeDatabase(String providerName) {
        Optional<DatabaseService> databaseService = this.getDatabase(providerName);
        databaseService.ifPresent(database -> {
            ((DatabaseServiceImpl)database).unload();
            this.databaseServices.remove(database);
        });
    }

    @Override
    public boolean isDatabasePresent(String providerName) {
        return this.databaseServices.stream().anyMatch(databaseService
                -> ((DatabaseServiceImpl)databaseService).getDatabaseModel().getProvider().equals(providerName));
    }

    @Override
    public boolean isDatabasePresent(final Predicate<? super DatabaseService> databaseServicePredicate) {
        return this.databaseServices.stream().anyMatch(databaseServicePredicate);
    }

}
