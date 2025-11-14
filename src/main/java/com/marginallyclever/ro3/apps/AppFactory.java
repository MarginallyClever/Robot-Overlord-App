package com.marginallyclever.ro3.apps;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link AppFactory} uses Reflection to find and register all Apps in a given package.
 * Then DockingPanel can call on the factory to create instances of those Apps as needed.
 */
public class AppFactory {
    private static final Map<String, AppCreator> appCreators = new HashMap<>();

    /**
     * Use Reflection to find and register all Apps in the given package.
     * ScanResult#getSubclasses finds all classes that extend App.
     * ScanResult#getClassesImplementing finds all classes that implement an interface, which is what ChatGPT suggested,
     * but App is not an interface, so it finds none.
     * @param sourcePath the package to scan for Apps
     */
    public static void registerApps(String sourcePath) {
        System.out.println("Scanning package: " + sourcePath);
        try(ScanResult result = new ClassGraph()
                //.verbose() // Enable verbose output to debug the scanning process
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(sourcePath)
                .scan()) {
            System.out.println("searching for "+App.class.getName()+" implementations...");
            var appClasses = result.getSubclasses(App.class.getName()).loadClasses();
            for (var appClass : appClasses) {
                // Every App has a default constructor.  Store a reference to a lambda that creates one.
                String appName = appClass.getName();
                System.out.println("Registering App: " + appName);

                appCreators.put(appName, () -> {
                    try {
                        App instance = (App)appClass.getDeclaredConstructor().newInstance();
                        return instance;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create App instance for " + appClass.getName(), e);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package for Apps: " + sourcePath, e);
        }
    }

    /**
     * List all registered Apps to the given PrintStream.
     * @param out the PrintStream to write to
     */
    public static void listApps(PrintStream out) {
        out.println("Registered Apps:");
        for (var appName : appCreators.keySet()) {
            out.println(" - " + appName);
        }
    }

    public static void main(String[] args) {
        AppFactory.registerApps("com.marginallyclever.ro3.apps");
        AppFactory.listApps(System.out);
    }

    public static int getRegisteredAppsCount() {
        return appCreators.size();
    }

    public static App create(String name) {
        for(String s : appCreators.keySet()) {
            if(s.endsWith(name)) {
                name = s;
                break;
            }
        }
        var creator = appCreators.get(name);
        return  (creator == null) ? null :  creator.createApp();
    }
}
