package com.eventlottery.ui.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.R;
import com.eventlottery.databinding.ActivityNotificationLogsBinding;
import com.eventlottery.ui.entrant.NotificationListFragment;

/**
 * Activity hosting NotificationListFragment in ADMIN mode.
 * Supports US 03.08.01.
 */
public class NotificationLogsActivity extends AppCompatActivity {

    private ActivityNotificationLogsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (savedInstanceState == null) {
            // Load the reusable fragment in ADMIN mode
            NotificationListFragment fragment = NotificationListFragment.newInstance(
                    NotificationListFragment.Mode.ADMIN, null
            );
            
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
