package me.thevace.standalone.database.startup;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.inject.Inject;
import me.thevace.standalone.database.api.service.DatabasePoolService;
import me.thevace.standalone.database.api.service.DatabaseService;
import me.thevace.standalone.database.impl.service.DatabaseServiceImpl;
import me.thevace.standalone.database.model.DatabaseModel;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.adelean.inject.resources.core.InjectResources.resource;

public class DatabaseLoader {

    @Inject
    private DatabasePoolService databasePoolService;
    private final Gson gson = new Gson();

    private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("DatabaseLoader")
            .build());

    public void load() {

        System.out.print("\033[H\033[2J");
        System.out.flush();

        File databaseFile = new File("database.json");
        if(!(databaseFile.exists())) {
            try {
                Files.copy(resource().withPath("database.json").asInputStream().inputStream(),
                        databaseFile.toPath());
            } catch (IOException ioException) {
                System.out.println("Could not copy Database-File");
                ioException.printStackTrace();
            }
        }

        List<DatabaseModel> databaseModels;
        try {
            databaseModels = this.gson.fromJson(
                    FileUtils.readFileToString(databaseFile, StandardCharsets.UTF_8),
                    new TypeToken<List<DatabaseModel>>(){}.getType()
            );

            databaseModels.forEach(databaseModel -> {
                System.out.printf("Loading Database called '%s' ..%n", databaseModel.getProvider());
                this.databasePoolService.addDatabase(databaseModel).whenComplete((result, throwable) -> {
                    if(result != null)
                        System.out.printf("Successfully loaded database called '%s'%n", databaseModel.getProvider());
                    else {
                        System.out.printf("Could not load database '%s': %s%n",
                                databaseModel.getProvider(),
                                throwable.getMessage());

                        this.executorService.execute(() -> {
                            System.out.printf("Retrying to connect to '%s' in 5 seconds..%n", databaseModel.getProvider());
                            Optional<DatabaseService> optionalDatabaseService = this.databasePoolService
                                    .getDatabase(databaseModel.getProvider());

                            optionalDatabaseService.ifPresent(databaseService -> {
                                while(databaseService.isDataSourceClosed()) {
                                    try {
                                        Thread.sleep(5000);
                                        ((DatabaseServiceImpl) databaseService).load(databaseModel.getProperties());
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    ((DatabaseServiceImpl)databaseService).getHikariConfig().decrementPoolNumber();
                                }
                            });
                        });
                    }
                });
            });
        } catch (IOException ioException) {
            System.out.println("Could not parse Database-File");
            ioException.printStackTrace();
        }

    }

    public DatabasePoolService getDatabasePoolService() {
        return databasePoolService;
    }
}
