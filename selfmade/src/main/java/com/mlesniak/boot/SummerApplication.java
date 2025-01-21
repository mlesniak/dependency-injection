package com.mlesniak.boot;

import com.mlesniak.Main;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/// Core dependency injection resolution.
public class SummerApplication {
    /// Entry point into dependency injection.
    ///
    /// @param mainClass The main class of the application, ideally placed at the root package.
    /// @param args      Command line args.
    public static void run(Class<Main> mainClass, String[] args) {
        List<Class<?>> components = getComponents(mainClass);

        // For our example, we support only singletons.
        Map<Class<?>, Object> instances = new HashMap<>();
        components.forEach(component -> createSingleton(instances, new HashSet<>(), component));

        // Find entry point by looking for the class implementing CommandLineRunner.
        var entryClasses = instances
                .keySet().stream()
                .filter(SummerApplication::hasCommandLineRunnerInterface)
                .toList();
        if (entryClasses.isEmpty()) {
            throw new IllegalStateException("No entry point defined via CommandLineRunner");
        }
        if (entryClasses.size() > 1) {
            throw new IllegalStateException("Ambiguous entry points defined via CommandLineRunner");
        }
        var entryClass = entryClasses.getFirst();

        ((CommandLineRunner) instances.get(entryClass)).run(args);
    }

    private static boolean hasCommandLineRunnerInterface(Class<?> clazz) {
        return Arrays.stream(clazz.getInterfaces())
                .anyMatch(i -> i == CommandLineRunner.class);
    }

    /// Creates a new instance for the passed class using its constructor.
    ///
    /// We resolve all dependent constructor parameters.
    private static void createSingleton(Map<Class<?>, Object> singletons, Set<Class<?>> visited, Class<?> clazz) {
        // Cycle detection. We've been called to resolve a parameter dependency, but already tried to resolve the
        // dependencies for this class. When trying to resolve clazz' dependencies, we will run into an infinite cycle.
        if (!visited.add(clazz)) {
            var names = visited.stream().map(Class::getSimpleName).collect(Collectors.joining(", "));
            throw new IllegalStateException("Cycle detected. Visited classes=" + names);
        }
        var cs = clazz.getDeclaredConstructors();
        if (cs.length > 1) {
            throw new IllegalArgumentException("No unique constructor found for " + clazz.getSimpleName());
        }

        var constructor = cs[0];
        var expectedInjections = constructor.getParameterTypes();

        // For every dependent dependency, generate a new instance. Note that we implicitly handle the case for
        // parameter-less constructors here as well.
        Arrays.stream(expectedInjections).forEach(depClass -> {
            createSingleton(singletons, visited, depClass);
        });

        var params = Arrays.stream(expectedInjections).map(singletons::get).toArray();
        try {
            singletons.put(clazz, constructor.newInstance(params));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to create instance for " + clazz.getSimpleName(), e);
        }
    }

    /// Get a list of all components based on the passed package of the class.
    ///
    /// @param mainClass the root class to start scanning.
    private static List<Class<?>> getComponents(Class<Main> mainClass) {
        try {
            return findAllClassesInPackage(mainClass.getPackageName()).
                    stream()
                    .filter(c -> c.getAnnotation(Component.class) != null)
                    .collect(Collectors.toList());
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Error retrieving components, starting at " + mainClass.getSimpleName(), e);
        }
    }

    /// Retrieve a list of all classes in a package (or its children). This method supports both unpacked
    /// (target/classes) and packed (.jar) class containers.
    private static Set<Class<?>> findAllClassesInPackage(String packageName) throws IOException, URISyntaxException {
        String path = packageName.replace('.', '/');
        URI uri = SummerApplication.class.getResource("/" + path).toURI();

        if (uri.getScheme().equals("jar")) {
            // We have to create a "virtual" filesystem to access the class files stored in the
            // .jar file.
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                return findClassesInPath(fileSystem.getPath(path), packageName);
            }
        }

        // We're running the injection code from an unpacked archive and can directly access the .class files.
        return findClassesInPath(Paths.get(uri), packageName);
    }

    /// Iterate through all .class files in the given path for the given package.
    private static Set<Class<?>> findClassesInPath(Path path, String packageName) throws IOException {
        try (var walk = Files.walk(path, 1)) {
            return walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().endsWith(".class"))
                    .map(p -> getClass(p, packageName))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }

    /// Retrieve a class based on the path and package name.
    private static Class<?> getClass(Path classPath, String packageName) {
        try {
            String className = packageName + "." + classPath.getFileName().toString().replace(".class", "");
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
