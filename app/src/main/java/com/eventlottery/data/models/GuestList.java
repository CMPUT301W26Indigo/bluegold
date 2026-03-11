package com.eventlottery.data.models;

import java.util.ArrayList;

public class GuestList {
    private String eventId;
    private ArrayList<String> attendeeIds;
    private Integer listCount;
    private Integer listLimit;

    public GuestList(String eventId, Integer listLimit) {
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<String>();
        this.listCount = 0;
        this.listLimit = listLimit;
    }

    public GuestList(String eventId) {
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<String>();
        this.listCount = 0;
        this.listLimit = null;
    }

    public String getEventId() {
        return eventId;
    }

    public ArrayList<String> getAttendeeIds() {
        return attendeeIds;
    }

    public Integer getListCount() {
        return listCount;
    }

    public Integer getListLimit() {
        return listLimit;
    }

    public void addGuestAttendee(String attendeeId) {
        attendeeIds.add(attendeeId);
        listCount++;
    }



}
