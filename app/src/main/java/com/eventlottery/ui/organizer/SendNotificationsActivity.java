package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivitySendNotificationsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

// This class was created by Gemini on March 26, 2026 when prompted to move the
//  Notifications UI from an activity based system to a fragment based system.
/**
 * Activity for organizers to compose and send notifications to different groups of entrants.
 */
public class SendNotificationsActivity extends AppCompatActivity {

    private ActivitySendNotificationsBinding binding;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null) {
            Toast.makeText(this, "Error: Missing Event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        binding.btnSend.setOnClickListener(v -> {
            String title = binding.etNotificationTitle.getText().toString().trim();
            String message = binding.etNotificationMessage.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please enter both a title and message", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> recipientGroups = new ArrayList<>();
            if (binding.chipWaitlist.isChecked()) recipientGroups.add("WAITING");
            if (binding.chipSelected.isChecked()) recipientGroups.add("SELECTED");
            if (binding.chipCancelled.isChecked()) recipientGroups.add("CANCELLED");

            if (recipientGroups.isEmpty()) {
                Toast.makeText(this, "Please select at least one recipient group", Toast.LENGTH_SHORT).show();
                return;
            }

            sendNotifications(title, message, recipientGroups);
        });
    }

    private void sendNotifications(String title, String message, List<String> groups) {
        // This method query's the database to get the event's guest list/waitlist
        // for users in the selected groups and create notification documents for each.
        // For the UI demonstration, we'll just show a success message.
        
        Toast.makeText(this, "Sending notifications to " + String.join(", ", groups), Toast.LENGTH_LONG).show();
        
        // Log the notification for admin review (US 03.08.01)
        // logic...

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
