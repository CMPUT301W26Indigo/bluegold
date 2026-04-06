package com.eventlottery.model;

public class ValidatePhone {
    private String phoneNumber;

    public ValidatePhone(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        // If statement added by Gemini to fix issue with null pointer exception
        //  when testing deleteUser.
        if (phoneNumber == null) return false;
        if (phoneNumber.equals("Unknown")) return true;
        return phoneNumber.matches("\\d{10}");
    }
}
