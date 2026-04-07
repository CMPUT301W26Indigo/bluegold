package com.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivityMainBinding;
import com.eventlottery.model.AbstractUser;
import com.eventlottery.model.Attendee;
import com.eventlottery.ui.entrant.BrowseEventsActivity;
import com.eventlottery.ui.organizer.OrganizerDashboardActivity;
import com.eventlottery.ui.admin.AdminDashboardActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

/**
 * MainActivity - Role Selection Screen
 * 
 * This is the entry point of the application where users select their role:
 * - Entrant: Browse and join events
 * - Organizer: Create and manage events
 * - Administrator: Moderate platform content
 */
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState Saved data when the instance was last closed
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupRoleSelection();

        // Update all attendees with new field
        updateAllAttendeesWithNewField();
    }

    /**
     * Sets up the role selection functionality
     */
    private void setupRoleSelection() {
        // Entrant role selection
        binding.entrantCard.setOnClickListener(v -> navigateToEntrantFlow());
        binding.btnEnterAsEntrant.setOnClickListener(v -> navigateToEntrantFlow());
        
        // Organizer role selection
        binding.organizerCard.setOnClickListener(v -> navigateToOrganizerFlow());
        binding.btnEnterAsOrganizer.setOnClickListener(v -> navigateToOrganizerFlow());
        
        // Admin role selection
        binding.adminCard.setOnClickListener(v -> navigateToAdminFlow());
        binding.btnEnterAsAdmin.setOnClickListener(v -> navigateToAdminFlow());

        // How it works
        binding.btnHowLotteryWorks.setOnClickListener(v -> {
            Intent intent = new Intent(this, LotteryInfoActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Navigates to the entrant flow
     */
    private void navigateToEntrantFlow() {
        Intent intent = new Intent(this, BrowseEventsActivity.class);
        startActivity(intent);
    }

    /**
     * Navigates to the organizer flow
     */
    private void navigateToOrganizerFlow() {
        Intent intent = new Intent(this, OrganizerDashboardActivity.class);
        //intent.putExtra("ORGANIZERID",userId);
        startActivity(intent);
    }

    /**
     * Navigates to the admin flow
     */
    private void navigateToAdminFlow() {
        AbstractUser.getFirebaseId().addOnSuccessListener(id -> {
            Attendee attendee = new Attendee();
            attendee.setID(id);
            attendee.fetchFromFirebase(new Attendee.OnAttendeeLoadedListener() {
                @Override
                public void onSuccess(Attendee loadedAttendee) {
                    FirebaseFirestore.getInstance().collection("attendees").document(id).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                                Boolean admin = documentSnapshot.getBoolean("admin");
                                if (isAdmin != null && isAdmin || admin != null && admin) {
                                    Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "This is restricted to admins", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(MainActivity.this, "Error verifying admin status", Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Error identifying user", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Destroys the activity and sets the binding to null
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    public void updateAllAttendeesWithNewField() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("attendees").get().addOnSuccessListener(querySnapshot -> {
            WriteBatch batch = db.batch(); // Use a Batch for better performance

            for (DocumentSnapshot doc : querySnapshot) {
                DocumentReference ref = doc.getReference();
                // Add the new field with a default value (e.g., "Unknown" or false)
                if (!doc.contains("isAdmin")) {
                    batch.update(ref, "isAdmin", true);
                }
            }

            // Commit all changes at once
            batch.commit().addOnSuccessListener(aVoid -> Log.d("DB", "All attendees updated!"));
        });
    }
}
