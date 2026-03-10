package com.eventlottery;

public class ValidatePhone {
    private String phoneNumber;

    public ValidatePhone(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}");
    }
}
