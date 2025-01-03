package com.mlesniak.boot;

/// Marker interface to determine where our application
/// starts since we do not have typical controllers
/// waiting for HTTP requests.
public interface CommandLineRunner {
    void run(String... args);
}
