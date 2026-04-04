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
    @Override
    public void setName(String name) {
        this.name = name;
        if (attendee != null) attendee.setName(name);
        if (eventOrganizer != null) eventOrganizer.setName(name);
        saveToFirebase();
    }

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

    @Override
    public void setAddress(String address) {
        this.address = address;
        if (attendee != null) attendee.setAddress(address);
        if (eventOrganizer != null) eventOrganizer.setAddress(address);
        saveToFirebase();
    }

    @Override
    public void setID(String deviceID) {
        this.deviceID = deviceID;
        if (attendee != null) attendee.setID(deviceID);
        if (eventOrganizer != null) eventOrganizer.setID(deviceID);
        //saveToFirebase(); - needed??
    }

    @Override
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    @Override
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isNotification() {
        return notification;
    }

    @Override
    public void setNotification(boolean notification) {
        this.notification = notification;
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




}
