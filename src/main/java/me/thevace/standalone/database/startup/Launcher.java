package me.thevace.standalone.database.startup;

import com.google.inject.Guice;
import me.thevace.standalone.database.api.service.DatabasePoolService;
import me.thevace.standalone.database.api.service.DatabaseService;
import me.thevace.standalone.database.module.DatabaseModule;
import me.thevace.standalone.database.registry.DatabaseServiceRegistry;

import java.sql.SQLException;
import java.util.Optional;

public class Launcher {

    public static void main(String[] args) {

        Launcher instance = new Launcher();
        instance.start();

    }

    public void start() {
        DatabaseServiceRegistry.setInjector(Guice.createInjector(new DatabaseModule()));
        DatabaseLoader databaseLoader = new DatabaseLoader();

        DatabaseServiceRegistry.injectMembers(databaseLoader);
        databaseLoader.load();

	/* Infinite loop for keeping instances */
        while(true) {
            System.console().readLine();
        }
    }

}
