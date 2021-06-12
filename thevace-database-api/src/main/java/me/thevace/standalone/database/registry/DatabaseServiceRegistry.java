package me.thevace.standalone.database.registry;

import com.google.inject.Injector;

public class DatabaseServiceRegistry {

    private static Injector injector;

    public static <T> T getService(Class<T> serviceClass) {
        return DatabaseServiceRegistry.injector.getInstance(serviceClass);
    }

    public static void injectMembers(Object instance) {
        DatabaseServiceRegistry.injector.injectMembers(instance);
    }

    public static boolean isServicePresent(Class<?> serviceClass) {
        return DatabaseServiceRegistry.injector.getInstance(serviceClass) != null;
    }

    public static void setInjector(Injector injector) {
        if(DatabaseServiceRegistry.injector == null)
            DatabaseServiceRegistry.injector = injector;
    }
}
