package com.mlesniak;

import com.mlesniak.boot.CommandLineRunner;
import com.mlesniak.boot.Component;
import com.mlesniak.boot.SummerApplication;

@Component
public class Main implements CommandLineRunner {
    private final MessageUsageService usageService;

    public Main(MessageUsageService usageService) {
        this.usageService = usageService;
    }

    public static void main(String... args) {
        SummerApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        usageService.useService();
    }
}
