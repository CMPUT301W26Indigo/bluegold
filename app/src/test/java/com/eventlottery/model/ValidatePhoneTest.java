package com.eventlottery.data.models;

import org.junit.Test;
import static org.junit.Assert.*;
import com.eventlottery.model.ValidatePhone;

import com.eventlottery.model.ValidatePhone;


import com.eventlottery.model.ValidatePhone;

public class ValidatePhoneTest {

    @Test
    public void testValidPhoneNumber() {
        assertTrue("A 10-digit  phone number should be valid", ValidatePhone.isValidPhoneNumber("1234567890"));
    }

    @Test
    public void testInvalidPhoneNumbers() {
        assertFalse("Too few digits should be invalid", ValidatePhone.isValidPhoneNumber("123456789"));
        assertFalse("Too many digits should be invalid", ValidatePhone.isValidPhoneNumber("12345678901"));
        assertFalse("Non-numeric characters should be invalid", ValidatePhone.isValidPhoneNumber("123-456-7890"));
        assertFalse("Spaces should be invalid", ValidatePhone.isValidPhoneNumber("123 456 7890"));
        assertFalse("Empty string should be invalid", ValidatePhone.isValidPhoneNumber(""));
    }

    @Test(expected = NullPointerException.class)
    public void testNullPhoneNumber() {
        // The current implementation uses phoneNumber.matches() which will throw NPE if null
        ValidatePhone.isValidPhoneNumber(null);
    }
}
