package com.eventlottery.model;

import java.util.ArrayList;

/**
 * Manages the waitlist for a specific event.
 * Tracks attendees who are waiting to join an event and enforces capacity limits if set.
 */
public class Waitlist {
    private String eventId;
    private ArrayList<String> attendeeIds;
    private Integer waitlistLimit;
    private Integer waitlistCount;
    private String registrationDeadline;

    /**
     * Constructs a Waitlist with a specific capacity limit.
     *
     * @param eventId       The unique identifier of the event.
     * @param waitlistLimit The maximum number of attendees allowed on the waitlist.
     */
    public Waitlist(String eventId, Integer waitlistLimit, String registrationDeadline) {
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<String>();
        this.waitlistLimit = waitlistLimit;
        this.waitlistCount = 0;
        this.registrationDeadline = registrationDeadline;
    }

    /**
     * Constructs an unlimited Waitlist for the given event.
     *
     * @param eventId The unique identifier of the event.
     */

    public Waitlist(String eventId) {
        this.eventId = eventId;
        this.attendeeIds = new ArrayList<String>();
        this.waitlistLimit = null;
        this.waitlistCount = 0;
        this.registrationDeadline = null;
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
     * Gets the list of attendee IDs currently on the waitlist.
     *
     * @return An ArrayList of attendee ID strings.
     */
    public ArrayList<String> getAttendeeIds() {
        return attendeeIds;
    }

    /**
     * Gets the maximum number of attendees allowed on this waitlist.
     *
     * @return The waitlist limit, or null if there is no limit.
     */
    public Integer getWaitlistLimit() {
        return waitlistLimit;
    }

    /**
     * Gets the current number of attendees on the waitlist.
     *
     * @return The current waitlist count.
     */
    public Integer getWaitlistCount() {
        return waitlistCount;
    }

    /**
     * Checks if the waitlist has reached its capacity limit.
     *
     * @return true if the waitlist is full, false if there is space or no limit.
     */
    public boolean isWaitlistFull() {
        if (waitlistLimit == null) {
            return false; // Unlimited waitlist
        }
        return waitlistCount >= waitlistLimit;
    }

    /**
     * Adds an attendee to the waitlist if it is not full.
     *
     * @param attendeeId The unique identifier of the attendee to add.
     * @throws IllegalStateException if the waitlist is already full.
     */
    public void addAttendee(String attendeeId) {
        if (isWaitlistFull()) {
            throw new IllegalStateException("Waitlist is full");
        } else {
            attendeeIds.add(attendeeId);
            waitlistCount++;
        }
    }

    /**
     * Removes an attendee from the waitlist.
     *
     * @param attendeeId The unique identifier of the attendee to remove.
     * @throws IllegalArgumentException if the attendee is not found in the waitlist.
     */
    public void removeAttendee(String attendeeId) {
        if (!attendeeIds.contains(attendeeId)) {
            throw new IllegalArgumentException("Attendee not found in waitlist");
        } else {
            attendeeIds.remove(attendeeId);
            waitlistCount--;
        }
    }

    /**
     * Checks if a specific attendee is currently on the waitlist.
     *
     * @param attendeeId The unique identifier of the attendee.
     * @return true if the attendee is on the waitlist, false otherwise.
     */
    public boolean findAttendee(String attendeeId) {
        return attendeeIds.contains(attendeeId);
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }


}
