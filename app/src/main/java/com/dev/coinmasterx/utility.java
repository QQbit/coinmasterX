package com.dev.coinmasterx;
import java.util.Random;
import java.util.UUID;

public class utility {

    public static String generateUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public static String randomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(randomStringBuilder.length());
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}

