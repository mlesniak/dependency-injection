package com.mlesniak.boot;

@FunctionalInterface
public interface CommandLineRunner {
    void run(String... args);
}
