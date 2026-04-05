package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivitySendNotificationsBinding;
import com.eventlottery.model.Notification;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// This class was created by Gemini on March 26, 2026 when prompted to move the
//  Notifications UI from an activity based system to a fragment based system.
// Heavily edited by others on April
/**
 * Activity for organizers to compose and send notifications to different groups of entrants.
 * Supports targeted notifications to the waitlist, declined entrants, or the entire guest list.
 */
public class SendNotificationsActivity extends AppCompatActivity {

    private static final String TAG = "SendNotificationsActivity";
    private ActivitySendNotificationsBinding binding;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        
        // Retrieve event ID from intent, supporting both common keys used in the app
        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            eventId = getIntent().getStringExtra("eventId");
        }

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

            List<String> selectedGroups = new ArrayList<>();
            if (binding.chipWaitlist.isChecked()) selectedGroups.add("WAITING");
            if (binding.chipDeclined.isChecked()) selectedGroups.add("DECLINED");
            if (binding.chipAllGuestlist.isChecked()) selectedGroups.add("ALL_GUESTLIST");

            if (selectedGroups.isEmpty()) {
                Toast.makeText(this, "Please select at least one recipient group", Toast.LENGTH_SHORT).show();
                return;
            }

            sendNotifications(title, message, selectedGroups);
        });
    }

    /**
     * Logic to fetch recipient IDs from event sub-collections and create notification documents.
     */
    private void sendNotifications(String title, String message, List<String> groups) {
        binding.btnSend.setEnabled(false);
        Log.d(TAG, "Attempting to send notifications for event: " + eventId);
        
        db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
            if (!eventDoc.exists()) {
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                binding.btnSend.setEnabled(true);
                return;
            }

            final String eventName = eventDoc.getString("name");
            final String organizerId = eventDoc.getString("organizerId");

            List<Task<QuerySnapshot>> fetchTasks = new ArrayList<>();

            // Add tasks for each selected group to fetch recipient IDs
            if (groups.contains("WAITING")) {
                fetchTasks.add(db.collection("events").document(eventId).collection("waitlist").get());
            }

            // 2. Fetch from GuestList sub-collection for Declined entrants
            if (groups.contains("DECLINED")) {
                fetchTasks.add(db.collection("events").document(eventId).collection("guestList")
                        .whereEqualTo("status", "declined").get());
            }

            // 3. Fetch everyone on GuestList sub-collection
            if (groups.contains("ALL_GUESTLIST")) {
                fetchTasks.add(db.collection("events").document(eventId).collection("guestList").get());
            }

            // Wait for all fetch tasks to complete
            Tasks.whenAllSuccess(fetchTasks).addOnSuccessListener(results -> {
                Set<String> recipientIds = new HashSet<>();
                for (Object res : results) {
                    QuerySnapshot snapshot = (QuerySnapshot) res;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        // The document ID in these sub-collections is the attendeeId
                        recipientIds.add(doc.getId());
                    }
                }

                Log.d(TAG, "Resolved " + recipientIds.size() + " unique recipients");

                if (recipientIds.isEmpty()) {
                    Toast.makeText(this, "No recipients found in selected groups", Toast.LENGTH_SHORT).show();
                    binding.btnSend.setEnabled(true);
                    return;
                }

                // Safely handle organizer details for the notification sender fields
                if (organizerId == null || organizerId.isEmpty()) {
                    Log.w(TAG, "No organizerId found for event, using event name as fallback");
                    performBatchSend(title, message, recipientIds, eventName, null);
                } else {
                    db.collection("attendees").document(organizerId).get().addOnSuccessListener(orgDoc -> {
                        String senderName = orgDoc.exists() ? orgDoc.getString("name") : eventName;
                        performBatchSend(title, message, recipientIds, senderName, organizerId);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch organizer profile, using event name as fallback", e);
                        performBatchSend(title, message, recipientIds, eventName, organizerId);
                    });
                }

            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching recipients from sub-collections", e);
                Toast.makeText(this, "Error fetching recipients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.btnSend.setEnabled(true);
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading event from Firestore", e);
            Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
            binding.btnSend.setEnabled(true);
        });
    }

    /**
     * Commits a Firestore batch to create the notification documents.
     */
    private void performBatchSend(String title, String message, Set<String> recipientIds, String senderName, String organizerId) {
        WriteBatch batch = db.batch();
        int count = 0;
        
        for (String recipientId : recipientIds) {
            String notifId = UUID.randomUUID().toString();
            Notification notification = new Notification(
                    notifId,
                    title,
                    message,
                    recipientId,
                    eventId,
                    "INFO",
                    new Date()
            );
            notification.setSenderId(organizerId);
            notification.setSenderName(senderName);
            
            batch.set(db.collection("notifications").document(notifId), notification);
            count++;
            
            // Limit batch to slightly under Firestore's 500 operation limit
            if (count >= 495) {
                Log.w(TAG, "Reached batch limit of 495 notifications");
                break;
            }
        }

        final int finalCount = count;
        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Successfully committed " + finalCount + " notifications to Firestore");
            Toast.makeText(this, "Successfully sent " + finalCount + " notifications", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to commit batch to Firestore", e);
            Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.btnSend.setEnabled(true);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
