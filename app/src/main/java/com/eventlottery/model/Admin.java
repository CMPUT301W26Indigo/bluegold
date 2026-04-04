package com.eventlottery.model;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Represents an Admin user who can also act as an Attendee and Event Organizer.
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


    public interface OnAdminLoadedListener {
        void onSuccess(Admin admin);
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
        //saveToFirebase(); - needed??
    }

    /**
     * Sets the attendee's profile image URL.
     * Updates the attendee and event organizer's profile image URLs as well.
     * Saves to Firestore
     * @param profileImageUrl
     */
    @Override
    public void setProfileImageUrl(String profileImageUrl) {
        super.setProfileImageUrl(profileImageUrl);
        if (attendee != null) {
            attendee.setProfileImageUrl(profileImageUrl);
            attendee.saveToFirebase();
        }
        if (eventOrganizer != null) {
            eventOrganizer.setProfileImageUrl(profileImageUrl);
            eventOrganizer.saveToFirebase();
        }
        saveToFirebase();
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
     * @return isAdmin.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Returns if the user is an attendee boolean.
     * @return isAttendee
     */
    public boolean isAttendee() {
        return isAttendee;
    }

    /**
     * Returns if the user is an event organizer boolean.
     * @return isEventOrganizer
     */
    public boolean isEventOrganizer() {
        return isEventOrganizer;
    }

    /**
     * Gets the attendee associated with this admin.
     * @return attendee
     */
    public Attendee getAttendee() {
        return attendee;
    }

    /**
     * Sets the attendee associated with this admin.
     * @param attendee
     */
    public void setAttendee(Attendee attendee) {
        this.attendee = attendee;
    }

    /**
     * Gets the event organizer associated with this admin.
     * @return
     */
    public EventOrganizer getEventOrganizer() {
        return eventOrganizer;
    }

    /**
     * Sets the event organizer associated with this admin.
     * @param eventOrganizer
     */
    public void setEventOrganizer(EventOrganizer eventOrganizer) {
        this.eventOrganizer = eventOrganizer;
    }

    /**
     * Creates and returns an attendee object for admin to use if one doesn't already exist
     * @return attendee
     */
    public Attendee createAttendee() {
        if (attendee == null) {
            attendee = new Attendee();
            attendee.setID(this.deviceID);
            attendee.setName(this.name);
            attendee.setEmail(this.email);
            attendee.setPhoneNumber(this.phoneNumber);
            attendee.setAddress(this.address);
            attendee.setProfileImageUrl(this.profileImageUrl);
            attendee.setNotification(this.notification);
            attendee.saveToFirebase();
            isAttendee = true;
            saveToFirebase();
        }
        return attendee;
    }

    /**
     * Creates and returns an event organizer object for admin to use if one doesn't already exist
     * @return eventOrganizer
     */
    public EventOrganizer createEventOrganizer() {
        if (eventOrganizer == null) {
            eventOrganizer = new EventOrganizer();
            eventOrganizer.setID(this.deviceID);
            eventOrganizer.setName(this.name);
            eventOrganizer.setEmail(this.email);
            eventOrganizer.setPhoneNumber(this.phoneNumber);
            eventOrganizer.setAddress(this.address);
            eventOrganizer.setProfileImageUrl(this.profileImageUrl);
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
        db.collection(COLLECTION_NAME).document(deviceID).get()
                .addOnSuccessListener(documentSnapshot -> {
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

                        // Re-attach listeners to nested attendee's history
                        if (this.attendee != null && this.attendee.getEventHistory() != null) {
                            for (AttendeeEventHistory history : this.attendee.getEventHistory()) {
                                history.setOnChangeListener(() -> {
                                    this.attendee.saveToFirebase();
                                    this.saveToFirebase();
                                });
                            }
                        }
                        if (remote.isEventOrganizer) isEventOrganizer = true;

                        if (listener != null) listener.onSuccess(this);
                    } else if (listener != null) {
                        listener.onError(new Exception("Admin document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }

    /**
     * Removes an event from the database
     * Removes event from associated attendee event history and waitlist
     * @param eventId
     */
    public void removeEvent(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.get().addOnCompleteListener(doc -> {
            Event event = doc.getResult().toObject(Event.class);
            if (event == null) return;
            GuestList guestList = event.getGuestList();
            if (guestList != null) {
                ArrayList<String> attendeeIds = guestList.getAttendeeIds();
                for (String id : attendeeIds) {
                    db.collection("attendees").document(id).update("eventHistory", FieldValue.arrayRemove(eventId));
                }
            }
            Waitlist waitlist = event.getWaitlist();
            if (waitlist != null) {
                ArrayList<String> waitlistAttendeeIds = waitlist.getAttendeeIds();
                if (waitlistAttendeeIds != null) {
                    for (String id : waitlistAttendeeIds) {
                        db.collection("attendees").document(id).update(
                                "eventHistory", FieldValue.arrayRemove(eventId),
                                "waitListed", FieldValue.arrayRemove(eventId)
                        );
                    }
                }
            }
            CollectionReference guestListRef = eventRef.collection("guestList");
            // Delete all documents inside the guestList collection
            guestListRef.get().addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot document : querySnapshot) {
                    document.getReference().delete();
                }
                CollectionReference waitlistRef = eventRef.collection("waitlist");
                // Delete all documents inside the waitlList collection
                waitlistRef.get().addOnSuccessListener(querySnapshotAgain -> {
                    for (DocumentSnapshot document : querySnapshot) {
                        document.getReference().delete();
                    }
                    eventRef.delete();
                });
            });
        });
    }

    public void removeAttendeeProfile(String attendeeId) {
        DocumentReference attendeeRef = db.collection("attendees").document(attendeeId);
        attendeeRef.get().addOnCompleteListener(doc -> {
            Attendee attendee = doc.getResult().toObject(Attendee.class);
            if (attendee == null) return;
            ArrayList<AttendeeEventHistory> eventHistory = attendee.getEventHistory();
            ArrayList<String> waitListed = attendee.getWaitListed();
            if (waitListed != null) {
                for (String eventId : waitListed) {
                    DocumentReference eventRef = db.collection("events").document(eventId);
                    eventRef.collection("waitlist").document(attendeeId).delete();
                    eventRef.collection("guestList").document(attendeeId).delete();
                    db.collection("events").document(eventId).update("waitlistCount", FieldValue.increment(-1));
                }
            }
            if (eventHistory != null) {
                for (AttendeeEventHistory history : eventHistory) {
                    DocumentReference eventRef = db.collection("events").document(history.getEventID());
                    eventRef.collection("guestList").document(attendeeId).delete();
                    db.collection("events").document(history.getEventID()).update("confirmedCount", FieldValue.increment(-1));

                }
            }
            attendeeRef.delete();
        });

    }

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
            eventOrganizerRef.delete();
        });
    }

    public void removeImage(String imageUrl) {

    }

    public void removeEventComments(String eventId) {

    }
}
