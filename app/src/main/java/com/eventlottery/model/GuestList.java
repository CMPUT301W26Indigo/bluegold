package com.eventlottery.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Manages the list of attendees for a specific event.
 * Handles attendee registration, status tracking, and capacity limits.
 */
public class GuestList {
    private String eventId;
    private ArrayList<HashMap<String, String>> attendeeIds;
    private Integer listCount;
    private Integer listLimit;

    /**
     * Constructs a GuestList with a specific capacity limit.
     *
     * @param eventId   The unique identifier of the event.
     * @param listLimit The maximum number of attendees allowed.
     */
    public GuestList(String eventId, Integer listLimit) {
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<HashMap<String, String>>();
        this.listCount = 0;
        this.listLimit = listLimit;
    }

    /**
     * Constructs a GuestList without a capacity limit.
     *
     * @param eventId The unique identifier of the event.
     */
    public GuestList(String eventId) {
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<HashMap<String, String>>();
        this.listCount = 0;
        this.listLimit = null;
    }

    /**
     * Gets the unique identifier for the event.
     *
     * @return The event ID string.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Gets the list of attendees and their current statuses.
     * Each entry is a map where the key is the attendee ID and the value is their status.
     *
     * @return An ArrayList of HashMaps representing attendees.
     */
    public ArrayList<HashMap<String, String>> getAttendeeIds() {
        return attendeeIds;
    }

    /**
     * Gets the current number of attendees in the list.
     *
     * @return The current count of attendees.
     */
    public Integer getListCount() {
        return listCount;
    }

    /**
     * Gets the maximum number of attendees allowed for this list.
     *
     * @return The capacity limit, or null if no limit is set.
     */
    public Integer getListLimit() {
        return listLimit;
    }

    /**
     * Adds a new attendee to the list with a default status of "maybe".
     * Increments the attendee count.
     *
     * @param attendeeId The unique identifier of the attendee to add.
     */
    public void addGuestAttendee(String attendeeId) {
        HashMap<String, String> attendee = new HashMap<String, String>();
        attendee.put(attendeeId, "maybe");
        attendeeIds.add(attendee);
        listCount++;
    }

    /**
     * Updates the status of an existing attendee in the list.
     *
     * @param attendeeId The unique identifier of the attendee.
     * @param status     The new status to assign (e.g., "accepted", "declined").
     */
    public void changeAttendeeStatus(String attendeeId, String status) {
        for (HashMap<String, String> attendee : attendeeIds) {
            if (attendee.containsKey(attendeeId)) {
                attendee.put(attendeeId, status);
                return;
            }
        }
    }

    /**
     * Change all entrants who did not sign up for the event to the cancelled status
     * Cancelled attendees are those who have the declined and maybe statuses.
     */
    public void cancelEntrants() {
        ArrayList<String> toCancel = new ArrayList<>();

        for (HashMap<String, String> attendee : attendeeIds) {
            for (String key : attendee.keySet()) {
                String status = attendee.get(key);

                if (status.equals("declined") || status.equals("maybe")) {
                    toCancel.add(key);
                }
            }
        }

        for (String attendeeId : toCancel) {
            changeAttendeeStatus(attendeeId, "cancelled");
        }
    }
}
