package com.eventlottery.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * Represents an Attendee in the Event Lottery System.
 * Stores personal information, event history, and waitlist status.
 * Automatically synchronizes changes with Firebase Firestore.
 */
public class Attendee extends AbstractUser {
    private static final String TAG = "Attendee";
    private static final String COLLECTION_NAME = "attendees";

    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private double latitude;
    private double longitude;
    private String deviceID;
    private ArrayList<AttendeeEventHistory> eventHistory;
    private ArrayList<String> waitListed;
    private boolean notification;

    @Exclude
    private final FirebaseFirestore db;

    /**
     * Interface for handling asynchronous attendee loading from Firebase.
     */
    public interface OnAttendeeLoadedListener {
        void onSuccess(Attendee attendee);
        void onError(Exception e);
    }

    /**
     * Constructs a new Attendee with default values.
     * Initializes empty lists for event history and waitlists and connects to Firestore.
     */
    public Attendee() {
        super();
        this.notification = true;
        this.eventHistory = new ArrayList<AttendeeEventHistory>();
        this.waitListed = new ArrayList<String>();
        
        FirebaseFirestore tempDb = null;
        try {
            tempDb = FirebaseFirestore.getInstance();
        } catch (IllegalStateException e) {
            tempDb = null;
            Log.w(TAG, "Firebase not initialized, Firestore operations will be unavailable");
        }
        this.db = tempDb;
    }

    /**
     * Synchronizes the current state of the Attendee object to Firebase.
     * Only works if deviceID is set.
     */
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
     * Pulls the latest data for this attendee from Firebase using the deviceID.
     * @param listener Callback for completion.
     */
    public void fetchFromFirebase(OnAttendeeLoadedListener listener) {
        if (db == null) {
            if (listener != null) listener.onError(new Exception("Firebase not initialized"));
            return;
        }
        if (deviceID == null || deviceID.isEmpty()) {
            if (listener != null) listener.onError(new Exception("DeviceID not set"));
            return;
        }
        db.collection(COLLECTION_NAME).document(deviceID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Attendee remote = documentSnapshot.toObject(Attendee.class);
                    if (remote != null) {
                        this.name = remote.name;
                        this.email = remote.email;
                        this.phoneNumber = remote.phoneNumber;
                        this.address = remote.address;
                        this.eventHistory = remote.eventHistory != null ? remote.eventHistory : new ArrayList<>();
                        // Re-attach listeners to loaded history objects
                        for (AttendeeEventHistory history : this.eventHistory) {
                            history.setOnChangeListener(this::saveToFirebase);
                        }
                        this.waitListed = remote.waitListed != null ? remote.waitListed : new ArrayList<>();
                        this.notification = remote.notification;
                        if (listener != null) listener.onSuccess(this);
                    } else if (listener != null) {
                        listener.onError(new Exception("Attendee document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }

    /**
     * Adds an event to the attendee's personal waitlist.
     * Gets the attendee's email address.
     * @return The email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the attendee's email address after validation and updates Firebase.
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
     * Sets the attendee's name and updates Firebase.
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
     * Sets the attendee's phone number after validation and updates Firebase.
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
     * Gets the attendee's unique ID.
     * @return The attendee ID.
     */
    public String getAttendeeID() {
        return deviceID;
    }


    /**
     * Adds an event to the attendee's personal waitlist and updates Firebase.
     * @param eventID The unique identifier of the event.
     */
    public void joinWaitList(String eventID) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "waiting");

        db.collection("attendees").document(getAttendeeID())
                .collection("waitListed").document(eventID)
                .set(data);
    }

    /**
     * Adds an event to the attendee's history and updates Firebase.
     * Sets up a listener so that attendance status updates are also synced.
     * @param eventID The unique identifier of the event.
     */
    public void addEventToHistory(String eventID) {
        AttendeeEventHistory event = new AttendeeEventHistory(eventID);
        event.setOnChangeListener(this::saveToFirebase);
        eventHistory.add(event);
        saveToFirebase();
    }

    /**
     * Removes an event from the attendee's waitlist and updates Firebase.
     * @param eventID The unique identifier of the event.
     */
    public void leaveWaitList(String eventID) {
        if (waitListed.remove(eventID)) {
            saveToFirebase();
        }
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


    /**
     * Gets the attendee's physical address.
     * @return The address string.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the attendee's physical address and updates Firebase.
     * @param address The address to set.
     */
    public void setAddress(String address) {
        this.address = address;
        saveToFirebase();
    }

    /**
     * Sets the list of events the attendee has participated in.
     * @param eventHistory
     */
    public void setEventHistory(ArrayList<AttendeeEventHistory> eventHistory) {
        this.eventHistory = eventHistory;
    }

    /**
     * Sets the attendee's ID. Required for tests or loading.
     * @param attendeeID
     */
    public void setAttendeeID(String attendeeID) {
        this.deviceID = attendeeID;
    }
}
