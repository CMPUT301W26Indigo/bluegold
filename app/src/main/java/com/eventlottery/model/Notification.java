package com.eventlottery.model;

/**
 * Represents a notification sent to an attendee.
 */
public class Notification {
    private String id;
    private String message;
    private String attendeeId;
    private String eventId;

    /**
     * Default constructor required for Firestore serialization.
     */
    public Notification() {
    }

    public Notification(String message, String attendeeId, String eventId) {
        this.message = message;
        this.attendeeId = attendeeId;
        this.eventId = eventId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(String attendeeId) {
        this.attendeeId = attendeeId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void sendNotification() {
        // Send the notification to the user
    }
}
