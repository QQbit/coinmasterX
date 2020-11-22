package com.dev.coinmasterx;
import java.util.UUID;

public class utility {

    public static String generateUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }
}

