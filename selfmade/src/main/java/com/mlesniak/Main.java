package com.mlesniak;

import com.mlesniak.boot.CommandLineRunner;
import com.mlesniak.boot.Component;
import com.mlesniak.boot.SummerApplication;

@Component
public class Main implements CommandLineRunner {
    private final UsageService usageService;

    public Main(UsageService usageService) {
        this.usageService = usageService;
    }

    public static void main(String... args) {
        SummerApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        usageService.useService();
    }
}
