package com.mlesniak.di;

import org.springframework.stereotype.Component;

@Component
public class MessageProvider {
    public String getMessage() {
        return "Hello, world";
    }
}