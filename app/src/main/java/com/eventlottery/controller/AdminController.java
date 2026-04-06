package com.eventlottery.controller;

import android.util.Log;

import com.eventlottery.model.Attendee;
import com.eventlottery.model.Event;
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
    private static final String TAG = "AdminController";
    private final FirebaseFirestore db;

    /**
     * Interface for handling data loaded from Firestore.
     * @param <T> The type of data to be loaded.
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
     * Interface for handling operations on data.
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
     * Constructor for AdminController.
     */
    public AdminController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches all users from the 'users' collection.
     * @param listener Callback for the loaded users.
     */
    public void getAllUsers(OnDataLoadedListener<User> listener) {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                user.setId(document.getId());
                                users.add(user);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user document: " + document.getId(), e);
                        }
                    }
                    listener.onDataLoaded(users);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Fetches all attendees from the 'attendees' collection.
     * @param listener Callback for the loaded attendees.
     */
    public void getAllAttendees(OnDataLoadedListener<Attendee> listener) {
        db.collection("attendees")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Attendee> attendees = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Attendee attendee = document.toObject(Attendee.class);
                            if (attendee != null) {
                                attendee.setID(document.getId());
                                attendees.add(attendee);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing attendee document: " + document.getId(), e);
                        }
                    }
                    listener.onDataLoaded(attendees);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Deletes a user from the system.
     * @param userId The ID of the user to be deleted.
     * @param listener Callback for the deletion result.
     */
    public void deleteUser(String userId, OnOperationListener listener) {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Fetches flagged events for review.
     * @param listener Callback for the flagged events.
     */
    public void getFlaggedEvents(OnDataLoadedListener<Event> listener) {
        db.collection("events")
                .whereEqualTo("isFlagged", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Event event = document.toObject(Event.class);
                            if (event != null) {
                                event.setId(document.getId());
                                events.add(event);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing event document: " + document.getId(), e);
                        }
                    }
                    listener.onDataLoaded(events);
                })
                .addOnFailureListener(listener::onError);
    }
}
