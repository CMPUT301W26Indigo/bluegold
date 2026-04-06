package com.eventlottery.model;

import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Model class representing an image stored in Firestore.
 * This class handles the storage and retrieval of image metadata,
 * specifically the image URL and the associated event ID.
 */
public class Image {
    private static final String TAG = "Images";
    private static final String COLLECTION_NAME = "eventImages";
    private String url;
    private String eventId;

    @Exclude
    private final FirebaseFirestore db;

    /**
     * Default constructor required for Firebase deserialization.
     * Initializes the Firestore instance and sets default null values for fields.
     */
    public Image() {
        this.url = null;
        this.eventId = null;
        FirebaseFirestore tempDb = null;
        try {
            tempDb = FirebaseFirestore.getInstance();
        } catch (IllegalStateException e) {
            tempDb = null;
            Log.w(TAG, "Firebase not initialized, Firestore operations will be unavailable");
        }
        this.db = tempDb;
    }

    /**
     * Constructs an Image with a specified URL and event ID.
     * @param url The URL or Base64 string of the image.
     * @param eventId The unique identifier of the event this image belongs to.
     */
    public Image(String url, String eventId) {
        this();
        this.url = url;
        this.eventId = eventId;
    }

    /**
     * Interface for handling asynchronous image loading from Firebase.
     */
    public interface OnImageLoadedListener {
        /**
         * Called when the image has been successfully loaded.
         * @param image The loaded Image object.
         */
        void onSuccess(Image image);

        /**
         * Called when an error occurs during image loading.
         * @param e The exception that occurred.
         */
        void onError(Exception e);
    }

    /**
     * Gets the URL or Base64 string of the image.
     * @return The image URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL or Base64 string of the image.
     * @param url The image URL to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the ID of the event associated with this image.
     * @return The associated event ID.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the ID of the event associated with this image.
     * @param eventId The event ID to set.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Synchronizes the current state of the Image object to Firebase.
     * Requires the eventId to be set as it is used as the document ID.
     */
    public void saveToFirebase() {
        if (db == null) return;
        if (eventId == null || eventId.isEmpty()) {
            Log.w(TAG, "Cannot save image: id is null or empty");
            return;
        }
        db.collection(COLLECTION_NAME).document(eventId).set(this)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Image successfully updated on Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating image on Firebase", e));
    }

    /**
     * Fetches the image metadata from Firebase using the eventId.
     * @param listener Callback for completion, providing the loaded image or an error.
     */
    public void fetchFromFirebase(OnImageLoadedListener listener) {
        if (db == null) {
            if (listener != null) listener.onError(new Exception("Firebase not initialized"));
            return;
        }
        if (eventId == null || eventId.isEmpty()) {
            if (listener != null) listener.onError(new Exception("EventId not set"));
            return;
        }
        db.collection(COLLECTION_NAME).document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Image remote = documentSnapshot.toObject(Image.class);
                    if (remote != null) {
                        this.url = remote.url;
                        this.eventId = remote.eventId;

                        if (listener != null) listener.onSuccess(this);
                    } else if (listener != null) {
                        listener.onError(new Exception("Image document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e);
                });
    }
}
