package com.eventlottery.controller;

import com.eventlottery.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Deals with database operations for events.
 */
public class EventController {
    private final FirebaseFirestore db;
    private final String COLLECTION_NAME = "events";

    /**
     * Interface for handling events loaded from Firestore.
     */
    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<Event> events);
        void onError(Exception e);
    }

    /**
     * Interface for handling operations on events.
     */
    public interface OnEventOperationListener {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Constructor for EventController.
     */
    public EventController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches all events from Firestore.
     */
    public void getAllEvents(OnEventsLoadedListener listener) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId());
                        events.add(event);
                    }
                    listener.onEventsLoaded(events);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Adds a new event to Firestore.
     */
    public void addEvent(Event event, OnEventOperationListener listener) {
        db.collection(COLLECTION_NAME)
                .add(event)
                .addOnSuccessListener(documentReference -> {
                    event.setId(documentReference.getId());
                    listener.onSuccess();
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Updates an existing event in Firestore.
     */
    public void updateEvent(Event event, OnEventOperationListener listener) {
        if (event.getId() == null || event.getId().isEmpty()) {
            listener.onError(new IllegalArgumentException("Event ID is required for update"));
            return;
        }
        db.collection(COLLECTION_NAME)
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Deletes an event from Firestore.
     */
    public void deleteEvent(String eventId, OnEventOperationListener listener) {
        db.collection(COLLECTION_NAME)
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

}
