package com.eventlottery.controller;

import com.eventlottery.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Organizer-related operations.
 * Part of the 'Controller' in MVC.
 */
public class OrganizerController {
    private final FirebaseFirestore db;

    public interface OnDataLoadedListener<T> {
        void onDataLoaded(List<T> data);
        void onError(Exception e);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onError(Exception e);
    }

    public OrganizerController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches events created by a specific organizer.
     */
    public void getOrganizerEvents(String organizerId, OnDataLoadedListener<Event> listener) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
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
     * Conducts a lottery draw for an event.
     */
    public void conductLottery(String eventId, OnOperationListener listener) {
        // Logic to select winners from the waitlist
        listener.onSuccess();
    }
}
