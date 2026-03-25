package com.eventlottery.model;

/**
 * Represents the history of an attendee's participation in a specific event.
 * Stores the unique identifier for the event and the attendance status.
 */
public class AttendeeEventHistory {
    private String eventID;
    private boolean attended;

    /**
     * Default no-argument constructor required for Firebase Firestore deserialization.
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
     * @return The event ID.
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Sets the unique identifier for the event.
     * @param eventID The event ID.
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
     * Sets whether the attendee attended the event.
     * @param attended true if the attendee attended, false otherwise.
     */
    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    /**
     * Updates the attendance status to true, marking the attendee as having attended.
     */
    public void updateAttendance() {
        this.attended = true;
    }

}
