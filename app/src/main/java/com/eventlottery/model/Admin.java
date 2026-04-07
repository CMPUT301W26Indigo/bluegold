package com.eventlottery.model;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents an Admin user who can also act as an Attendee and Event Organizer.
 * Admins are stored in the "attendees" collection.
 */
public class Admin extends AbstractUser {
    private static final String TAG = "Admin";
    private static final String COLLECTION_NAME = "attendees";

    private Attendee attendee;
    private EventOrganizer eventOrganizer;

    private boolean isAttendee;
    private boolean isEventOrganizer;

    @Exclude
    private final FirebaseFirestore db;

    /**
     * Interface for handling asynchronous admin profile loading.
     */
    public interface OnAdminLoadedListener {
        void onSuccess(Admin admin);
        void onError(Exception e);
    }

    /**
     * Interface for handling asynchronous event fetching.
     */
    public interface OnEventsFetchedListener {
        /**
         * Called when events are successfully fetched.
         * @param events ArrayList of fetched Event objects.
         */
        void onSuccess(ArrayList<Event> events);

        /**
         * Called when an error occurs during event fetching.
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

    public boolean isAdmin() { return isAdmin; }
    public boolean isAttendee() { return isAttendee; }
    public boolean isEventOrganizer() { return isEventOrganizer; }

    public Attendee getAttendee() { return attendee; }
    public void setAttendee(Attendee attendee) { this.attendee = attendee; }

    public EventOrganizer getEventOrganizer() { return eventOrganizer; }
    public void setEventOrganizer(EventOrganizer eventOrganizer) { this.eventOrganizer = eventOrganizer; }

    /**
     * Creates an attendee object for this admin if one doesn't exist.
     * @return The attendee object.
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
     * Creates an event organizer object for this admin if one doesn't exist.
     * @return The event organizer object.
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
     * Fetches the admin profile from Firebase.
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
        db.collection(COLLECTION_NAME).document(deviceID).get()
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
     * Fetches all events from Firestore where the given profileId is the organizer.
     * @param profileId The ID of the organizer.
     * @param listener Callback returning an ArrayList of events.
     */
    public void getEvents(String profileId, OnEventsFetchedListener listener) {
        if (db == null) {
            if (listener != null) listener.onError(new Exception("Firebase not initialized"));
            return;
        }
        db.collection("events")
                .whereEqualTo("organizerId", profileId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            event.setId(document.getId());
                            events.add(event);
                        }
                    }
                    if (listener != null) listener.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }

    /**
     * Fetches all events for a given organizer profileID and deletes them if no co-organizer is specified.
     * @param profileId The ID of the organizer.
     */
    public void removeEvents(String profileId) {
        getEvents(profileId, new OnEventsFetchedListener() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                for (Event event : events) {
                    // Delete the event if coOrganizerIds is null or empty
                    if (event.getCoOrganizerIds() == null || event.getCoOrganizerIds().isEmpty()) {
                        removeEvent(event.getId());
                    } else {
                        List<String> coOrganizerIds = event.getCoOrganizerIds();
                        String newOrganizerId = coOrganizerIds.get(0);
                        coOrganizerIds.remove(0);
                        
                        event.setOrganizerId(newOrganizerId);
                        event.setCoOrganizerIds(coOrganizerIds);

                        db.collection("events").document(event.getId())
                                .update("organizerId", newOrganizerId, "coOrganizerIds", coOrganizerIds)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Event organizer updated successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating event organizer", e));
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching events for removal", e);
            }
        });
    }

    /**
     * Removes a single event and cleans up associated references.
     * @param eventId The ID of the event to remove.
     */
    public void removeEvent(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.get().addOnCompleteListener(doc -> {
            Event event = doc.getResult().toObject(Event.class);
            if (event == null) return;

            WriteBatch batch = db.batch();

            String organizerId = event.getOrganizerId();
            if (organizerId != null) {
                DocumentReference organizerRef = db.collection("eventOrganizers").document(organizerId);
                batch.update(organizerRef, "events", FieldValue.arrayRemove(event));
            }

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
            
            batch.delete(eventRef);

            batch.commit().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Main event data deleted. Cleaning up sub-collections...");
                deleteCollectionDocs(eventRef.collection("guestList"));
                deleteCollectionDocs(eventRef.collection("waitlist"));
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to remove event", e));
        });
    }

    /**
     * Removes an attendee profile and cleans up their associations with events.
     * @param attendeeId The ID of the attendee to remove.
     */
    public void removeProfile(String attendeeId) {
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
     * @param eventOrganizerId The ID of the organizer.
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
     * @param collection The collection to clear.
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
     * Removes an image associated with an event.
     * @param eventId The ID of the event.
     * @return A Task representing the asynchronous operation.
     */
    public Task<Void> removeImage(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference imageRef = eventRef.collection("images").document("main");
        imageRef.update("imageUrl", null);
        return eventRef.update("posterImageUrl", null);
    }
}
