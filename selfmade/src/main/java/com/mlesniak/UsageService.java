package com.mlesniak;

import com.mlesniak.boot.Component;

@Component
public class UsageService {
    private DemoService demoService;

    public UsageService(DemoService demoService) {
        this.demoService = demoService;
    }

    public void useService() {
        System.out.println(demoService.getMessage());
    }
}
