package me.thevace.standalone.database.module;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import me.thevace.standalone.database.api.factory.DatabaseServiceFactory;
import me.thevace.standalone.database.api.service.DatabasePoolService;
import me.thevace.standalone.database.api.service.DatabaseService;
import me.thevace.standalone.database.impl.service.DatabasePoolServiceImpl;
import me.thevace.standalone.database.impl.service.DatabaseServiceImpl;

public class DatabaseModule extends AbstractModule {

    @Override
    protected void configure() {
        super.bind(DatabasePoolService.class).to(DatabasePoolServiceImpl.class);
        super.install(new FactoryModuleBuilder()
                .implement(DatabaseService.class, DatabaseServiceImpl.class)
                .build(DatabaseServiceFactory.class));
    }

}
