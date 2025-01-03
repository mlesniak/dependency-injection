package com.mlesniak.boot;

import com.mlesniak.Main;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class SummerApplication {
    public static void run(Class<Main> mainClass, String[] args) {
        List<Class<?>> components = getComponents(mainClass);
        System.out.println("Components:");
        components.forEach(System.out::println);

        Map<Class<?>, Object> instances = new HashMap<>();
        components.forEach(component -> createInstances(instances, component));

        // instances.keySet().forEach(System.out::println);

        // Find entry point.
        var entryClass = instances.keySet().stream().filter(c -> Arrays.stream(c.getInterfaces()).anyMatch(i -> i == CommandLineRunner.class)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No entry point via CommandLineRunner found"));

        ((CommandLineRunner) instances.get(entryClass)).run(args);
    }

    // No cycle check yet.
    private static void createInstances(Map<Class<?>, Object> instances, Class<?> clazz) {
        System.out.println("Handling " + clazz.getSimpleName());
        var cs = clazz.getDeclaredConstructors();
        // No unique constructor.
        if (cs.length > 1) {
            throw new IllegalArgumentException("No unique constructor found");
        }

        // We might have dependencies. Look them up or generate them if they are not used.
        var constructor = cs[0];
        var expectedTypes = constructor.getParameterTypes();

        // No dependencies? We can create an object.
        if (expectedTypes.length == 0) {
            try {
                instances.put(clazz, clazz.getDeclaredConstructor().newInstance());
                return;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        // For every dependency, generate them.
        Arrays.stream(expectedTypes).forEach(depClass -> {
            System.out.println("Creating depending instance for " + depClass.getSimpleName());
            createInstances(instances, depClass);
        });

        var params = Arrays.stream(expectedTypes).map(param -> instances.get(param)).toArray();
        try {
            instances.put(clazz, constructor.newInstance(params));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Class<?>> getComponents(Class<Main> mainClass) {
        List<Class<?>> components;
        try {
            components = findAllClassesInPackage(mainClass.getPackageName()).
                    stream()
                    .filter(c -> c.getAnnotation(Component.class) != null)
                    .collect(Collectors.toList());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return components;
    }

    public static Set<Class<?>> findAllClassesInPackage(String packageName) throws IOException, URISyntaxException {
        String path = packageName.replace('.', '/');
        URI uri = SummerApplication.class.getResource("/" + path).toURI();

        if (uri.getScheme().equals("jar")) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                return findClassesInPath(fileSystem.getPath(path), packageName);
            }
        } else {
            return findClassesInPath(Paths.get(uri), packageName);
        }
    }

    private static Set<Class<?>> findClassesInPath(Path path, String packageName) throws IOException {
        try (var walk = Files.walk(path, 1)) {
            return walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().endsWith(".class"))
                    .map(p -> getClass(p, packageName))
                    .filter(c -> c != null)
                    .collect(Collectors.toSet());
        }
    }

    private static Class<?> getClass(Path classPath, String packageName) {
        try {
            String className = packageName + "." + classPath.getFileName().toString().replace(".class", "");
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
