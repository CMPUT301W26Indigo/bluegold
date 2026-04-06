package com.eventlottery.model;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents an Admin user who can also act as an Attendee and Event Organizer.
 * Admins are currently stored in the "attendees" collection.
 */
public class Admin extends AbstractUser {
    private static final String TAG = "Admin";
    private static final String COLLECTION_NAME = "admins";

    private Attendee attendee;
    private EventOrganizer eventOrganizer;

    private boolean isAdmin;
    private boolean isAttendee;
    private boolean isEventOrganizer;
    private boolean notification;

    @Exclude
    private final FirebaseFirestore db;

    /**
     * Interface for handling asynchronous admin loading.
     */
    public interface OnAdminLoadedListener {
        /**
         * Called when admin is successfully loaded.
         * @param admin The loaded admin instance.
         */
        void onSuccess(Admin admin);
        
        /**
         * Called when an error occurs during loading.
         * @param e The exception that occurred.
         */
        void onError(Exception e);
    }

    /**
     * Constructs a new Admin with default values and connects to Firestore.
     */
    public Admin() {
        super();
        this.attendee = null;
        this.eventOrganizer = null;
        this.isAdmin = true;
        this.isAttendee = false;
        this.isEventOrganizer = false;
        this.notification = true;

        FirebaseFirestore tempDb = null;
        try {
            tempDb = FirebaseFirestore.getInstance();
        } catch (IllegalStateException e) {
            tempDb = null;
            Log.w(TAG, "Firebase not initialized, Firestore operations will be unavailable");
        }
        this.db = tempDb;
    }

    // Getters and Setters

    /**
     * Sets the attendee's name.
     * Updates the attendee and event organizer's names as well.
     * Saves to Firestore
     * @param name The name to set.
     */
    @Override
    public void setName(String name) {
        this.name = name;
        if (attendee != null) {
            attendee.setName(name);
            attendee.saveToFirebase();
        }
        if (eventOrganizer != null) {
            eventOrganizer.setName(name);
            eventOrganizer.saveToFirebase();
        }
        saveToFirebase();
    }

