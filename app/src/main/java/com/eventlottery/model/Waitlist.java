package com.eventlottery.model;

import android.util.Log;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

/**
 * Manages the waitlist for a specific event.
 * Tracks attendees who are waiting to join an event and enforces capacity limits if set.
 * Automatically synchronizes changes with Firebase Firestore.
 */
public class Waitlist {
    private static final String TAG = "Waitlist";
    private static final String COLLECTION_NAME = "waitlists";

    private String eventId;
    private ArrayList<String> attendeeIds;
    private Integer waitlistLimit;
    private Integer waitlistCount;
    private String registrationDeadline;

    @Exclude
    private final FirebaseFirestore db;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Waitlist() {
        this.attendeeIds = new ArrayList<>();
        this.waitlistCount = 0;
        FirebaseFirestore tempDb;
        try {
            tempDb = FirebaseFirestore.getInstance();
        } catch (IllegalStateException e) {
            tempDb = null;
            Log.w(TAG, "Firebase not initialized, Firestore operations will be unavailable");
        }
        this.db = tempDb;
    }

    /**
     * Constructs a Waitlist with a specific capacity limit.
     *
     * @param eventId       The unique identifier of the event.
     * @param waitlistLimit The maximum number of attendees allowed on the waitlist.
     */
    public Waitlist(String eventId, Integer waitlistLimit) {
        this();
        this.eventId = eventId;
        this.waitlistLimit = waitlistLimit;
    }

    // Fixed with AI: This method was added by Gemini to fix build error in Event
    /**
     * Constructs a Waitlist with a specific capacity limit and registration deadline.
     *
     * @param eventId              The unique identifier of the event.
     * @param waitlistLimit       The maximum number of attendees allowed on the waitlist.
     * @param registrationDeadline The deadline for registration.
     */
    public Waitlist(String eventId, Integer waitlistLimit, String registrationDeadline) {
        this(eventId, waitlistLimit);
        this.registrationDeadline = registrationDeadline;
    }

    /**
     * Constructs an unlimited Waitlist for the given event.
     *
     * @param eventId The unique identifier of the event.
     */
    public Waitlist(String eventId) {
        this();
        this.eventId = eventId;
        this.waitlistLimit = null;
    }

    /**
     * Constructs a Waitlist with a specific FirebaseFirestore instance.
     * Useful for dependency injection in tests.
     * @param db The Firestore instance to use.
     */
    public Waitlist(FirebaseFirestore db) {
        this.attendeeIds = new ArrayList<>();
        this.waitlistCount = 0;
        this.db = db;
    }

    /**
     * Synchronizes the current state of the Waitlist object to Firebase.
     * Uses eventId as the document ID in the "waitlists" collection.
     */
    public void saveToFirebase() {
        if (db == null) return;
        if (eventId == null || eventId.isEmpty()) {
            Log.w(TAG, "Cannot save waitlist: eventId is null or empty");
            return;
        }
        db.collection(COLLECTION_NAME).document(eventId).set(this)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Waitlist successfully updated on Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating waitlist on Firebase", e));
    }

    /**
     * Pulls the latest data for this waitlist from Firebase using the eventId.
     * @param listener Callback for completion.
     */
    public void fetchFromFirebase(OnWaitlistLoadedListener listener) {
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
                    Waitlist remote = documentSnapshot.toObject(Waitlist.class);
                    if (remote != null) {
                        this.attendeeIds = remote.attendeeIds != null ? remote.attendeeIds : new ArrayList<>();
                        this.waitlistLimit = remote.waitlistLimit;
                        this.waitlistCount = remote.waitlistCount;
                        this.registrationDeadline = remote.registrationDeadline;
                        if (listener != null) listener.onSuccess();
                    } else if (listener != null) {
                        listener.onError(new Exception("Waitlist document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }

    public interface OnWaitlistLoadedListener {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Gets the unique identifier for the event.
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
     * Gets the list of attendee IDs currently on the waitlist.
     * @return An ArrayList of attendee ID strings.
     */
    public ArrayList<String> getAttendeeIds() {
        return attendeeIds;
    }

    /**
     * Sets the attendee IDs and updates Firebase.
     * @param attendeeIds The list of IDs.
     */
    public void setAttendeeIds(ArrayList<String> attendeeIds) {
        this.attendeeIds = attendeeIds;
        this.waitlistCount = attendeeIds != null ? attendeeIds.size() : 0;
        saveToFirebase();
    }

    /**
     * Gets the maximum number of attendees allowed on this waitlist.
     * @return The waitlist limit, or null if there is no limit.
     */
    public Integer getWaitlistLimit() {
        return waitlistLimit;
    }

    /**
     * Sets the waitlist limit and updates Firebase.
     * @param waitlistLimit The limit to set.
     */
    public void setWaitlistLimit(Integer waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
        saveToFirebase();
    }

    /**
     * Gets the current number of attendees on the waitlist.
     * @return The current waitlist count.
     */
    public Integer getWaitlistCount() {
        return waitlistCount;
    }

    /**
     * Sets the waitlist count. Required for Firestore.
     * @param waitlistCount The count to set.
     */
    public void setWaitlistCount(Integer waitlistCount) {
        this.waitlistCount = waitlistCount;
    }

    /**
     * Gets the registration deadline.
     * @return The registration deadline string.
     */
    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    /**
     * Sets the registration deadline and updates Firebase.
     * @param registrationDeadline The deadline to set.
     */
    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
        saveToFirebase();
    }

    /**
     * Checks if the waitlist has reached its capacity limit.
     * @return true if the waitlist is full, false if there is space or no limit.
     */
    public boolean isWaitlistFull() {
        if (waitlistLimit == null) {
            return false; // Unlimited waitlist
        }
        return waitlistCount >= waitlistLimit;
    }

    /**
     * Adds an attendee to the waitlist if it is not full and updates Firebase.
     * @param attendeeId The unique identifier of the attendee to add.
     * @throws IllegalStateException if the waitlist is already full.
     */
    public void addAttendee(String attendeeId) {
        if (isWaitlistFull()) {
            throw new IllegalStateException("Waitlist is full");
        } else if (!attendeeIds.contains(attendeeId)) {
            attendeeIds.add(attendeeId);
            waitlistCount = attendeeIds.size();
            saveToFirebase();
        }
    }

    /**
     * Removes an attendee from the waitlist and updates Firebase.
     * @param attendeeId The unique identifier of the attendee to remove.
     * @throws IllegalArgumentException if the attendee is not found in the waitlist.
     */
    public void removeAttendee(String attendeeId) {
        if (attendeeIds.remove(attendeeId)) {
            waitlistCount = attendeeIds.size();
            saveToFirebase();
        } else {
            throw new IllegalArgumentException("Attendee not found in waitlist");
        }
    }

    /**
     * Checks if a specific attendee is currently on the waitlist.
     * @param attendeeId The unique identifier of the attendee.
     * @return true if the attendee is on the waitlist, false otherwise.
     */
    public boolean findAttendee(String attendeeId) {
        return attendeeIds.contains(attendeeId);
    }
}
