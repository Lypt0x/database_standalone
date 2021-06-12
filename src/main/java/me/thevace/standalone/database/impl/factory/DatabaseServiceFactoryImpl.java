package me.thevace.standalone.database.impl.factory;

import com.google.inject.Inject;
import me.thevace.standalone.database.api.factory.DatabaseServiceFactory;
import me.thevace.standalone.database.api.service.DatabaseService;
import me.thevace.standalone.database.impl.service.DatabaseServiceImpl;
import me.thevace.standalone.database.model.DatabaseModel;

public class DatabaseServiceFactoryImpl implements DatabaseServiceFactory {

    @Inject
    public DatabaseServiceFactoryImpl() {}

    @Override
    public DatabaseService create(DatabaseModel databaseModel) {
        return new DatabaseServiceImpl(databaseModel);
    }
}
