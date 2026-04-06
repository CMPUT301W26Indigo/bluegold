package com.eventlottery.model;

import android.util.Log;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages the list of attendees for a specific event.
 * Handles attendee registration, status tracking, and capacity limits.
 * Automatically synchronizes changes with Firebase Firestore.
 */
public class GuestList {
    private static final String TAG = "GuestList";
    private static final String COLLECTION_NAME = "guestlists";

    private String eventId;
    private HashMap<String, String> attendees; // <attendeeId, status>
    // Statuses: maybe, invited, confirmed, declined, cancelled
    private Integer listCount;
    private Integer listLimit;

    @Exclude
    private final FirebaseFirestore db;

    /**
     * Default no-argument constructor required for Firebase Firestore deserialization.
     */
    public GuestList() {
        this.attendees = new HashMap<>();
        this.listCount = 0;
        this.listLimit = null;
        FirebaseFirestore dbInstance = null;
        try {
            dbInstance = FirebaseFirestore.getInstance();
        } catch (IllegalStateException e) {
            dbInstance = null;
            Log.w(TAG, "Firebase not initialized, Firestore operations will be unavailable");
        }
        this.db = dbInstance;
    }

    /**
     * Constructs a GuestList with a specific capacity limit.
     *
     * @param eventId   The unique identifier of the event.
     * @param listLimit The maximum number of attendees allowed.
     */
    public GuestList(String eventId, Integer listLimit) {
        this();
        this.eventId = eventId;
        this.attendees = new HashMap<String, String>();
        this.listCount = 0;
        this.listLimit = listLimit;
    }

    /**
     * Constructs a GuestList without a capacity limit.
     *
     * @param eventId The unique identifier of the event.
     */
    public GuestList(String eventId) {
        this();
        this.eventId = eventId;
        this.attendees = new HashMap<String, String>();
        this.listCount = 0;
        this.listLimit = null;
    }

    public GuestList(FirebaseFirestore db) {
        this.db = db;
        this.attendees = new HashMap<>();
        this.listCount = 0;
        this.listLimit = null;
    }

    /**
     * Synchronizes the current state of the GuestList object to Firebase.
     * Uses eventId as the document ID in the "guestlists" collection.
     */
    public void saveToFirebase() {
        if (db == null) return;
        if (eventId == null || eventId.isEmpty()) {
            Log.w(TAG, "Cannot save guest list: eventId is null or empty");
            return;
        }
        db.collection(COLLECTION_NAME).document(eventId).set(this)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "GuestList successfully updated on Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating guest list on Firebase", e));
    }

    /**
     * Pulls the latest data for this guest list from Firebase using the eventId.
     * @param listener Callback for completion.
     */
    public void fetchFromFirebase(OnGuestListLoadedListener listener) {
        if (db == null) {
            if (listener != null) listener.onError(new Exception("Firebase not initialized"));
            return;
        }
        if (eventId == null || eventId.isEmpty()) {
            if (listener != null) listener.onError(new Exception("EventId not set"));
            return;
        }
        db.collection(COLLECTION_NAME).document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    GuestList remote = documentSnapshot.toObject(GuestList.class);
                    if (remote != null) {
                        this.attendees = remote.attendees != null ? remote.attendees : new HashMap<>();
                        this.listCount = remote.listCount;
                        this.listLimit = remote.listLimit;
                        if (listener != null) listener.onSuccess();
                    } else if (listener != null) {
                        listener.onError(new Exception("GuestList document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }

    public interface OnGuestListLoadedListener {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Gets the unique identifier for the event.
     *
     * @return The event ID string.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the event ID. Required for Firestore.
     * @param eventId The event ID to set.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the list of attendees and their current statuses.
     * Each entry is a map where the key is the attendee ID and the value is their status.
     *
     * @return A HashMap representing attendees where the key is attendee ID and value is status.
     */
    public HashMap<String, String> getAttendees() {
        return attendees;
    }

    /**
     * Sets the list of attendees and their current statuses.
     *
     * @param attendees A HashMap representing attendees.
     */
    public void setAttendees(HashMap<String, String> attendees) {
        this.attendees = attendees;
    }

    /**
     * Gets the current number of attendees in the list.
     *
     * @return The current count of attendees.
     */
    public Integer getListCount() {
        return listCount;
    }

    /**
     * Sets the list count. Required for Firestore.
     * @param listCount The count to set.
     */
    public void setListCount(Integer listCount) {
        this.listCount = listCount;
    }

    /**
     * Gets the maximum number of attendees allowed for this list.
     *
     * @return The capacity limit, or null if no limit is set.
     */
    public Integer getListLimit() {
        return listLimit;
    }

    /**
     * Sets the list limit and updates Firebase.
     * @param listLimit The limit to set.
     */
    public void setListLimit(Integer listLimit) {
        this.listLimit = listLimit;
        saveToFirebase();
    }

    /**
     * Adds a new attendee to the list with a default status of "maybe".
     * Increments the attendee count and updates Firebase.
     *
     * @param attendeeId The unique identifier of the attendee to add.
     */
    public void addGuestAttendee(String attendeeId) {
        attendees.put(attendeeId, "maybe");
        listCount++;
    }

    /**
     * Finds the attendee with the given ID in the list.
     *
     * @param attendeeId
     * @return status of attendee or null if none is found
     */
    public String findAttendee(String attendeeId) {
        return attendees.get(attendeeId);
    }

    /**
     * Updates the status of an existing attendee in the list and updates Firebase.
     *
     * @param attendeeId The unique identifier of the attendee.
     * @param status     The new status to assign (e.g., "accepted", "declined").
     */
    public void changeAttendeeStatus(String attendeeId, String status) {
        String currentStatus = attendees.get(attendeeId);
        if (currentStatus == null) {
            throw new IllegalArgumentException("Attendee with ID " + attendeeId + " is not in the list.");
        } else {
            attendees.put(attendeeId, status);
            saveToFirebase();
        }
    }

    /**
     * Removes an attendee from the guest list and updates Firebase.
     *
     * @param attendeeId The unique identifier of the attendee to remove.
     */
    public void removeAttendee(String attendeeId) {
        if (attendees.remove(attendeeId) != null) {
            listCount = attendees.size();
            saveToFirebase();
        }
    }

    /**
     * Change all entrants who did not sign up for the event to the cancelled status
     * Cancelled attendees are those who have the declined and maybe statuses.
     */
    public void cancelEntrants() {
        ArrayList<String> toCancel = new ArrayList<>();
        attendees.forEach((attendeeId, status) -> {
            Boolean b = status.equals("declined") || status.equals("maybe") ? toCancel.add(attendeeId) : null;
        });

        for (String attendeeId : toCancel) {
            changeAttendeeStatus(attendeeId, "cancelled");
        }
    }

    /**
     * Creates and returns a list of attendee IDs.
     *
     * @return ArrayList of just attendee IDs
     */
    public ArrayList<String> getAttendeeIds() {
        return new ArrayList<>(attendees.keySet());
    }

    /**
     * Returns the status of an attendee in the guest list.
     *
     * @param attendeeId The attendee ID
     * @return The attendee status (maybe, invited, confirmed, declined, cancelled)
     *         or null if the attendee is not on the guest list
     */
    public String getAttendeeStatus(String attendeeId) {
        if (attendeeId == null || attendees == null) {
            return null;
        }
        return attendees.get(attendeeId);
    }

    public boolean isGuestlistFull(int capacity) {
        return getListCount() >= capacity;
    }
}
