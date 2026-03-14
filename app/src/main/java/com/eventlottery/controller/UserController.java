package com.eventlottery.controller;

import com.eventlottery.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Controller for User-related operations.
 * Part of the 'Controller' in MVC.
 */
public class UserController {
    private final FirebaseFirestore db;
    private final String COLLECTION_NAME = "users";

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
     * Before deletion, it calls methods to leave waitlists and
     * update guest list status to "decline".
     *
     * @param userId   The ID of the user to delete.
     * @param listener The listener for success or error callbacks.
     */
    public void deleteUser(String userId, OnUserOperationListener listener) {
        // TODO: Remove user from waitlist using Waitlist.removeAttendee(userId)
        // TODO: Set user's status to "declined" in guest list using GuestList.changeAttendeeStatus(userId, "declined")

        // Delete the main user profile
        db.collection(COLLECTION_NAME).document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
    }
}
