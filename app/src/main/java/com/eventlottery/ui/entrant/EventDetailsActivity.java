package com.eventlottery.ui.entrant;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.R;
import com.eventlottery.model.Event;

/**
 * EventDetailsActivity
 * 
 * Screen E2 from storyboard - Detailed event information
 * 
 * Features:
 * - Display full event information
 * - Show event poster image
 * - Display geolocation requirements
 * - Join/Leave waitlist button
 * - Show capacity and spots available
 * 
 * TODO: Implement full functionality
 * - Load event data from Intent extras
 * - Display event poster with Glide
 * - Handle join/leave waitlist actions
 * - Check geolocation requirements
 */
public class EventDetailsActivity extends AppCompatActivity {
    
    private Event event;
    private String eventId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        
        // Get event from Intent
        eventId = getIntent().getStringExtra("EVENT_ID");
        
        // Use type-safe getParcelableExtra for API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            event = getIntent().getParcelableExtra("EVENT", Event.class);
        } else {
            // Suppress warning for older APIs as it's unavoidable there
            //noinspection deprecation
            event = getIntent().getParcelableExtra("EVENT");
        }
        
        // TODO: Setup views and load event data
        setupToolbar();
        loadEventDetails();
    }
    
    private void setupToolbar() {
        // TODO: Setup toolbar with back button
    }
    
    private void loadEventDetails() {
        // TODO: Load event details from database if not passed in Intent
        // TODO: Display event information
        // TODO: Setup join waitlist button
    }
}
