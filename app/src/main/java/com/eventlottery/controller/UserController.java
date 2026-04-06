package com.eventlottery.controller;

import android.util.Log;

import com.eventlottery.model.Attendee;
import com.eventlottery.model.AttendeeEventHistory;
import com.eventlottery.model.GuestList;
import com.eventlottery.model.User;
import com.eventlottery.model.Waitlist;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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
     * If the user was confirmed for an event, it triggers a spot opening for a redraw.
     *
     * @param userId   The ID of the user to delete.
     * @param listener The listener for success or error callbacks.
     */
    public void deleteUser(String userId, OnUserOperationListener listener) {
        Log.d(TAG, "Starting robust deletion for user: " + userId);

        // 1. Fetch Attendee data to identify which events need cleaning up
        db.collection(ATTENDEE_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Task<?>> cleanupTasks = new ArrayList<>();

                    if (documentSnapshot.exists()) {
                        Attendee attendee = documentSnapshot.toObject(Attendee.class);
                        if (attendee != null) {
                            // Queue removal from waitlists (Sub-collection & Top-level)
                            if (attendee.getWaitListed() != null) {
                                for (String eventId : attendee.getWaitListed()) {
                                    cleanupTasks.add(db.collection("events").document(eventId)
                                            .collection("waitlist").document(userId).delete());
                                    cleanupTasks.add(scrubFromWaitlistDocument(eventId, userId));
                                }
                            }

                            // Queue removal from guest lists (Sub-collection & Top-level)
                            if (attendee.getEventHistory() != null) {
                                for (AttendeeEventHistory history : attendee.getEventHistory()) {
                                    String eventId = history.getEventID();
                                    cleanupTasks.add(handleGuestListScrubAndRedraw(eventId, userId));
                                    cleanupTasks.add(scrubFromGuestListDocument(eventId, userId));
                                }
                            }

                            // Queue removal of user's personal 'Selected' sub-collection
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

                    // 2. Wait for all cleanup tasks to complete
                    Tasks.whenAllComplete(cleanupTasks).addOnCompleteListener(allCleanupTask -> {
                        // 3. Final wipe of the primary profile documents
                        db.collection(ATTENDEE_COLLECTION).document(userId).delete();
                        db.collection(COLLECTION_NAME).document(userId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Cleanup and deletion successful for: " + userId);
                                    if (listener != null) listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Final wipe failed", e);
                                    if (listener != null) listener.onError(e);
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    // Fallback: If attendee fetch fails, attempt final wipe anyway
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
     * Removes user from guest list sub-collection and signals a redraw if they were confirmed.
     */
    private Task<Void> handleGuestListScrubAndRedraw(String eventId, String userId) {
        return db.collection("events").document(eventId).collection("guestList").document(userId).get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String status = task.getResult().getString("status");
                        List<Task<Void>> subTasks = new ArrayList<>();

                        // Delete the guest entry
                        subTasks.add(db.collection("events").document(eventId)
                                .collection("guestList").document(userId).delete());

                        // If confirmed, decrement confirmedCount to trigger a spot opening for redraw
                        if ("confirmed".equals(status)) {
                            subTasks.add(db.collection("events").document(eventId)
                                    .update("confirmedCount", FieldValue.increment(-1)));
                        }
                        return Tasks.whenAll(subTasks);
                    }
                    return Tasks.forResult(null);
                });
    }

    /**
     * Helper task to remove a user from a top-level Waitlist document.
     */
    private Task<Void> scrubFromWaitlistDocument(String eventId, String userId) {
        return db.collection("waitlists").document(eventId).get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Waitlist wl = task.getResult().toObject(Waitlist.class);
                if (wl != null && wl.getAttendeeIds() != null && wl.getAttendeeIds().contains(userId)) {
                    wl.removeAttendee(userId);
                    return db.collection("waitlists").document(eventId).set(wl);
                }
            }
            return Tasks.forResult(null);
        });
    }

    /**
     * Helper task to remove a user from a top-level GuestList document.
     */
    private Task<Void> scrubFromGuestListDocument(String eventId, String userId) {
        return db.collection("guestlists").document(eventId).get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                GuestList gl = task.getResult().toObject(GuestList.class);
                if (gl != null && gl.getAttendees() != null && gl.getAttendees().containsKey(userId)) {
                    gl.removeAttendee(userId);
                    return db.collection("guestlists").document(eventId).set(gl);
                }
            }
            return Tasks.forResult(null);
        });
    }
}
