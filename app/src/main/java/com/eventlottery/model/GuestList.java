package com.eventlottery.data.models;

import java.util.ArrayList;
import java.util.HashMap;

public class GuestList {
    private String eventId;
    private ArrayList<HashMap<String, String>> attendeeIds;
    private Integer listCount;
    private Integer listLimit; // if null, no limit


    public GuestList(String eventId, Integer listLimit) {
        // Constructor for GuestList with listLimit
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<HashMap<String, String>>();
        this.listCount = 0;
        this.listLimit = listLimit;
    }

    public GuestList(String eventId) {
        // Constructor for GuestList without listLimit
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<HashMap<String, String>>();
        this.listCount = 0;
        this.listLimit = null;
    }

    public String getEventId() {
        return eventId;
    }

    public ArrayList<HashMap<String, String>> getAttendeeIds() {
        return attendeeIds;
    }

    public Integer getListCount() {
        return listCount;
    }

    public Integer getListLimit() {
        return listLimit;
    }

    public void addGuestAttendee(String attendeeId) {
        // Add an attendee to the list
        HashMap<String, String> attendee = new HashMap<String, String>();
        attendee.put(attendeeId, "maybe");
        attendeeIds.add(attendee);
        listCount++;
    }

    public void changeAttendeeStatus(String attendeeId, String status) {
        // Change the status of an attendee in the list
        for (HashMap<String, String> attendee : attendeeIds) {
            if (attendee.containsKey(attendeeId)) {
                attendee.put(attendeeId, status);
                return;
            }
        }
    }
}
