package com.mlesniak;

import com.mlesniak.boot.Component;

@Component
public class MessageProviderService {
    public String getMessage() {
        return "Hello, world";
    }
}
