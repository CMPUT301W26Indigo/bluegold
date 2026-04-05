package com.eventlottery.controller;

import com.eventlottery.model.Attendee;
import com.eventlottery.model.AttendeeEventHistory;
import com.eventlottery.model.GuestList;
import com.eventlottery.model.User;
import com.eventlottery.model.Waitlist;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Controller for User-related operations.
 * Part of the 'Controller' in MVC.
 */
public class UserController {
    private final FirebaseFirestore db;
    private final String COLLECTION_NAME = "users";
    private final String ATTENDEE_COLLECTION = "attendees";

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onError(Exception e);
    }

    public interface OnUserOperationListener {
        void onSuccess();
        void onError(Exception e);
    }

    public UserController() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getUser(String userId, OnUserLoadedListener listener) {
        db.collection(COLLECTION_NAME).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId());
                            listener.onUserLoaded(user);
                        } else {
                            listener.onError(new Exception("Failed to parse user"));
                        }
                    } else {
                        listener.onError(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    public void saveUser(User user, OnUserOperationListener listener) {
        db.collection(COLLECTION_NAME).document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Deletes a user profile from Firestore.
     * Before deletion, it removes the user from all waitlists and guest lists.
     * This fulfills the requirement of wiping the profile from existence.
     *
     * @param userId   The ID of the user to delete.
     * @param listener The listener for success or error callbacks.
     */
    public void deleteUser(String userId, OnUserOperationListener listener) {
        // 1. Fetch Attendee data to find associated events for cleanup
        db.collection(ATTENDEE_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Attendee attendee = documentSnapshot.toObject(Attendee.class);
                        if (attendee != null) {
                            // Remove from all waitlists the user joined
                            if (attendee.getWaitListed() != null) {
                                for (String eventId : attendee.getWaitListed()) {
                                    Waitlist waitlist = new Waitlist(eventId);
                                    waitlist.fetchFromFirebase(new Waitlist.OnWaitlistLoadedListener() {
                                        @Override
                                        public void onSuccess() {
                                            try {
                                                waitlist.removeAttendee(userId);
                                            } catch (Exception ignored) {}
                                        }
                                        @Override
                                        public void onError(Exception e) {}
                                    });
                                }
                            }

                            // Remove from all guest lists (from history)
                            if (attendee.getEventHistory() != null) {
                                for (AttendeeEventHistory history : attendee.getEventHistory()) {
                                    String eventId = history.getEventID();
                                    GuestList guestList = new GuestList(eventId);
                                    guestList.fetchFromFirebase(new GuestList.OnGuestListLoadedListener() {
                                        @Override
                                        public void onSuccess() {
                                            try {
                                                guestList.removeAttendee(userId);
                                            } catch (Exception ignored) {}
                                        }
                                        @Override
                                        public void onError(Exception e) {}
                                    });
                                }
                            }
                        }
                    }
                    
                    // 2. Perform final deletion of primary Firestore documents
                    db.collection(ATTENDEE_COLLECTION).document(userId).delete();
                    db.collection(COLLECTION_NAME).document(userId).delete()
                            .addOnSuccessListener(aVoid -> {
                                if (listener != null) listener.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                if (listener != null) listener.onError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    // Even if attendee fetch fails, attempt to delete the primary documents
                    db.collection(ATTENDEE_COLLECTION).document(userId).delete();
                    db.collection(COLLECTION_NAME).document(userId).delete()
                            .addOnSuccessListener(aVoid -> {
                                if (listener != null) listener.onSuccess();
                            })
                            .addOnFailureListener(err -> {
                                if (listener != null) listener.onError(err);
                            });
                });
    }
}
