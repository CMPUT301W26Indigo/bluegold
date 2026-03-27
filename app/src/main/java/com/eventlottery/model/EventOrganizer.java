package com.eventlottery.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventOrganizer extends AbstractUser {
    private static final String TAG = "EventOrganizer";
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

    /**
     * Retrieves all attendees for a specific event asynchronously.
     *
     * @param eventId The ID of the event.
     * @param listener Callback to handle the list of retrieved attendees.
     */
    public void getAttendeesOfEvent(String eventId, OnAttendeesLoadedListener listener) {
        Event event = findEvent(eventId);
        ArrayList<String> attendeeIds = event.getGuestList().getAttendeeIds();
        ArrayList<Attendee> attendees = new ArrayList<>();

        if (attendeeIds.isEmpty()) {
            if (listener != null) listener.onSuccess(attendees);
            return;
        }

        final int total = attendeeIds.size();
        final int[] count = {0};

        for (String attendeeId : attendeeIds) {
            findAttendee(attendeeId, new Attendee.OnAttendeeLoadedListener() {
                @Override
                public void onSuccess(Attendee attendee) {
                    attendees.add(attendee);
                    checkProgress();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error fetching attendee " + attendeeId, e);
                    checkProgress();
                }

                private void checkProgress() {
                    count[0]++;
                    if (count[0] == total) {
                        if (listener != null) listener.onSuccess(attendees);
                    }
                }
            });
        }
    }

    /**
     * Fetches an attendee from Firebase by ID.
     * Since Firebase is asynchronous, this uses a listener to return the data once loaded.
     *
     * @param attendeeId The ID of the attendee to find.
     * @param listener Callback to handle the retrieved attendee.
     */
    public void findAttendee(String attendeeId, Attendee.OnAttendeeLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("attendees").document(attendeeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Attendee attendee = documentSnapshot.toObject(Attendee.class);
                    if (attendee != null) {
                        attendee.setAttendeeID(attendeeId);
                        if (listener != null) listener.onSuccess(attendee);
                    } else if (listener != null) {
                        listener.onError(new Exception("Attendee document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }

    public interface OnAttendeesLoadedListener {
        void onSuccess(ArrayList<Attendee> attendees);
        void onError(Exception e);
    }
}
