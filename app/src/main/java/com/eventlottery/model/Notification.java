package com.eventlottery.model;

public class Notification {
    private String message;
    private String attendeeId;
    private String eventId;

    public Notification(String message, String attendeeId, String eventId) {
        this.message = message;
        this.attendeeId = attendeeId;
        this.eventId = eventId;
    }

    public void sendNotification() {
        // Send the notification to the user
    }
}
