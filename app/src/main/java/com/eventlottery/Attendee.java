package com.eventlottery;
import com.eventlottery.ValidateEmail;

public class Attendee {
    private String name;
    private String email;
    private String phoneNumber;
    private String location;

    public Attendee() {
        this.name = null;
        this.email = null;
        this.phoneNumber = null;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        ValidateEmail validateEmail = new ValidateEmail(email);
        if (validateEmail.isValidEmail(email)) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        ValidatePhone validatePhone = new ValidatePhone(phoneNumber);
        if (validatePhone.isValidPhoneNumber(phoneNumber)) {
            this.phoneNumber = phoneNumber;
        } else {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }
}
