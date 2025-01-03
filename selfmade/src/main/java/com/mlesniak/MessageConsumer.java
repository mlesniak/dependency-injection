package com.mlesniak;

import com.mlesniak.boot.CommandLineRunner;
import com.mlesniak.boot.Component;

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
