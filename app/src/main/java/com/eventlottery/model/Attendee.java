package com.eventlottery.model;

import android.content.Context;
import android.provider.Settings;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;

/**
 * Represents an Attendee in the Event Lottery System.
 * Stores personal information, event history, and waitlist status.
 */
public class Attendee {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String deviceID;
    private ArrayList<AttendeeEventHistory> eventHistory;
    private ArrayList<String> waitListed;
    private boolean notification;

    /**
     * Constructs a new Attendee with default values.
     * Initializes empty lists for event history and waitlists.
     */
    public Attendee() {
        this.name = null;
        this.email = null;
        this.phoneNumber = null;
        this.address = null;
        this.deviceID = null;
        this.notification = true;
        this.eventHistory = new ArrayList<AttendeeEventHistory>();
        this.waitListed = new ArrayList<String>();
    }

    /**
     * Gets the attendee's email address.
     * @return The email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the attendee's email address after validation.
     * @param email The email address to set.
     * @throws IllegalArgumentException if the email format is invalid.
     */
    public void setEmail(String email) {
        if (ValidateEmail.isValidEmail(email)) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Gets the attendee's name.
     * @return The name of the attendee.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the attendee's name.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the attendee's phone number.
     * @return The phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the attendee's phone number after validation.
     * @param phoneNumber The phone number to set.
     * @throws IllegalArgumentException if the phone number format is invalid.
     */
    public void setPhoneNumber(String phoneNumber) {
        if (ValidatePhone.isValidPhoneNumber(phoneNumber)) {
            this.phoneNumber = phoneNumber;
        } else {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }

    /**
     * Retrieves the unique Android device ID for this app installation.
     * Source - https://stackoverflow.com/a/60505449
     * Posted by Rahul Samaddar
     * Retrieved 2026-03-09, License - CC BY-SA 4.0
     * @param context The application context.
     * @return The unique Android ID string.
     */
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Asynchronously retrieves the unique Firebase Installation ID.
     * This ID is unique to the app installation on the device and remains consistent
     * unless the app is uninstalled or the device is factory reset.
     *
     * @return A Task that will resolve to the Firebase Installation ID.
     */
    public static Task<String> getFirebaseId() {
        return FirebaseInstallations.getInstance().getId();
    }

    /**
     * Gets the attendee's unique ID (typically the device ID or Firebase ID).
     * @return The attendee ID.
     */
    public String getAttendeeID() {
        return deviceID;
    }

    /**
     * Adds an event to the attendee's personal waitlist.
     * @param eventID The unique identifier of the event.
     */
    public void joinWaitList(String eventID) {
        waitListed.add(eventID);
    }

    /**
     * Adds an event to the attendee's history of participated events.
     * @param eventID The unique identifier of the event.
     */
    public void addEventToHistory(String eventID) {
        AttendeeEventHistory event = new AttendeeEventHistory(eventID);
        eventHistory.add(event);
    }

    /**
     * Removes an event from the attendee's waitlist.
     * @param eventID The unique identifier of the event.
     */
    public void leaveWaitList(String eventID) {
        waitListed.remove(eventID);
    }

    /**
     * Gets the list of events the attendee has a history with.
     * @return An ArrayList of AttendeeEventHistory objects.
     */
    public ArrayList<AttendeeEventHistory> getEventHistory() {
        return eventHistory;
    }

    /**
     * Gets the list of event IDs the attendee is currently waitlisted for.
     * @return An ArrayList of event ID strings.
     */
    public ArrayList<String> getWaitListed() {
        return waitListed;
    }

    /**
     * Sets the notification preference for the attendee.
     * @param notification True to enable notifications, false to disable.
     */
    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    /**
     * Gets the notification preference for the attendee.
     * @return True if notifications are enabled, false otherwise.
     */
    public boolean getNotification() {
        return notification;
    }

    /**
     * Gets the attendee's physical address.
     * @return The address string.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the attendee's physical address.
     * @param address The address to set.
     * @todo Throw IllegalArgumentException for invalid format and ensure it can be converted to coordinates.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Sets the attendee's unique ID.
     * @param id The ID to set (e.g., the Firebase Installation ID).
     */
    public void setAttendeeID(String id) {
        this.deviceID = id;
    }
}
