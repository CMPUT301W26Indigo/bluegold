package com.eventlottery.model;
import org.apache.commons.validator.routines.EmailValidator;

public class ValidateEmail {
    private String email;

    public ValidateEmail(String email) {
        this.email = email;
    }

    public static boolean isValidEmail(String email) {
        // If statement added by Gemini to fix issue with null pointer exception
        //  when testing deleteUser.
        if (email == null) return false;
        // Code inspired from this website
        // https://commons.apache.org/proper/commons-validator/
        return EmailValidator.getInstance().isValid(email);
    }
}
