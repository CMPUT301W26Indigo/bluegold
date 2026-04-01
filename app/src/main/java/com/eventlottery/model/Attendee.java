package com.eventlottery.model;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;

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
    private String deviceID;
    private ArrayList<AttendeeEventHistory> eventHistory;
    private ArrayList<String> waitListed;
    private boolean notification;

    @Exclude
    private final FirebaseFirestore db;

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
            Log.w(TAG, "Firebase not initialized, Firestore operations will be unavailable");
        }
        this.db = tempDb;
    }

    /**
     * Synchronizes the current state of the Attendee object to Firebase.
     * Only works if deviceID is set.
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
     * Pulls the latest data for this attendee from Firebase using the deviceID.
     * @param listener Callback for completion.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fetchFromFirebase(OnUserLoadedListener<? extends AbstractUser> listener) {
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
                        if (listener != null) ((OnUserLoadedListener<Attendee>)listener).onSuccess(this);
                    } else if (listener != null) {
                        listener.onError(new Exception("Attendee document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }

    @Override
    public String getEmail() { return email; }

    @Override
    public void setEmail(String email) {
        if (ValidateEmail.isValidEmail(email)) {
            this.email = email;
            saveToFirebase();
        } else {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    @Override
    public String getName() { return name; }

    @Override
    public void setName(String name) {
        this.name = name;
        saveToFirebase();
    }

    @Override
    public String getPhoneNumber() { return phoneNumber; }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        if (ValidatePhone.isValidPhoneNumber(phoneNumber)) {
            this.phoneNumber = phoneNumber;
            saveToFirebase();
        } else {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }

    @Override
    public String getAttendeeID() { return deviceID; }

    @Override
    public void setAttendeeID(String attendeeID) { this.deviceID = attendeeID; }

    @Override
    public String getAddress() { return address; }

    @Override
    public void setAddress(String address) {
        this.address = address;
        saveToFirebase();
    }

    public void joinWaitList(String eventID) {
        if (!waitListed.contains(eventID)) {
            waitListed.add(eventID);
            saveToFirebase();
        }
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

    public void setNotification(boolean notification) {
        this.notification = notification;
        saveToFirebase();
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
}
