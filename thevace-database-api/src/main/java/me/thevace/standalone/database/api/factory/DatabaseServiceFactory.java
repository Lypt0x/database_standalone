package me.thevace.standalone.database.api.factory;

import me.thevace.standalone.database.api.service.DatabaseService;
import me.thevace.standalone.database.model.DatabaseModel;

public interface DatabaseServiceFactory {

    DatabaseService create(final DatabaseModel databaseModel);

}
