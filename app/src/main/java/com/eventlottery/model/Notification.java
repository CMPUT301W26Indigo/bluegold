package com.eventlottery.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Represents a notification sent to an attendee.
 * Supports different types of notifications, including invitations
 * that can be accepted or declined.
 */
public class Notification {
    private String id;
    private String message;
    private String attendeeId;
    private String eventId;
    private String type; // e.g., "INVITATION", "INFO"
    private String status; // e.g., "PENDING", "ACCEPTED", "DECLINED"
    private boolean isRead;
    
    @ServerTimestamp
    private Date timestamp;

    /**
     * Default constructor required for Firestore serialization.
     */
    public Notification() {
    }

    // Adding the timestamp to the UI to sort notifications from newest to
    // oldest was helped implemented with the assistance of Gemini.
    /**
     * Constructs a new Notification.
     *
     * @param id         The unique ID of the notification.
     * @param message    The message content.
     * @param attendeeId The ID of the recipient.
     * @param eventId    The ID of the associated event.
     * @param type       The type of notification (e.g., "INVITATION").
     * @param timestamp  The time the notification was created.
     */
    public Notification(String id, String message, String attendeeId, String eventId, String type, Date timestamp) {
        this.id = id;
        this.message = message;
        this.attendeeId = attendeeId;
        this.eventId = eventId;
        this.type = type;
        this.status = "PENDING";
        this.isRead = false;
        this.timestamp = timestamp;
    }

    // Getters and Setters

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void sendNotification() {
        // Send the notification to the user
    }
}
