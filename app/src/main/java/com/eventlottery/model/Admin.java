package com.eventlottery.model;

import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

public class Admin extends AbstractUser {
    private static final String TAG = "Admin";
    private static final String COLLECTION_NAME = "admins";

    private Attendee attendee;
    private EventOrganizer eventOrganizer;
    @Exclude
    private final FirebaseFirestore db;


    public interface OnAdminLoadedListener {
        void onSuccess(Admin admin);
        void onError(Exception e);
    }

    /**
     * Constructs a new Admin with default values and connects to Firestore.
     */
    public Admin() {
        super();
        this.attendee = null;
        this.eventOrganizer = null;
        this.isAdmin = true;

        FirebaseFirestore tempDb = null;
        try {
            tempDb = FirebaseFirestore.getInstance();
        } catch (IllegalStateException e) {
            tempDb = null;
            Log.w(TAG, "Firebase not initialized, Firestore operations will be unavailable");
        }
        this.db = tempDb;

    }

    // Getters and Setters

    /**
     * Sets the attendee's name.
     * Updates the attendee and event organizer's names as well.
     * Saves to Firestore
     * @param name The name to set.
     */
    @Override
    public void setName(String name) {
        this.name = name;
        if (attendee != null) attendee.setName(name);
        if (eventOrganizer != null) eventOrganizer.setName(name);
        saveToFirebase();
    }

    /**
     * Sets the attendee's email address.
     * Updates the attendee and event organizer's emails as well.
     * Saves to Firestore
     * @param email The email address to set.
     */
    @Override
    public void setEmail(String email) {
        if (!ValidateEmail.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email;
        if (attendee != null) attendee.setEmail(email);
        if (eventOrganizer != null) eventOrganizer.setEmail(email);
        saveToFirebase();
    }

    /**
     * Sets the attendee's phone number.
     * Updates the attendee and event organizer's phone numbers as well.
     * Saves to Firestore
     * @param phoneNumber The phone number to set.
     */
    @Override
    public void setPhoneNumber(String phoneNumber) {
        if (!ValidatePhone.isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.phoneNumber = phoneNumber;
        if (attendee != null) attendee.setPhoneNumber(phoneNumber);
        if (eventOrganizer != null) eventOrganizer.setPhoneNumber(phoneNumber);
        saveToFirebase();
    }

    /**
     * Sets the attendee's physical address.
     * Updates the attendee and event organizer's addresses as well.
     * Saves to Firestore
     * @param address The address to set.
     * Todo Throw IllegalArgumentException for invalid format and ensure it can be converted to coordinates.
     */
    @Override
    public void setAddress(String address) {
        this.address = address;
        if (attendee != null) attendee.setAddress(address);
        if (eventOrganizer != null) eventOrganizer.setAddress(address);
        saveToFirebase();
    }

    /**
     * Sets the attendee's unique ID.
     * Updates the attendee and event organizer's IDs as well.
     * Saves to Firestore
     * @param deviceID The ID to set (e.g., the Firebase Installation ID).
     */
    @Override
    public void setID(String deviceID) {
        this.deviceID = deviceID;
        if (attendee != null) attendee.setID(deviceID);
        if (eventOrganizer != null) eventOrganizer.setID(deviceID);
        //saveToFirebase(); - needed??
    }

    /**
     * Sets the attendee's profile image URL.
     * Updates the attendee and event organizer's profile image URLs as well.
     * Saves to Firestore
     * @param profileImageUrl
     */
    @Override
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        if (attendee != null) attendee.setProfileImageUrl(profileImageUrl);
        if (eventOrganizer != null) eventOrganizer.setProfileImageUrl(profileImageUrl);
        saveToFirebase();
    }

    /**
     * Sets the attendee's notification preference.
     * Updates the attendee and event organizer's notification preferences as well.
     * Saves to Firestore
     * @param notification True to enable notifications, false to disable.
     */
    @Override
    public void setNotification(boolean notification) {
        this.notification = notification;
        if (attendee != null) attendee.setNotification(notification);
        if (eventOrganizer != null) eventOrganizer.setNotification(notification);
        saveToFirebase();
    }

    /**
     * Returns if the user is an admin boolean.
     * @return isAdmin.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Gets the attendee associated with this admin.
     * @return attendee
     */
    public Attendee getAttendee() {
        return attendee;
    }

    /**
     * Sets the attendee associated with this admin.
     * @param attendee
     */
    public void setAttendee(Attendee attendee) {
        this.attendee = attendee;
    }

    /**
     * Gets the event organizer associated with this admin.
     * @return
     */
    public EventOrganizer getEventOrganizer() {
        return eventOrganizer;
    }

    /**
     * Sets the event organizer associated with this admin.
     * @param eventOrganizer
     */
    public void setEventOrganizer(EventOrganizer eventOrganizer) {
        this.eventOrganizer = eventOrganizer;
    }

    /**
     * Creates and returns an attendee object for admin to use if one doesn't already exist
     * @return attendee
     */
    public Attendee createAttendee() {
        if (attendee == null) {
            attendee = new Attendee();
            attendee.setID(this.deviceID);
            attendee.setName(this.name);
            attendee.setEmail(this.email);
            attendee.setPhoneNumber(this.phoneNumber);
            attendee.setAddress(this.address);
            saveToFirebase();
        }
        return attendee;
    }

    /**
     * Creates and returns an event organizer object for admin to use if one doesn't already exist
     * @return eventOrganizer
     */
    public EventOrganizer createEventOrganizer() {
        if (eventOrganizer == null) {
            eventOrganizer = new EventOrganizer();
            eventOrganizer.setID(this.deviceID);
            eventOrganizer.setName(this.name);
            eventOrganizer.setEmail(this.email);
            eventOrganizer.setPhoneNumber(this.phoneNumber);
            eventOrganizer.setAddress(this.address);
            saveToFirebase();
        }
        return eventOrganizer;
    }

    /**
     * Saves the admin to Firebase.
     */
    @Override
    public void saveToFirebase() {
        if (db == null) return;
        if (deviceID == null || deviceID.isEmpty()) {
            Log.w(TAG, "Cannot save attendee: deviceID is null or empty");
            return;
        }
        db.collection(COLLECTION_NAME).document(deviceID).set(this)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Attendee successfully updated on Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating attendee on Firebase", e));
    }

    /**
     * Fetches the admin from Firebase.
     * @param listener Callback for completion.
     */
    public void fetchFromFirebase(OnAdminLoadedListener listener) {

    }




}
