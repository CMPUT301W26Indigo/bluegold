package com.eventlottery.controller;

import com.eventlottery.model.EventTemp;
import com.eventlottery.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Admin-related operations.
 * Part of the 'Controller' in MVC.
 */
public class AdminController {
    private final FirebaseFirestore db;

    public interface OnDataLoadedListener<T> {
        void onDataLoaded(List<T> data);
        void onError(Exception e);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onError(Exception e);
    }

    public AdminController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches all users for administration.
     */
    public void getAllUsers(OnDataLoadedListener<User> listener) {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setId(document.getId());
                        users.add(user);
                    }
                    listener.onDataLoaded(users);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Deletes a user from the system.
     */
    public void deleteUser(String userId, OnOperationListener listener) {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Fetches flagged events for review.
     */
    public void getFlaggedEvents(OnDataLoadedListener<EventTemp> listener) {
        db.collection("events")
                .whereEqualTo("isFlagged", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventTemp> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        EventTemp event = document.toObject(EventTemp.class);
                        event.setId(document.getId());
                        events.add(event);
                    }
                    listener.onDataLoaded(events);
                })
                .addOnFailureListener(listener::onError);
    }
}
