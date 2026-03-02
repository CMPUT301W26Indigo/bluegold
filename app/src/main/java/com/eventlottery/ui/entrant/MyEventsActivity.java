package com.eventlottery.ui.entrant;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.R;

/**
 * MyEventsActivity
 * 
 * Screen E3 from storyboard - User's events with tabs
 * 
 * Features:
 * - Tab layout: Waiting, Selected, Confirmed, History
 * - Display user's registered events
 * - Show lottery status
 * - Confirm/Decline invitations
 * 
 * TODO: Implement full functionality
 */
public class MyEventsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Create layout file activity_my_events.xml
        setContentView(R.layout.activity_my_events);
        
        // TODO: Setup tabs and ViewPager
        // TODO: Load user's events from database
    }
}
