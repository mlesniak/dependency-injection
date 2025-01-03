package com.mlesniak.di;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer implements CommandLineRunner {
    private final MessageProvider messageProvider;

    public MessageConsumer(MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    @Override
    public void run(String... args) {
        String message = messageProvider.getMessage();
        System.out.println(message);
    }
}
