package com.mlesniak;

import com.mlesniak.boot.Component;

@Component
public class MessageProvider {
    public String getMessage() {
        return "Hello, world";
    }
}