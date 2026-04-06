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
    private String title;
    private String message;
    private String attendeeId;
    private String recipientName;
    private String eventId;
    private String senderId;
    private String senderName;
    private String type; // e.g., "INVITATION", "INFO"
    private String status; // e.g., "PENDING", "ACCEPTED", "DECLINED"
    private boolean read; // Database field name: "read"
    
    @ServerTimestamp
    private Date timestamp;

    public Notification() {
    }

    public Notification(String id, String title, String message, String attendeeId, String eventId, String type, Date timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.attendeeId = attendeeId;
        this.eventId = eventId;
        this.type = type;
        this.status = "PENDING";
        this.read = false;
        this.timestamp = timestamp;
    }

    public Notification(String id, String message, String attendeeId, String eventId, String type, Date timestamp) {
        this(id, null, message, attendeeId, eventId, type, timestamp);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Standard Java Boolean getter. Firestore SDK will map this to "read" field.
     */
    public boolean isRead() { return read; }

    public void setRead(boolean read) { this.read = read; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
