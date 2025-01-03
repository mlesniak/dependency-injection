package com.mlesniak.boot;

import com.mlesniak.Main;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class SummerApplication {
    public static void run(Class<Main> mainClass, String[] args) {
        Set<Class<?>> components = getComponents(mainClass);
        System.out.println(components);
    }

    private static Set<Class<?>> getComponents(Class<Main> mainClass) {
        Set<Class<?>> components;
        try {
            components = findAllClassesInPackage(mainClass.getPackageName()).
                    stream()
                    .filter(c -> c.getAnnotation(Component.class) != null)
                    .collect(Collectors.toSet());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return components;
    }

    public static Set<Class<?>> findAllClassesInPackage(String packageName) throws IOException, URISyntaxException {
        String path = packageName.replace('.', '/');
        URI uri = SummerApplication.class.getResource("/" + path).toURI();
        System.out.println(uri);

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
