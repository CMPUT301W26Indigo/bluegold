package com.eventlottery.model;

public class AttendeeEventHistory {
    private String eventID;
    private boolean attended;

    public AttendeeEventHistory(String eventID) {
        this.eventID = eventID;
        this.attended = false;
    }

    public String getEventID() {
        return eventID;
    }

    public boolean isAttended() {
        return attended;
    }

    public void updateAttendance() {
        this.attended = true;
    }

}
