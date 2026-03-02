package com.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivityMainBinding;
import com.eventlottery.ui.entrant.BrowseEventsActivity;
import com.eventlottery.ui.organizer.OrganizerDashboardActivity;
import com.eventlottery.ui.admin.AdminDashboardActivity;

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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupRoleSelection();
    }
    
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
    
    private void navigateToEntrantFlow() {
        Intent intent = new Intent(this, BrowseEventsActivity.class);
        startActivity(intent);
    }
    
    private void navigateToOrganizerFlow() {
        Intent intent = new Intent(this, OrganizerDashboardActivity.class);
        startActivity(intent);
    }
    
    private void navigateToAdminFlow() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
