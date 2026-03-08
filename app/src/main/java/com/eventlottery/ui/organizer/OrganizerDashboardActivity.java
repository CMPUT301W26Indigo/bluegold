package com.eventlottery.ui.organizer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.R;
import com.eventlottery.databinding.ActivityOrganizerDashboardBinding;

/**
 * Activity hosting OrganizerDashboardFragment.
 * Part of the 'View' in MVC.
 */
public class OrganizerDashboardActivity extends AppCompatActivity {

    private ActivityOrganizerDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OrganizerDashboardFragment())
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
