package com.eventlottery.data.models;
import android.content.Context;
import android.provider.Settings;

import java.util.ArrayList;

public class Attendee {
    private String name;
    private String email;
    private String phoneNumber;
    private String location;
    private String deviceID;
    private ArrayList<AttendeeEventHistory> eventHistory;
    private ArrayList<String> waitListed;

    public Attendee() {
        this.name = null;
        this.email = null;
        this.phoneNumber = null;
        this.location = null;
        this.deviceID = null;

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

    // Source - https://stackoverflow.com/a/60505449
    // Posted by Rahul Samaddar
    // Retrieved 2026-03-09, License - CC BY-SA 4.0

    public static String getDeviceId(Context context) {
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return id;
    }

    public String getAttendeeID() {
        return deviceID;
    }

    public void joinWaitList(String eventID) {
        waitListed.add(eventID);
    }

    public void addEventToHistory(String eventID) {
        AttendeeEventHistory event = new AttendeeEventHistory(eventID);
        eventHistory.add(event);
    }

    public void leaveWaitList(String eventID) {
        waitListed.remove(eventID);
    }

    public ArrayList<AttendeeEventHistory> getEventHistory() {
        return eventHistory;
    }

    public ArrayList<String> getWaitListed() {
        return waitListed;
    }




}
