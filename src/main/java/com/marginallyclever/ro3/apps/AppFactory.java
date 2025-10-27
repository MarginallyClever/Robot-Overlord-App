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
    private final Map<String, AppCreator> appCreators = new HashMap<>();

    /**
     * Use Reflection to find and register all Apps in the given package.
     * ScanResult#getSubclasses finds all classes that extend App.
     * ScanResult#getClassesImplementing finds all classes that implement an interface, which is what ChatGPT suggested,
     * but App is not an interface, so it finds none.
     * @param sourcePath the package to scan for Apps
     */
    public void registerApps(String sourcePath) {
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
                System.out.println("Registering App: " + appClass.getName());

                appCreators.put(appClass.getName(), () -> {
                    try {
                        return (App)appClass.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create App instance for " + appClass.getName(), e);
                    }
                });
            }
        }

    }

    public void listApps(PrintStream out) {
        out.println("Registered Apps:");
        for (var appName : appCreators.keySet()) {
            out.println(" - " + appName);
        }
    }

    public static void main(String[] args) {
        var f = new AppFactory();
        f.registerApps("com.marginallyclever.ro3.apps");
        f.listApps(System.out);
    }
}
