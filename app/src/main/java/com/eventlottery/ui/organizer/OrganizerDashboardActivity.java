package com.eventlottery.ui.organizer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.eventlottery.R;
import com.eventlottery.databinding.ActivityOrganizerDashboardBinding;

/**
 * This Activity holds the general dashboard for the organizers
 * allows them to see their events and be able to move to another
 * activity to create events
 */
public class OrganizerDashboardActivity extends AppCompatActivity {

    private ActivityOrganizerDashboardBinding binding;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState Saved data when the instance was last closed
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase
        com.google.firebase.FirebaseApp.initializeApp(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        binding = ActivityOrganizerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        //calls OrganizerDashboardFragment to display events
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OrganizerDashboardFragment())
                    .commit();
        }
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