    /**
     * Sets the attendee's email address.
     * Updates the attendee and event organizer's emails as well.
     * Saves to Firestore
     * @param email The email address to set.
     */
    @Override
    public void setEmail(String email) {
        if (!ValidateEmail.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email;
        if (attendee != null) {
            attendee.setEmail(email);
            attendee.saveToFirebase();
        }
        if (eventOrganizer != null) {
            eventOrganizer.setEmail(email);
            eventOrganizer.saveToFirebase();
        }
        saveToFirebase();
    }

    /**
     * Sets the attendee's phone number.
     * Updates the attendee and event organizer's phone numbers as well.
     * Saves to Firestore
     * @param phoneNumber The phone number to set.
     */
    @Override
    public void setPhoneNumber(String phoneNumber) {
        if (!ValidatePhone.isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.phoneNumber = phoneNumber;
        if (attendee != null) {
            attendee.setPhoneNumber(phoneNumber);
            attendee.saveToFirebase();
        }
        if (eventOrganizer != null) {
            eventOrganizer.setPhoneNumber(phoneNumber);
            eventOrganizer.saveToFirebase();
        }
        saveToFirebase();
    }

    /**
     * Sets the attendee's physical address.
     * Updates the attendee and event organizer's addresses as well.
     * Saves to Firestore
     * @param address The address to set.
     * Todo Throw IllegalArgumentException for invalid format and ensure it can be converted to coordinates.
     */
    @Override
    public void setAddress(String address) {
        this.address = address;
        if (attendee != null) {
            attendee.setAddress(address);
            attendee.saveToFirebase();
        }
        if (eventOrganizer != null) {
            eventOrganizer.setAddress(address);
            eventOrganizer.saveToFirebase();
        }
        saveToFirebase();
    }

    /**
     * Sets the attendee's unique ID.
     * Updates the attendee and event organizer's IDs as well.
     * Saves to Firestore
     * @param deviceID The ID to set (e.g., the Firebase Installation ID).
     */
    @Override
    public void setID(String deviceID) {
        this.deviceID = deviceID;
        if (attendee != null) attendee.setID(deviceID);
        if (eventOrganizer != null) eventOrganizer.setID(deviceID);
    }

    /**
     * Sets the attendee's notification preference.
     * Updates the attendee and event organizer's notification preferences as well.
     * Saves to Firestore
     * @param notification True to enable notifications, false to disable.
     */
    @Override
    public void setNotification(boolean notification) {
        this.notification = notification;
        if (attendee != null) {
            attendee.setNotification(notification);
            attendee.saveToFirebase();
        }
        if (eventOrganizer != null) {
            eventOrganizer.setNotification(notification);
            eventOrganizer.saveToFirebase();
        }
        saveToFirebase();
    }

    /**
     * Returns if the user is an admin boolean.
     * @return true if the user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Returns if the user is an attendee boolean.
     * @return true if the user is an attendee, false otherwise.
     */
    public boolean isAttendee() {
        return isAttendee;
    }

    /**
     * Returns if the user is an event organizer boolean.
     * @return true if the user is an event organizer, false otherwise.
     */
    public boolean isEventOrganizer() {
        return isEventOrganizer;
    }

    /**
     * Gets the attendee associated with this admin.
     * @return The associated Attendee object.
     */
    public Attendee getAttendee() {
        return attendee;
    }

    /**
     * Sets the attendee associated with this admin.
     * @param attendee The Attendee object to set.
     */
    public void setAttendee(Attendee attendee) {
        this.attendee = attendee;
    }

    /**
     * Gets the event organizer associated with this admin.
     * @return The associated EventOrganizer object.
     */
    public EventOrganizer getEventOrganizer() {
        return eventOrganizer;
    }

    /**
     * Sets the event organizer associated with this admin.
     * @param eventOrganizer The EventOrganizer object to set.
     */
    public void setEventOrganizer(EventOrganizer eventOrganizer) {
        this.eventOrganizer = eventOrganizer;
    }

    /**
     * Creates and returns an attendee object for admin to use if one doesn't already exist.
     * @return The created or existing Attendee object.
     */
    public Attendee createAttendee() {
        if (attendee == null) {
            attendee = new Attendee();
            attendee.setID(this.deviceID);
            attendee.setName(this.name);
            attendee.setEmail(this.email);
            attendee.setPhoneNumber(this.phoneNumber);
            attendee.setAddress(this.address);
            attendee.setNotification(this.notification);
            attendee.saveToFirebase();
            isAttendee = true;
            saveToFirebase();
        }
        return attendee;
    }

    /**
     * Creates and returns an event organizer object for admin to use if one doesn't already exist.
     * @return The created or existing EventOrganizer object.
     */
    public EventOrganizer createEventOrganizer() {
        if (eventOrganizer == null) {
            eventOrganizer = new EventOrganizer();
            eventOrganizer.setID(this.deviceID);
            eventOrganizer.setName(this.name);
            eventOrganizer.setEmail(this.email);
            eventOrganizer.setPhoneNumber(this.phoneNumber);
            eventOrganizer.setAddress(this.address);
            eventOrganizer.setNotification(this.notification);
            eventOrganizer.saveToFirebase();
            isEventOrganizer = true;
            saveToFirebase();
        }
        return eventOrganizer;
    }

    /**
     * Synchronizes admin state to Firebase.
     */
    @Override
    public void saveToFirebase() {
        if (db == null) return;
        if (deviceID == null || deviceID.isEmpty()) {
            Log.w(TAG, "Cannot save admin: deviceID is null or empty");
            return;
        }
        db.collection(COLLECTION_NAME).document(deviceID).set(this)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Admin successfully updated on Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating admin on Firebase", e));
    }

    /**
     * Fetches the admin from Firebase.
     * @param listener Callback for completion.
     */
    public void fetchFromFirebase(OnAdminLoadedListener listener) {
        if (db == null) {
            if (listener != null) listener.onError(new Exception("Firebase not initialized"));
            return;
        }
        if (deviceID == null || deviceID.isEmpty()) {
            if (listener != null) listener.onError(new Exception("DeviceID not set"));
            return;
        }
        db.collection("attendees").document(deviceID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        Admin remote = documentSnapshot.toObject(Admin.class);
                        if (remote != null) {
                            this.name = remote.name;
                            this.email = remote.email;
                            this.phoneNumber = remote.phoneNumber;
                            this.address = remote.address;
                            this.attendee = remote.attendee;
                            this.eventOrganizer = remote.eventOrganizer;
                            this.isAttendee = remote.isAttendee;
                            this.isEventOrganizer = remote.isEventOrganizer;
                            this.notification = remote.notification;
                            this.isAdmin = remote.isAdmin;

                            // Re-attach listeners to nested attendee's history
                            if (this.attendee != null && this.attendee.getEventHistory() != null) {
                                for (AttendeeEventHistory history : this.attendee.getEventHistory()) {
                                    history.setOnChangeListener(() -> {
                                        this.attendee.saveToFirebase();
                                        this.saveToFirebase();
                                    });
                                }
                            }

                            if (listener != null) listener.onSuccess(this);
                        } else if (listener != null) {
                            listener.onError(new Exception("Admin document not found"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error deserializing Admin profile", e);
                        if (listener != null) listener.onError(e);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }

    /**
     * Removes an event from the database.
     * Removes event from associated attendee event history and waitlist.
     * @param eventId The ID of the event to remove.
     */
    public void removeEvent(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.get().addOnCompleteListener(doc -> {
            Event event = doc.getResult().toObject(Event.class);
            if (event == null) return;

            WriteBatch batch = db.batch();

            // remove event from organizer's list of events
            String organizerId = event.getOrganizerId();
            if (organizerId != null) {
                DocumentReference organizerRef = db.collection("eventOrganizers").document(organizerId);
                batch.update(organizerRef, "events", FieldValue.arrayRemove(event));
            }

            // remove event from attendee's event history and waitlist
            GuestList guestList = event.getGuestList();
            if (guestList != null) {
                ArrayList<String> attendeeIds = guestList.getAttendeeIds();
                for (String id : attendeeIds) {
                    batch.update(db.collection("attendees").document(id), "eventHistory", FieldValue.arrayRemove(eventId));
                }
            }
            Waitlist waitlist = event.getWaitlist();
            if (waitlist != null) {
                ArrayList<String> waitlistAttendeeIds = waitlist.getAttendeeIds();
                if (waitlistAttendeeIds != null) {
                    for (String id : waitlistAttendeeIds) {
                        batch.update(db.collection("attendees").document(id),
                                "eventHistory", FieldValue.arrayRemove(eventId),
                                "waitListed", FieldValue.arrayRemove(eventId)
                        );
                    }
                }
            }
            
            // Delete the event document
            batch.delete(eventRef);

            batch.commit().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Main event data deleted. Cleaning up sub-collections...");
                deleteCollectionDocs(eventRef.collection("guestList"));
                deleteCollectionDocs(eventRef.collection("waitlist"));
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to remove event", e));
        });
    }

    /**
     * Removes an attendee profile and scrubs them from associated events.
     * @param attendeeId The ID of the attendee profile to remove.
     */
    public void removeAttendeeProfile(String attendeeId) {
        DocumentReference attendeeRef = db.collection("attendees").document(attendeeId);
        attendeeRef.get().addOnCompleteListener(doc -> {
            Attendee attendee = doc.getResult().toObject(Attendee.class);
            if (attendee == null) return;
            
            WriteBatch batch = db.batch();
            
            ArrayList<AttendeeEventHistory> eventHistory = attendee.getEventHistory();
            ArrayList<String> waitListed = attendee.getWaitListed();
            if (waitListed != null) {
                for (String eventId : waitListed) {
                    DocumentReference eventRef = db.collection("events").document(eventId);
                    batch.delete(eventRef.collection("waitlist").document(attendeeId));
                    batch.delete(eventRef.collection("guestList").document(attendeeId));
                    batch.update(eventRef, "waitlistCount", FieldValue.increment(-1));
                }
            }
            if (eventHistory != null) {
                for (AttendeeEventHistory history : eventHistory) {
                    DocumentReference eventRef = db.collection("events").document(history.getEventID());
                    batch.delete(eventRef.collection("guestList").document(attendeeId));
                    batch.update(eventRef, "confirmedCount", FieldValue.increment(-1));
                }
            }
            batch.delete(attendeeRef);
            
            batch.commit()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Attendee profile removed successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove attendee profile", e));
        });

    }

    /**
     * Removes an event organizer's profile from the database.
     * @param eventOrganizerId The ID of the organizer profile to remove.
     */
    public void removeEventOrganizerProfile(String eventOrganizerId) {
        DocumentReference eventOrganizerRef = db.collection("eventOrganizers").document(eventOrganizerId);
        eventOrganizerRef.get().addOnCompleteListener(doc -> {
            EventOrganizer eventOrganizer = doc.getResult().toObject(EventOrganizer.class);
            if (eventOrganizer == null) return;
            ArrayList<Event> events = eventOrganizer.getEvents();
            if (events != null) {
                for (Event event : events) {
                    removeEvent(event.getId());
                }
            }
            eventOrganizerRef.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Organizer profile removed"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove organizer profile", e));
        });
    }

    /**
     * Deletes all documents in a collection.
     * @param collection The collection reference to delete.
     */
    private void deleteCollectionDocs(CollectionReference collection) {
        collection.get().addOnSuccessListener(querySnapshot -> {
            if (querySnapshot.isEmpty()) return;
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : querySnapshot) {
                batch.delete(doc.getReference());
            }
            batch.commit();
        });
    }

    /**
     * Removes the poster image associated with an event.
     * @param eventId The ID of the event whose image is to be removed.
     * @return A Task representing the deletion operation.
     */
    public Task<Void> removeImage(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        return eventRef.update("posterImageUrl", null);
    }

    /**
     * Removes all comments associated with an event.
     * @param eventId The ID of the event whose comments are to be removed.
     */
    public void removeEventComments(String eventId) {

    }

    /**
     * Migrates existing images to a new collection structure.
     */
    public void migrateExistingImages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Get all documents from your original 'events' collection
        db.collection("events").get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot eventDoc : querySnapshot) {
                String eventId = eventDoc.getId(); // This is the Document Name
                String url = eventDoc.getString("imageUrl");

                if (url != null && !url.isEmpty()) {
                    // 2. Prepare the data for the new collection
                    HashMap<String, Object> imageData = new HashMap<>();
                    imageData.put("url", url);
                    imageData.put("eventId", eventId); // Storing it as a field too

                    // 3. Create the document in the NEW collection
                    // We use .document(eventId) to set the Document Name
                    db.collection("imageCollection").document(eventId)
                            .set(imageData)
                            .addOnSuccessListener(aVoid -> Log.d("Migration", "Created image doc for: " + eventId))
                            .addOnFailureListener(e -> Log.e("Migration", "Failed: " + eventId, e));
                }
            }
        });
    }
}
