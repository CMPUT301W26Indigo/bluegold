package com.eventlottery.model;

import com.google.firebase.firestore.Exclude;

/**
 * Represents the history of an attendee's participation in a specific event.
 * Stores the unique identifier for the event and the attendance status.
 */
public class AttendeeEventHistory {
    private String eventID;
    private boolean attended;

    @Exclude
    private OnChangeListener listener;

    /**
     * Interface to notify parent objects of changes.
     */
    public interface OnChangeListener {
        /**
         * Called when the attendance status or event information changes.
         */
        void onChanged();
    }

    /**
     * Default constructor required for Firestore deserialization.
     */
    public AttendeeEventHistory() {
    }

    /**
     * Constructs a new AttendeeEventHistory for the given event.
     * Initial attendance status is set to false.
     * @param eventID The unique identifier of the event.
     */
    public AttendeeEventHistory(String eventID) {
        this.eventID = eventID;
        this.attended = false;
    }

    /**
     * Gets the unique identifier for the event.
     * @return The event ID string.
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Sets the event ID. Required for Firestore.
     * @param eventID The event ID to set.
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Checks whether the attendee attended the event.
     * @return true if the attendee attended, false otherwise.
     */
    public boolean isAttended() {
        return attended;
    }

    /**
     * Sets the attendance status. Required for Firestore.
     * @param attended The attendance status to set.
     */
    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    /**
     * Sets a listener to be notified of changes to this history item.
     * @param listener The listener to notify.
     */
    @Exclude
    public void setOnChangeListener(OnChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the attendance status to true and notifies the listener to sync with Firebase.
     */
    public void updateAttendance() {
        this.attended = true;
        if (listener != null) {
            listener.onChanged();
        }
    }
}
