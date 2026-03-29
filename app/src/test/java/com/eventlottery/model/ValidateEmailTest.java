package com.eventlottery.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidateEmailTest {

    @Test
    public void testValidEmails() {
        assertTrue("Basic email should be valid", ValidateEmail.isValidEmail("test@example.com"));
        assertTrue("Email with dots should be valid", ValidateEmail.isValidEmail("first.last@domain.co.uk"));
        assertTrue("Email with plus should be valid", ValidateEmail.isValidEmail("user+extra@gmail.com"));
        assertTrue("Email with numbers should be valid", ValidateEmail.isValidEmail("user123@provider.net"));
    }

    @Test
    public void testInvalidEmails() {
        assertFalse("Missing @ should be invalid", ValidateEmail.isValidEmail("testexample.com"));
        assertFalse("Missing domain should be invalid", ValidateEmail.isValidEmail("test@"));
        assertFalse("Missing user should be invalid", ValidateEmail.isValidEmail("@example.com"));
        assertFalse("Multiple @ should be invalid", ValidateEmail.isValidEmail("test@@example.com"));
        assertFalse("Missing TLD should be invalid", ValidateEmail.isValidEmail("test@example"));
        assertFalse("Spaces should be invalid", ValidateEmail.isValidEmail("test @example.com"));
        assertFalse("Null should be invalid", ValidateEmail.isValidEmail(null));
        assertFalse("Empty string should be invalid", ValidateEmail.isValidEmail(""));
    }
}
