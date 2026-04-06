package com.eventlottery.model;

import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Model class representing an image stored in Firestore.
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

    public Image(String url, String eventId) {
        this();
        this.url = url;
        this.eventId = eventId;
    }

    public interface OnImageLoadedListener {
        void onSuccess(Image image);
        void onError(Exception e);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

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
     * Fetches the image from Firebase.
     * @param listener Callback for completion.
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
