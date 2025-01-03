package com.mlesniak;

import com.mlesniak.boot.Component;

@Component
public class MessageUsageService {
    private final MessageProviderService messageProviderService;

    public MessageUsageService(MessageProviderService messageProviderService) {
        this.messageProviderService = messageProviderService;
    }

    public void useService() {
        System.out.println(messageProviderService.getMessage());
    }
}
