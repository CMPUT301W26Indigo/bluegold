package com.eventlottery.model;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class EventOrganizer extends AbstractUser {
    private ArrayList<Event> events;

    public EventOrganizer() {
        super();
        this.events = new ArrayList<Event>();
    }

    // Methods to create an event with or without parameters based on UI implementation

    /**
     * Creates an event with no parameters.
     * @return created event
     */
    public Event createEventBlanked() {
        Event event = new Event();
        events.add(event);
        return event;
    }
    /**
     * Creates an event with all parameters.
     * @return created event
     */
    public Event createEvent(
            String id,
            String name,
            String description,
            String organizerId,
            String date,
            String time,
            String endTime,
            String location,
            String locationAddress,
            int capacity,
            Integer waitlistLimit,
            int waitlistCount,
            int confirmedCount,
            String posterImageUrl,
            double price,
            long registrationOpens,
            long registrationCloses,
            Long lotteryDrawDate,
            long createdAt,
            long updatedAt,
            List<String> tags,
            boolean geolocationEnabled,
            Integer geolocationRadius,
            String status,
            String qrCodeUrl,
            Bitmap qrCode,
            boolean isFlagged,
            int flagCount,
            Waitlist waitlist,
            GuestList guestList,
            boolean recurringEvent,
            boolean isPrivate) {
        Event event = new Event(
                id,
                name,
                description,
                organizerId,
                date,
                time,
                endTime,
                location,
                locationAddress,
                capacity,
                waitlistLimit,
                waitlistCount,
                confirmedCount,
                posterImageUrl,
                price,
                registrationOpens,
                registrationCloses,
                lotteryDrawDate,
                createdAt,
                updatedAt,
                tags,
                geolocationEnabled,
                geolocationRadius,
                status,
                qrCodeUrl,
                qrCode,
                isFlagged,
                flagCount,
                waitlist,
                guestList,
                recurringEvent,
                isPrivate);
        events.add(event);
        return event;
    }

    /**
     * Adds an event to the organizer's list of events.
     * @param event
     */
    public void addEvent(Event event) {
        events.add(event);
    }

    /**
     * Removes an event from the organizer's list of events.
     * If event with that ID is not found, and exception is thrown
     * @param eventId
     * @return found event
     */
    public Event findEvent(String eventId) {
        for (Event event : events) {
            if (event.getId().equals(eventId)) {
                return event;
            }
        }
        throw new IllegalArgumentException("No event found with ID: " + eventId);
    }

    public ArrayList<Attendee> getAttendeesOfEvent(String eventId) {
        Event event = findEvent(eventId);
        ArrayList<String> attendeeIds = event.getGuestList().getAttendeeIds();
        ArrayList<Attendee> attendees = new ArrayList<>();
        for (String attendeeId : attendeeIds) {
            attendees.add(findAttendee(attendeeId));
        }
        return attendees;
    }

    public Attendee findAttendee(String attendeeId) {
        //TODO: Implement this method with Firebase
        return null;
    }


}
