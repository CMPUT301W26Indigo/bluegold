package com.eventlottery.ui.entrant;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.eventlottery.databinding.ActivityNotificationsBinding;
import com.eventlottery.model.Notification;
import com.eventlottery.ui.adapters.NotificationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying notifications to the attendee.
 */
public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private ActivityNotificationsBinding binding;
    private NotificationAdapter adapter;
    private FirebaseFirestore db;
    private String attendeeId = "mock_user_id"; // TODO: Replace with actual attendee ID, getter from the controller

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

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

    /**
     * Fetches notifications for the current user from Firestore.
     */
    private void loadNotifications() {
        db.collection("notifications")
                .whereEqualTo("attendeeId", attendeeId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<Notification> notifications = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Notification notification = doc.toObject(Notification.class);
                            notification.setId(doc.getId());
                            notifications.add(notification);
                        }
                        adapter.setNotifications(notifications);
                    }
                });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read if it's not already read
        if (!notification.isRead()) {
            notification.setRead(true);
            
            // Update in Firestore to persist the "read" state
            db.collection("notifications").document(notification.getId())
                    .update("isRead", true);
        }

        // TODO: Handle notification interaction (out of scope for this branch)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
