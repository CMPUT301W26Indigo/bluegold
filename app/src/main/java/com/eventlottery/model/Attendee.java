package com.eventlottery.model;

import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an Attendee in the Event Lottery System.
 * Stores personal information, event history, and waitlist status.
 * Automatically synchronizes changes with Firebase Firestore.
 */
public class Attendee extends AbstractUser {
    private static final String TAG = "Attendee";
    private static final String COLLECTION_NAME = "attendees";

    // Fields like name, email, and phoneNumber are inherited from AbstractUser.
    // Declaring them here again causes shadowing and serialization issues.

    // These fields are preserved for location services
    private double latitude;
    private double longitude;
    
    // Inherited fields (name, email, phoneNumber, deviceID, notification) 
    // are NOT re-declared here to avoid shadowing issues during serialization.

    private ArrayList<AttendeeEventHistory> eventHistory;
    private ArrayList<String> waitListed;

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
                        // Populate inherited fields from the deserialized object
                        this.name = remote.getName();
                        this.email = remote.getEmail();
                        this.phoneNumber = remote.getPhoneNumber();
                        this.address = remote.getAddress();
                        this.notification = remote.getNotification();
                        this.fcmToken = remote.getFcmToken();
                        this.profileImageUrl = remote.getProfileImageUrl();
                        
                        this.latitude = remote.latitude;
                        this.longitude = remote.longitude;

                        this.eventHistory = remote.eventHistory != null ? remote.eventHistory : new ArrayList<>();
                        // Re-attach listeners to loaded history objects
                        for (AttendeeEventHistory history : this.eventHistory) {
                            history.setOnChangeListener(this::saveToFirebase);
                        }
                        this.waitListed = remote.waitListed != null ? remote.waitListed : new ArrayList<>();

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
     * Adds an event to the attendee's personal waitlist and updates Firebase.
     * @param eventID The unique identifier of the event.
     */
    public void joinWaitList(String eventID) {
        // should update event history as well??
        Map<String, Object> data = new HashMap<>();
        data.put("status", "waiting");

        db.collection("attendees").document(getID())
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

    public void setWaitListed(ArrayList<String> waitListed) { this.waitListed = waitListed;}

    /**
     * Sets the list of events the attendee has participated in.
     * @param eventHistory
     */
    public void setEventHistory(ArrayList<AttendeeEventHistory> eventHistory) {
        this.eventHistory = eventHistory;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Checks if attendee profile is complete enough to join events
     */
    @Exclude
    public boolean isProfileComplete() {
        return name != null && !name.trim().isEmpty()
                && email != null && !email.trim().isEmpty()
                && phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

}
