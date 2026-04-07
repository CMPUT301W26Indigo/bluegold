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

    /**
     * Default no-argument constructor required for Firebase Firestore deserialization.
     */
    public Notification() {
    }

    /**
     * Constructs a new Notification with full details.
     * @param id Unique identifier for the notification.
     * @param title The title of the notification.
     * @param message The body text of the notification.
     * @param attendeeId ID of the recipient attendee.
     * @param eventId ID of the event related to the notification.
     * @param type The type of notification (e.g., INVITATION, INFO).
     * @param timestamp The creation date and time.
     */
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

    /**
     * Constructs a new Notification without an explicit title.
     * @param id Unique identifier for the notification.
     * @param message The body text of the notification.
     * @param attendeeId ID of the recipient attendee.
     * @param eventId ID of the event related to the notification.
     * @param type The type of notification (e.g., INVITATION, INFO).
     * @param timestamp The creation date and time.
     */
    public Notification(String id, String message, String attendeeId, String eventId, String type, Date timestamp) {
        this(id, null, message, attendeeId, eventId, type, timestamp);
    }

    // Getters and Setters
    /**
     * Gets the unique ID of the notification.
     * @return The notification ID string.
     */
    public String getId() { return id; }
    /**
     * Sets the unique ID of the notification.
     * @param id The notification ID to set.
     */
    public void setId(String id) { this.id = id; }
    /**
     * Gets the title of the notification.
     * @return The title string.
     */
    public String getTitle() { return title; }
    /**
     * Sets the title of the notification.
     * @param title The title string to set.
     */
    public void setTitle(String title) { this.title = title; }
    /**
     * Gets the body message of the notification.
     * @return The message string.
     */
    public String getMessage() { return message; }
    /**
     * Sets the body message of the notification.
     * @param message The message string to set.
     */
    public void setMessage(String message) { this.message = message; }
    /**
     * Gets the ID of the attendee who receives the notification.
     * @return The recipient attendee ID.
     */
    public String getAttendeeId() { return attendeeId; }
    /**
     * Sets the ID of the attendee who receives the notification.
     * @param attendeeId The recipient attendee ID to set.
     */
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }
    /**
     * Gets the name of the recipient.
     * @return The recipient's name.
     */
    public String getRecipientName() { return recipientName; }
    /**
     * Sets the name of the recipient.
     * @param recipientName The recipient's name to set.
     */
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    /**
     * Gets the ID of the associated event.
     * @return The event ID string.
     */
    public String getEventId() { return eventId; }
    /**
     * Sets the ID of the associated event.
     * @param eventId The event ID string to set.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }
    /**
     * Gets the ID of the notification sender.
     * @return The sender's ID string.
     */
    public String getSenderId() { return senderId; }
    /**
     * Sets the ID of the notification sender.
     * @param senderId The sender's ID string to set.
     */
    public void setSenderId(String senderId) { this.senderId = senderId; }
    /**
     * Gets the name of the notification sender.
     * @return The sender's name.
     */
    public String getSenderName() { return senderName; }
    /**
     * Sets the name of the notification sender.
     * @param senderName The sender's name to set.
     */
    public void setSenderName(String senderName) { this.senderName = senderName; }
    /**
     * Gets the type of notification.
     * @return The type string (e.g., INVITATION, INFO).
     */
    public String getType() { return type; }
    /**
     * Sets the type of notification.
     * @param type The type string to set.
     */
    public void setType(String type) { this.type = type; }
    /**
     * Gets the response status of the notification.
     * @return The status string (e.g., PENDING, ACCEPTED).
     */
    public String getStatus() { return status; }
    /**
     * Sets the response status of the notification.
     * @param status The status string to set.
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Standard Java Boolean getter. Firestore SDK will map this to "read" field.
     * @return true if the notification has been read, false otherwise.
     */
    public boolean isRead() { return read; }

    /**
     * Sets the read status of the notification.
     * @param read true to mark as read, false for unread.
     */
    public void setRead(boolean read) { this.read = read; }

    /**
     * Gets the creation timestamp of the notification.
     * @return The Date object representing the timestamp.
     */
    public Date getTimestamp() { return timestamp; }
    /**
     * Sets the creation timestamp of the notification.
     * @param timestamp The Date object to set.
     */
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
