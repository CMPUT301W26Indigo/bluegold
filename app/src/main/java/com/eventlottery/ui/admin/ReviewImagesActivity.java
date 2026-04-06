package com.eventlottery.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.eventlottery.databinding.ActivityReviewImagesBinding;
import com.eventlottery.model.AbstractUser;
import com.eventlottery.model.Admin;
import com.eventlottery.model.Image;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.eventlottery.ui.adapters.ImageAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity for Admins to review and delete images from events.
 * Identifies the current user and fetches their Admin profile to allow reviewing images.
 */
public class ReviewImagesActivity extends AppCompatActivity implements ImageAdapter.OnImageClickListener {
    private static final String TAG = "ReviewImagesActivity";
    private ActivityReviewImagesBinding binding;
    private ImageAdapter adapter;
    private FirebaseFirestore db;
    private Admin admin;

    /**
     * Called when the activity is first created. Initializes the view, toolbar, 
     * and triggers admin authentication and image fetching.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}. <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity started");
        
        try {
            binding = ActivityReviewImagesBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            Log.d(TAG, "onCreate: Layout inflated and set");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Failed to inflate layout", e);
            Toast.makeText(this, "Failed to load layout", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        db = FirebaseFirestore.getInstance();
        
        setupRecyclerView();
        initializeAdminAndFetchImages();

        migrateExistingImages(); // Migrate existing images to eventImages
    }

    /**
     * Sets up the toolbar with a back button and listener to close the activity.
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Handles the up navigation action.
     * @return true to indicate the event was handled.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Initializes the Admin object for the current user and then fetches images.
     * Uses the device's Firebase ID to identify the user and verify admin status.
     */
    private void initializeAdminAndFetchImages() {
        Log.d(TAG, "initializeAdminAndFetchImages: Getting Firebase ID");
        AbstractUser.getFirebaseId().addOnSuccessListener(id -> {
            Log.d(TAG, "Current user ID: " + id);
            admin = new Admin();
            admin.setID(id);
            
            // Fetch the admin profile from Firebase to ensure we have the latest data
            admin.fetchFromFirebase(new Admin.OnAdminLoadedListener() {
                @Override
                public void onSuccess(Admin loadedAdmin) {
                    admin = loadedAdmin;
                    Log.d(TAG, "Admin profile loaded successfully");
                    fetchImages(); // Fetch images only after admin profile is ready
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error fetching admin profile, using default admin object", e);
                    // Even if fetch fails, we proceed with the default admin object 
                    // which has the ID set, as long as the user is intended to be an admin.
                    fetchImages();
                }
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting Firebase ID", e);
            Toast.makeText(this, "Error identifying user", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Configures the RecyclerView with a GridLayoutManager and attaches the ImageAdapter.
     */
    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Initializing adapter");
        adapter = new ImageAdapter(this);
        binding.rvReviewImages.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvReviewImages.setAdapter(adapter);
    }

    /**
     * Fetches all images from the 'eventImages' collection in Firestore.
     * Updates the adapter's data set upon success.
     */
    private void fetchImages() {
        Log.d(TAG, "fetchImages: Fetching from eventImages collection");
        db.collection("eventImages")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "fetchImages: Success, found " + queryDocumentSnapshots.size() + " documents");
                    List<Image> imageList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Image image = document.toObject(Image.class);
                            if (image != null) {
                                imageList.add(image);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing Image document: " + document.getId(), e);
                        }
                    }
                    Log.d(TAG, "fetchImages: Displaying " + imageList.size() + " images");
                    adapter.setImages(imageList);
                    if (imageList.isEmpty()) {
                        Toast.makeText(this, "No images found to review", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching images", e);
                    Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Callback method from ImageAdapter when an image is clicked.
     * Shows a confirmation dialog for image deletion.
     * @param image The Image object that was clicked.
     */
    @Override
    public void onImageClick(Image image) {
        // When an image is clicked, confirm deletion
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to remove this image from the event and database?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(image))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Removes an image from both the associated Event document and the 'eventImages' collection.
     * @param image The Image object to be deleted.
     */
    private void deleteImage(Image image) {
        if (admin == null) {
            Toast.makeText(this, "Admin not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "deleteImage: Deleting image for event: " + image.getEventId());
        // Use the Admin model to remove the image from the Event document
        admin.removeImage(image.getEventId())
                .addOnSuccessListener(aVoid -> {
                    // Remove the document from eventImages collection
                    db.collection("eventImages").document(image.getEventId()).delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Image removed successfully", Toast.LENGTH_SHORT).show();
                                fetchImages(); // Refresh the list
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting from eventImages", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting image from event", e);
                    Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Helper to migrate existing images from the 'events' collection to 'eventImages' if needed.
     * Scans for events with posters and creates corresponding metadata documents.
     */
    public void migrateExistingImages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot eventDoc : querySnapshot) {
                String eventId = eventDoc.getId();
                String url = eventDoc.getString("posterImageUrl");

                if (url != null && !url.isEmpty()) {
                    HashMap<String, Object> imageData = new HashMap<>();
                    imageData.put("url", url);
                    imageData.put("eventId", eventId);

                    db.collection("eventImages").document(eventId)
                            .set(imageData)
                            .addOnSuccessListener(aVoid -> Log.d("Migration", "Created image doc for: " + eventId))
                            .addOnFailureListener(e -> Log.e("Migration", "Failed: " + eventId, e));
                }
            }
        });
    }
}
