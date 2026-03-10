package com.eventlottery;
import org.apache.commons.validator.routines.EmailValidator;

public class ValidateEmail {
    private String email;

    public ValidateEmail(String email) {
        this.email = email;
    }

    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
