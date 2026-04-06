package com.eventlottery.model;

import android.content.Context;
import android.provider.Settings;

import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;

public abstract class AbstractUser {
    protected String name;
    protected String email;
    protected String phoneNumber;
    protected String address;
    protected String deviceID;
    protected String profileImageUrl;
    protected String fcmToken;
    protected boolean isAdmin;
    protected boolean notification;


    public AbstractUser() {
        this.name = null;
        this.email = null;
        this.phoneNumber = null;
        this.address = null;
        this.deviceID = null;
        this.fcmToken = null;
        this.isAdmin = false;
        this.notification = true;
    }

    // Getters and Setters
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
            saveToFirebase();
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
        saveToFirebase();
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
            saveToFirebase();
        } else {
            throw new IllegalArgumentException("Invalid phone number format");
        }
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
     */
    public void setAddress(String address) {
        this.address = address;
        saveToFirebase();
    }

    /**
     * Gets the attendee's unique ID (typically the device ID or Firebase ID).
     * @return The attendee ID.
     */
    public String getID() {
        return deviceID;
    }

    /**
     * Sets the attendee's unique ID.
     * @param id The ID to set (e.g., the Firebase Installation ID).
     */
    public void setID(String id) {
        this.deviceID = id;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        saveToFirebase();
    }

    /**
     * Sets the notification preference and updates Firebase.
     * @param notification True to enable notifications, false to disable.
     */
    public void setNotification(boolean notification) {
        this.notification = notification;
        saveToFirebase();
    }

    /**
     * Gets the notification preference for the attendee.
     * @return True if notifications are enabled, false otherwise.
     */
    public boolean getNotification() {
        return notification;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * @deprecated Use {@link #getFirebaseId()} for consistent cross-device/install identification.
     * Retrieves the unique Android device ID for this app installation.
     *
     * @param context The application context.
     * @return The unique Android ID string.
     */
    @Deprecated
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

    public abstract void saveToFirebase();
}
