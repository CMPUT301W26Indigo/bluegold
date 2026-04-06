package com.eventlottery.controller;

import android.util.Log;

import com.eventlottery.model.Attendee;
import com.eventlottery.model.AttendeeEventHistory;
import com.eventlottery.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for User-related operations.
 * Part of the 'Controller' in MVC.
 */
public class UserController {
    private static final String TAG = "UserController";
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
     * If the user was confirmed for an event, it triggers cleanup for a redraw.
     *
     * @param userId   The ID of the user to delete.
     * @param listener The listener for success or error callbacks.
     */
    public void deleteUser(String userId, OnUserOperationListener listener) {
        Log.d(TAG, "Starting thorough deletion for user: " + userId);

        // 1. Fetch Attendee data to find associated events for cleanup
        db.collection(ATTENDEE_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Task<?>> cleanupTasks = new ArrayList<>();

                    if (documentSnapshot.exists()) {
                        Attendee attendee = documentSnapshot.toObject(Attendee.class);
                        if (attendee != null) {
                            // A. Scrub from Waitlists (Sub-collection & Top-level legacy)
                            if (attendee.getWaitListed() != null) {
                                for (String eventId : attendee.getWaitListed()) {
                                    // Remove from modern waitlist sub-collection
                                    cleanupTasks.add(db.collection("events").document(eventId)
                                            .collection("waitlist").document(userId).delete());
                                    // Remove from legacy top-level Waitlist document
                                    cleanupTasks.add(db.collection("waitlists").document(eventId)
                                            .update("attendeeIds", FieldValue.arrayRemove(userId)));
                                }
                            }

                            // B. Scrub from Guest Lists & Handle Redraws (Sub-collection & Top-level legacy)
                            if (attendee.getEventHistory() != null) {
                                for (AttendeeEventHistory history : attendee.getEventHistory()) {
                                    String eventId = history.getEventID();
                                    // Change status to "declined" and signal redraw if they were confirmed
                                    cleanupTasks.add(handleGuestListDeclineAndRedraw(eventId, userId));
                                    
                                    // Remove from legacy top-level GuestList document
                                    Map<String, Object> removeMap = new HashMap<>();
                                    removeMap.put("attendees." + userId, FieldValue.delete());
                                    cleanupTasks.add(db.collection("guestlists").document(eventId).update(removeMap));
                                }
                            }

                            // C. Wipe user's personal 'Selected' sub-collection
                            cleanupTasks.add(db.collection(ATTENDEE_COLLECTION).document(userId)
                                    .collection("Selected").get().continueWithTask(task -> {
                                        if (!task.isSuccessful() || task.getResult() == null) return Tasks.forResult(null);
                                        List<Task<Void>> deleteSubTasks = new ArrayList<>();
                                        for (DocumentSnapshot subDoc : task.getResult()) {
                                            deleteSubTasks.add(subDoc.getReference().delete());
                                        }
                                        return Tasks.whenAll(deleteSubTasks);
                                    }));
                        }
                    }

                    // Broad safety cleanup for legacy waitlists (Search and Remove)
                    cleanupTasks.add(db.collection("waitlists").whereArrayContains("attendeeIds", userId).get().continueWithTask(task -> {
                        if (!task.isSuccessful() || task.getResult() == null) return Tasks.forResult(null);
                        List<Task<Void>> subTasks = new ArrayList<>();
                        for (DocumentSnapshot d : task.getResult()) {
                            subTasks.add(d.getReference().update("attendeeIds", FieldValue.arrayRemove(userId)));
                        }
                        return Tasks.whenAll(subTasks);
                    }));

                    // 2. Wait for all cleanup tasks to finish before the final profile wipe
                    Tasks.whenAllComplete(cleanupTasks).addOnCompleteListener(allCleanupTask -> {
                        // 3. Final deletion of the primary Firestore documents
                        Task<Void> deleteAttendee = db.collection(ATTENDEE_COLLECTION).document(userId).delete();
                        Task<Void> deleteUser = db.collection(COLLECTION_NAME).document(userId).delete();
                        
                        Tasks.whenAll(deleteAttendee, deleteUser).addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Cleanup and deletion successful for: " + userId);
                            if (listener != null) listener.onSuccess();
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Final wipe failed", e);
                            if (listener != null) listener.onError(e);
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    // Fallback: If initial fetch fails, attempt final wipe anyway
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

    /**
     * Changes user status to "declined" in an event's guest list sub-collection.
     * If user was confirmed, it decrements confirmedCount to open a spot for redraw.
     */
    private Task<Void> handleGuestListDeclineAndRedraw(String eventId, String userId) {
        return db.collection("events").document(eventId).collection("guestList").document(userId).get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String status = task.getResult().getString("status");
                        List<Task<Void>> subTasks = new ArrayList<>();
                        
                        // Change status to "declined" per requirement
                        subTasks.add(db.collection("events").document(eventId)
                                .collection("guestList").document(userId).update("status", "declined"));

                        // Signal Redraw: if they were confirmed, opening a spot signals a vacancy
                        if ("confirmed".equals(status)) {
                            subTasks.add(db.collection("events").document(eventId)
                                    .update("confirmedCount", FieldValue.increment(-1)));
                        }
                        return Tasks.whenAll(subTasks);
                    }
                    return Tasks.forResult(null);
                });
    }
}
