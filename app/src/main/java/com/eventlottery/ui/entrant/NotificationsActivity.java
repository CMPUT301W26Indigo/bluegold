package com.eventlottery.ui.entrant;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.R;
import com.eventlottery.databinding.ActivityNotificationsBinding;
import com.eventlottery.model.Attendee;

/**
 * Activity hosting NotificationListFragment in ENTRANT mode.
 */
public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";
    private ActivityNotificationsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (savedInstanceState == null) {
            // Fetch the real Firebase ID asynchronously
            Attendee.getFirebaseId().addOnSuccessListener(id -> {
                // Load the reusable fragment in ENTRANT mode with the REAL attendee ID
                NotificationListFragment fragment = NotificationListFragment.newInstance(
                        NotificationListFragment.Mode.ENTRANT, id
                );
                
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get Firebase ID", e);
                Toast.makeText(this, "Error identifying user", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
