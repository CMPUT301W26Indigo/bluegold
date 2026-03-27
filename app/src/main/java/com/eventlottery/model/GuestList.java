package com.eventlottery.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages the list of attendees for a specific event.
 * Handles attendee registration, status tracking, and capacity limits.
 */
public class GuestList {
    private String eventId;
    //private ArrayList<HashMap<String, String>> attendeeIds;
    private HashMap<String, String> attendees; // <attendeeId, status>
    private Integer listCount;
    private Integer listLimit;

    /**
     * Default no-argument constructor required for Firebase Firestore deserialization.
     */
    public GuestList() {
        this.attendees = new HashMap<>();
        this.listCount = 0;
        this.listLimit = null;
    }

    /**
     * Constructs a GuestList with a specific capacity limit.
     *
     * @param eventId   The unique identifier of the event.
     * @param listLimit The maximum number of attendees allowed.
     */
    public GuestList(String eventId, Integer listLimit) {
        this.eventId = eventId;
        this.attendees = new HashMap<String, String>();
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
        this.attendees = new HashMap<String, String>();
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
     * Sets the unique identifier for the event.
     *
     * @param eventId The event ID string.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the list of attendees and their current statuses.
     * Each entry is a map where the key is the attendee ID and the value is their status.
     *
     * @return An ArrayList of HashMaps representing attendees.
     */
    public HashMap<String, String> getAttendees() {
        return attendees;
    }

    /**
     * Sets the list of attendees and their current statuses.
     *
     * @param attendees An ArrayList of HashMaps representing attendees.
     */
    public void setAttendees(HashMap<String, String> attendees) {
        this.attendees = attendees;
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
     * Sets the current number of attendees in the list.
     *
     * @param listCount The current count of attendees.
     */
    public void setListCount(Integer listCount) {
        this.listCount = listCount;
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
     * Sets the maximum number of attendees allowed for this list.
     *
     * @param listLimit The capacity limit.
     */
    public void setListLimit(Integer listLimit) {
        this.listLimit = listLimit;
    }

    /**
     * Adds a new attendee to the list with a default status of "maybe".
     * Increments the attendee count.
     *
     * @param attendeeId The unique identifier of the attendee to add.
     */
    public void addGuestAttendee(String attendeeId) {
        attendees.put(attendeeId, "maybe");
        listCount++;
    }

    /**
     * Finds the attendee with the given ID in the list.
     *
     * @param attendeeId
     * @return status of attendee or null if none is found
     */
    public String findAttendee(String attendeeId) {
        return attendees.get(attendeeId);
    }

    /**
     * Updates the status of an existing attendee in the list.
     *
     * @param attendeeId The unique identifier of the attendee.
     * @param status     The new status to assign (e.g., "accepted", "declined").
     */
    public void changeAttendeeStatus(String attendeeId, String status) {
        String currentStatus = attendees.get(attendeeId);
        if (currentStatus != null) {
            throw new IllegalArgumentException("Attendee with ID " + attendeeId + " is already in the list.");
        } else {
            attendees.put(attendeeId, status);
        }
    }

    /**
     * Change all entrants who did not sign up for the event to the cancelled status
     * Cancelled attendees are those who have the declined and maybe statuses.
     */
    public void cancelEntrants() {
        ArrayList<String> toCancel = new ArrayList<>();
        attendees.forEach((attendeeId, status) -> {
            Boolean b = status.equals("declined") || status.equals("maybe") ? toCancel.add(attendeeId) : null;
        });

        for (String attendeeId : toCancel) {
            changeAttendeeStatus(attendeeId, "cancelled");
        }
    }

    /**
     * Creates and returns a list of attendee IDs.
     *
     * @return ArrayList of just attendee IDs
     */
    public ArrayList<String> getAttendeeIds() {
        return new ArrayList<>(attendees.keySet());
    }


}
