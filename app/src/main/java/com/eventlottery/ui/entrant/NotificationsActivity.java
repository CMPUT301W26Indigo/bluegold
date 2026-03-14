package com.eventlottery.ui.entrant;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.eventlottery.databinding.ActivityNotificationsBinding;
import com.eventlottery.model.Notification;
import com.eventlottery.ui.adapters.NotificationAdapter;
import java.util.ArrayList;

/**
 * Activity for displaying notifications to the attendee.
 */
public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private ActivityNotificationsBinding binding;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        loadNotifications();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Initialize Adapter
        adapter = new NotificationAdapter(this);
        
        // Setup RecyclerView
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        // TODO: Replace with actual data loading from Firestore
        // This is a placeholder to verify the connection
        adapter.setNotifications(new ArrayList<>());
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // TODO: Handle notification click (e.g., show confirmation dialog for invitations)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
