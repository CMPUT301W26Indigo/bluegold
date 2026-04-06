package com.eventlottery.controller;

import com.eventlottery.model.Comment;
import com.eventlottery.model.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deals with database operations for events.
 * Part of the 'Controller' in MVC.
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
     * Interface for checking guestlist status.
     */
    public interface OnGuestlistStatusListener {
        void onStatusLoaded(String status);
        void onError(Exception e);
    }

    /**
     * Interface for checking waitlist status.
     */
    public interface OnWaitlistStatusListener {
        void onStatusChecked(boolean isOnWaitlist);
        void onError(Exception e);
    }

    /**
     * Interface for handling comments
     */
    public interface OnCommentsLoadedListener {
        void onCommentsLoaded(List<Comment> comments);
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
     * Fetches only public events from Firestore.
     * @param listener
     */
    public void getAllPublicEvents(OnEventsLoadedListener listener) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("private", false)
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
     * Fetches specific events from Firestore by their IDs.
     */
    public void getEventsByIds(List<String> eventIds, OnEventsLoadedListener listener) {
        if (eventIds == null || eventIds.isEmpty()) {
            listener.onEventsLoaded(new ArrayList<>());
            return;
        }

        // Firestore 'in' query limit is 10 (or 30 in some cases), but for simplicity:
        db.collection(COLLECTION_NAME)
                .whereIn(FieldPath.documentId(), eventIds)
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
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        String eventId = docRef.getId();

        event.setId(eventId);
        event.setQrCodeUrl("eventlottery://event/" + eventId);

        docRef.set(event)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
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

    /**
     * Adds an attendee to an event's waitlist.
     * @param eventId The ID of the event.
     * @param attendeeId The ID of the attendee.
     * @param listener Callback for completion.
     */
    public void joinWaitlist(String eventId, String attendeeId, OnEventOperationListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "waiting"); // Default status

        db.collection(COLLECTION_NAME).document(eventId)
                .collection("waitlist").document(attendeeId)
                .set(data)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);

        db.collection("attendees").document(attendeeId)
                .collection("waitListed").document(eventId)
                .set(data)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Removes an attendee from an event's waitlist.
     * @param eventId The ID of the event.
     * @param attendeeId The ID of the attendee.
     * @param listener Callback for completion.
     */
    public void leaveWaitlist(String eventId, String attendeeId, OnEventOperationListener listener) {
        db.collection(COLLECTION_NAME).document(eventId)
                .collection("waitlist").document(attendeeId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);

        db.collection("attendees").document(attendeeId)
                .collection("waitListed").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Checks if an attendee is currently on the waitlist for a specific event.
     * @param eventId The ID of the event.
     * @param attendeeId The ID of the attendee.
     * @param listener Callback for the result.
     */
    public void checkIfAttendeeOnWaitlist(String eventId, String attendeeId, OnWaitlistStatusListener listener) {
        db.collection(COLLECTION_NAME).document(eventId)
                .collection("waitlist").document(attendeeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    listener.onStatusChecked(documentSnapshot.exists());
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Adds a new comment to an event's comments subcollection.
     */
    public void addComment(String eventId, Comment comment, OnEventOperationListener listener) {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(eventId)
                .collection("comments").document();
        comment.setCommentId(docRef.getId());
        docRef.set(comment)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Fetches all comments for a specific event (ascending order in time)
     */
    public void getComments(String eventId, OnCommentsLoadedListener listener) {
        db.collection(COLLECTION_NAME).document(eventId)
                .collection("comments")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Comment comment = document.toObject(Comment.class);
                        comment.setCommentId(document.getId());
                        comments.add(comment);
                    }
                    listener.onCommentsLoaded(comments);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Deletes a specific comment
     */
    public void deleteComment(String eventId, String commentId, OnEventOperationListener listener) {
        db.collection(COLLECTION_NAME).document(eventId)
                .collection("comments").document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void checkIfAttendeeOnGuestlist(String eventId, String attendeeId, OnWaitlistStatusListener listener) {
        db.collection(COLLECTION_NAME).document(eventId)
                .collection("guestList").document(attendeeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    listener.onStatusChecked(documentSnapshot.exists());
                })
                .addOnFailureListener(listener::onError);
    }

    public void getAttendeeGuestlistStatus(String eventId, String attendeeId, OnGuestlistStatusListener listener) {
        db.collection(COLLECTION_NAME).document(eventId)
                .collection("guestList").document(attendeeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onStatusLoaded(documentSnapshot.getString("status"));
                    } else {
                        listener.onStatusLoaded(null);
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    public void removeFromGuestlist(String eventId, String attendeeId, OnEventOperationListener listener) {
        db.collection(COLLECTION_NAME).document(eventId)
                .collection("guestList").document(attendeeId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }
}
