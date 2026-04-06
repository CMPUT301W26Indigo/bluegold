package com.eventlottery.controller;

import android.util.Log;

import com.eventlottery.model.Attendee;
import com.eventlottery.model.Event;
import com.eventlottery.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

import com.eventlottery.model.Comment;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
        void onDataLoaded(List<T> data);
        void onError(Exception e);
    }

    /**
     * Interface for handling operations on data.
     */
    public interface OnOperationListener {
        void onSuccess();
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
     */
    public void deleteUser(String userId, OnOperationListener listener) {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    /**
     * Fetches flagged events for review.
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

    public void getAllEvents(OnDataLoadedListener<Event> listener) {
        db.collection("events")
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
     * Fetches all comments for a specific event
     * Used by admins to review comments
     *
     * @param eventId  The ID of the event
     * @param listener Callback that returns list of comments
     */
    public void getComments(String eventId, OnDataLoadedListener<Comment> listener) {
        db.collection("events").document(eventId)
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
                    listener.onDataLoaded(comments);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Deletes a specific comment from an event's comments subcollection
     * Used by admins to remove comments
     *
     * @param eventId   The ID of the event
     * @param commentId The ID of the comment to delete
     * @param listener  Callback for completion
     */
    public void deleteComment(String eventId, String commentId, OnOperationListener listener) {
        db.collection("events").document(eventId)
                .collection("comments").document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

}
