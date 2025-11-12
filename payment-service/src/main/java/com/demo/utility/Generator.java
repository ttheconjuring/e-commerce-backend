package com.demo.utility;

import java.util.Random;

public class Generator {

    private Generator(){}

    private static final String[] failureReasons = {
            "Reason: Incorrect cardholder name (code: 45678)",
            "Reason: Incorrect cardholder number (code: 3845)",
            "Reason: Incorrect expiry date (code: 3858)",
            "Reason: Incorrect CVC/CVV (code: 2457)",
    };

    public static String failureReason() {
        int randomIndex = new Random().nextInt(failureReasons.length);
        return failureReasons[randomIndex];
    }

    public static String transactionId() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 15) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

}
