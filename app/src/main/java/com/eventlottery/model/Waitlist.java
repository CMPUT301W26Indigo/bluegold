package com.eventlottery.model;

import java.util.ArrayList;

public class Waitlist {
    private String eventId;
    private ArrayList<String> attendeeIds;
    private Integer waitlistLimit;
    private Integer waitlistCount;

    public Waitlist(String eventId, Integer waitlistLimit) {
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<String>();
        this.waitlistLimit = waitlistLimit;
        this.waitlistCount = 0;
    }

    public Waitlist(String eventId) {
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<String>();
        this.waitlistLimit = null;
        this.waitlistCount = 0;
    }

    public String getEventId() {
        return eventId;
    }

    public ArrayList<String> getAttendeeIds() {
        return attendeeIds;
    }

    public Integer getWaitlistLimit() {
        return waitlistLimit;
    }

    public Integer getWaitlistCount() {
        return waitlistCount;
    }

    public boolean isWaitlistFull() {
        if (waitlistLimit == null) {
            return false; // Unlimited waitlist
        }
        return waitlistCount >= waitlistLimit;
    }

    public void addAttendee(String attendeeId) {
        if (isWaitlistFull()) {
            throw new IllegalStateException("Waitlist is full");
        } else {
            attendeeIds.add(attendeeId);
            waitlistCount++;
        }
    }

    public void removeAttendee(String attendeeId) {
        if (!attendeeIds.contains(attendeeId)) {
            throw new IllegalArgumentException("Attendee not found in waitlist");
        } else {
            attendeeIds.remove(attendeeId);
            waitlistCount--;
        }
    }

    public boolean findAttendee(String attendeeId) {
        return attendeeIds.contains(attendeeId);
    }


}
