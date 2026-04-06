package com.eventlottery.controller;

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
    public void getFlaggedEvents(OnDataLoadedListener<Event> listener) {
        db.collection("events")
                .whereEqualTo("isFlagged", true)
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
