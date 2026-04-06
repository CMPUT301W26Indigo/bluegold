package com.eventlottery.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventOrganizer extends AbstractUser {
    private static final String TAG = "EventOrganizer";
    private static final String COLLECTION_NAME = "Event Organizers";

    private ArrayList<Event> events;

    @Exclude
    private final FirebaseFirestore db;

    public interface OnEventOrganizerLoadedListener {
        void onSuccess(EventOrganizer eventOrganizer);
        void onError(Exception e);
    }

    public EventOrganizer() {
        super();
        this.events = new ArrayList<Event>();

        FirebaseFirestore tempDb = null;
        try {
            tempDb = FirebaseFirestore.getInstance();
        } catch (IllegalStateException e) {
            tempDb = null;
            Log.w(TAG, "Firebase not initialized, Firestore operations will be unavailable");
        }
        this.db = tempDb;
    }

    // Getters and Setters

    /**
     * Gets the list of events associated with this organizer.
     * @return events
     */
    public ArrayList<Event> getEvents() {
        return events;
    }

    /**
     * Sets the list of events associated with this organizer.
     * @param events
     */
    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }


    // Methods to create an event with or without parameters based on UI implementation
    /**
     * Creates an event with no parameters.
     * @return created event
     */
    public Event createEventBlank() {
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
            List<String> coOrganizerIds,
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
                coOrganizerIds,
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
        saveToFirebase();
    }

    /**
     * Finds an event from the organizer's list of events.
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

    public void removeEvent(String eventId) {
        try {
            Event event = findEvent(eventId);
            events.remove(event);
            saveToFirebase();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "No event found with ID: " + eventId, e);
        }
    }

    /**
     * Retrieves all attendees for a specific event asynchronously.
     * This function and the following one are from Gemini - "How to properly fetch attendees from Firebase"
     * 26/03/2026
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
                    synchronized (attendees) { // Synchronize access to attendees list so that it's thread-safe
                        attendees.add(attendee);
                    }
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
                        if (listener != null) listener.onSuccess(attendees); // This is where we pass the array of attendee objects into
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
                        attendee.setID(attendeeId);
                        if (listener != null) listener.onSuccess(attendee);
                    } else if (listener != null) {
                        listener.onError(new Exception("Attendee document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }

    /**
     * Sends a notification to all attendees of an event.
     *
     * @param eventId
     * @param message
     */
    public void sendNotification(String message, String eventId) {
        Notification notification = new Notification();
        notification.setMessage(message);
//        TODO: implement sendNotification method
//        notification.sendNotification();
        // This method may change depending on how the send notification method is implemented i.e whether it sends to
        // one user at a time or a list of users (this will determine whether this method needs a loop to send to all users)
    }

    /**
     * Triggers lottery system of a specified event without a limit.
     *
     * @param eventId
     */
    public void lotteryWithoutLimit(String eventId) {
        Event event = findEvent(eventId);
        event.LotterySystem();
    }

    /**
     * Triggers lottery system of a specified event with a limit.
     *
     * @param eventId
     * @param limit
     */
    public void lotteryWithLimit(String eventId, int limit) {
        Event event = findEvent(eventId);
        event.LotterySystem(limit);
    }

    /**
     * Removes all inactive attendees from a specified event.
     * @param eventId
     */
    public void removeInactiveAttendees(String eventId) {
        Event event = findEvent(eventId);
        event.getGuestList().cancelEntrants();
    }

    @Override
    public void saveToFirebase() {
        if (db == null) return;
        if (deviceID == null || deviceID.isEmpty()) {
            Log.w(TAG, "Cannot save attendee: deviceID is null or empty");
            return;
        }
        db.collection(COLLECTION_NAME).document(deviceID).set(this)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Attendee successfully updated on Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating attendee on Firebase", e));
    }

    public void fetchFromFirebase(OnEventOrganizerLoadedListener listener) {
        if (db == null) {
            if (listener != null) listener.onError(new Exception("Firebase not initialized"));
            return;
        }
        if (deviceID == null || deviceID.isEmpty()) {
            if (listener != null) listener.onError(new Exception("DeviceID not set"));
            return;
        }
        db.collection(COLLECTION_NAME).document(deviceID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    EventOrganizer remote = documentSnapshot.toObject(EventOrganizer.class);
                    if (remote != null) {
                        this.name = remote.name;
                        this.email = remote.email;
                        this.phoneNumber = remote.phoneNumber;
                        this.address = remote.address;
                        this.notification = remote.notification;
                        this.events = remote.events != null ? remote.events : new ArrayList<>();
                        this.isAdmin = remote.isAdmin;

                        if (listener != null) listener.onSuccess(this);
                    } else if (listener != null) {
                        listener.onError(new Exception("EventOrganizer document not found"));
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
