package com.eventlottery.controller;

import com.eventlottery.model.Event;
import com.eventlottery.model.Notification;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for Organizer-related operations.
 * Part of the 'Controller' in MVC.
 */
public class OrganizerController {
    private final FirebaseFirestore db;

    /**
     * Interface for handling events loaded from Firestore.
     * @param <T> The type of data being loaded.
     */
    public interface OnDataLoadedListener<T> {
        /**
         * Called when data is successfully loaded.
         * @param data List of loaded data items.
         */
        void onDataLoaded(List<T> data);
        
        /**
         * Called when an error occurs during data loading.
         * @param e The exception that occurred.
         */
        void onError(Exception e);
    }

    /**
     * Interface for handling operations on events.
     */
    public interface OnOperationListener {
        /**
         * Called when the operation is successful.
         */
        void onSuccess();
        
        /**
         * Called when an error occurs during the operation.
         * @param e The exception that occurred.
         */
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
     * @param userId The ID of the user whose events are to be fetched.
     * @param listener Callback for the loaded data.
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
     * Adds a user as a co-organizer to an event directly.
     * @param eventId The ID of the event.
     * @param coOrganizerId The ID of the co-organizer to add.
     * @param listener Callback for the operation result.
     * @deprecated Use {@link #sendCoOrganizerInvite} to follow invitation workflow.
     */
    @Deprecated
    public void addCoOrganizer(String eventId, String coOrganizerId, OnOperationListener listener) {
        db.collection("events").document(eventId)
                .update("coOrganizerIds", FieldValue.arrayUnion(coOrganizerId))
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Sends a co-organizer invitation to an attendee.
     * @param eventId The ID of the event.
     * @param attendeeId The ID of the attendee to invite.
     * @param senderId The ID of the organizer sending the invite.
     * @param senderName The name of the organizer sending the invite.
     * @param eventName The name of the event.
     * @param listener Callback for the operation result.
     */
    public void sendCoOrganizerInvite(String eventId, String attendeeId, String senderId, String senderName, String eventName, OnOperationListener listener) {
        String notificationId = UUID.randomUUID().toString();
        Notification invite = new Notification();
        invite.setId(notificationId);
        invite.setAttendeeId(attendeeId);
        invite.setEventId(eventId);
        invite.setSenderId(senderId);
        invite.setSenderName(senderName);
        invite.setTitle("Co-Organizer Invitation");
        invite.setMessage(senderName + " has invited you to be a co-organizer for the event: " + eventName);
        invite.setType("CO_ORGANIZER_INVITE");
        invite.setStatus("PENDING");
        invite.setRead(false);
        invite.setTimestamp(new Date());

        db.collection("notifications").document(notificationId)
                .set(invite)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Sends a private event invitation to an attendee.
     * @param eventId The ID of the event.
     * @param attendeeId The ID of the attendee to invite.
     * @param eventName The name of the event.
     * @param listener Callback for the operation result.
     */
    public void sendPrivateEventInvite(String eventId, String attendeeId, String eventName, OnOperationListener listener) {
        WriteBatch batch = db.batch();
        String nid = UUID.randomUUID().toString();
        Notification invite = new Notification(nid, "Private Event Invitation", "You have been invited to join the private event: " + eventName, attendeeId, eventId, "INVITATION", new Date());
        batch.set(db.collection("notifications").document(nid), invite);
        Map<String, Object> data = new HashMap<>();
        data.put("status", "invited");
        data.put("invitedAt", System.currentTimeMillis());
        batch.set(db.collection("events").document(eventId).collection("guestList").document(attendeeId), data);
        batch.set(db.collection("attendees").document(attendeeId).collection("Selected").document(eventId), data);
        batch.commit().addOnSuccessListener(aVoid -> listener.onSuccess()).addOnFailureListener(listener::onError);
    }

    /**
     * Conducts a lottery draw for an event.
     * @param eventId The ID of the event to conduct the lottery for.
     * @param listener Callback for the operation result.
     */
    public void conductLottery(String eventId, OnOperationListener listener) {
        // Logic to select winners from the waitlist
        listener.onSuccess();
    }
}
