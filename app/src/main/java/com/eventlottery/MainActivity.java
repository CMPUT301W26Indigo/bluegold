package com.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivityMainBinding;
import com.eventlottery.ui.entrant.BrowseEventsActivity;
import com.eventlottery.ui.organizer.OrganizerDashboardActivity;
import com.eventlottery.ui.admin.AdminDashboardActivity;
import com.google.firebase.FirebaseApp;

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
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        startActivity(intent);
    }

    /**
     * Destroys the activity and sets the binding to null
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
