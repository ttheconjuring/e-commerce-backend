package com.demo.utility;

import java.util.Random;

public class Generator {

    private Generator() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    private static final String[] failureReasons = {
            "Reason: The shipping address is incomplete or malformed (code: invalid_address)",
            "Reason: The address is well-formed but cannot be verified by an address validation service. (code: address_not_verified)",
            "Reason: Shipping is not allowed to the specified country, state, or address due to regulations or business rules. (code: destination_restricted)",
            "Reason: The selected carrier or shipping method does not deliver to P.O. boxes. (code: po_box_not_supported)",
            "Reason: An item in the order cannot be shipped (e.g., hazardous material, perishable goods with wrong shipping method). (code: item_not_shippable)"
    };

    public static String failureReason() {
        int randomIndex = new Random().nextInt(failureReasons.length);
        return failureReasons[randomIndex];
    }

    public static String trackingNumber() {
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
