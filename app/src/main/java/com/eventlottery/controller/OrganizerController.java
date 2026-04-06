package com.eventlottery.controller;

import com.eventlottery.model.Event;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Organizer-related operations.
 * Part of the 'Controller' in MVC.
 */
public class OrganizerController {
    private final FirebaseFirestore db;

    /**
     * Interface for handling events loaded from Firestore.
     */
    public interface OnDataLoadedListener<T> {
        void onDataLoaded(List<T> data);
        void onError(Exception e);
    }

    /**
     * Interface for handling operations on events.
     */
    public interface OnOperationListener {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Constructor for OrganizerController.
     */
    public OrganizerController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches events where the user is the main organizer or a co-organizer.
     */
    public void getOrganizerEvents(String userId, OnDataLoadedListener<Event> listener) {
        db.collection("events")
                .where(Filter.or(
                        Filter.equalTo("organizerId", userId),
                        Filter.arrayContains("coOrganizerIds", userId)
                ))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId());
                        events.add(event);
                    }
                    listener.onDataLoaded(events);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Adds a user as a co-organizer to an event.
     */
    public void addCoOrganizer(String eventId, String coOrganizerId, OnOperationListener listener) {
        db.collection("events").document(eventId)
                .update("coOrganizerIds", FieldValue.arrayUnion(coOrganizerId))
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Conducts a lottery draw for an event.
     */
    public void conductLottery(String eventId, OnOperationListener listener) {
        // Logic to select winners from the waitlist
        listener.onSuccess();
    }
}
