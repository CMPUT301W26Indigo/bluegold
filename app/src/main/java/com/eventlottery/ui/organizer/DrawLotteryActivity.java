package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.databinding.ActivityDrawLotteryBinding;
import com.eventlottery.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Handles the lottery draw for an event.
 *
 * Randomly selects winners from the waitlist and moves them to the guest list
 * with "invited" status. Winners receive notifications.
 */
public class DrawLotteryActivity extends AppCompatActivity {

    private static final String TAG = "DrawLotteryActivity";
    private ActivityDrawLotteryBinding binding;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDrawLotteryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("EVENT_ID");

        setupUI();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        binding.btnDrawLotteryNow.setOnClickListener(v -> {
            String winnerCountStr = binding.etWinnerCount.getText().toString();
            if (winnerCountStr.isEmpty()) {
                Toast.makeText(this, "Enter number of winners", Toast.LENGTH_SHORT).show();
                return;
            }
            int numWinners = Integer.parseInt(winnerCountStr);
            drawLottery(numWinners);
        });
    }

    private void drawLottery(int numWinners) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    eventName = eventDoc.getString("name");
                    Long capacityLong = eventDoc.getLong("capacity");
                    int capacity = (capacityLong != null) ? capacityLong.intValue() : 0;

                    db.collection("events").document(eventId)
                            .collection("guestList")
                            .whereEqualTo("status", "confirmed")
                            .get()
                            .addOnSuccessListener(confirmedQuery -> {
                                int confirmedCount = confirmedQuery.size();
                                int spotsAvailable = capacity - confirmedCount;

                                if (spotsAvailable <= 0) {
                                    Toast.makeText(this, "Event is full!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                int winnersToDraw = Math.min(numWinners, spotsAvailable);

                                db.collection("events").document(eventId)
                                        .collection("waitlist")
                                        .get()
                                        .addOnSuccessListener(waitlistQuery -> {
                                            if (waitlistQuery.isEmpty()) {
                                                Toast.makeText(this, "Waitlist is empty", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            List<QueryDocumentSnapshot> waitlist = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : waitlistQuery) {
                                                waitlist.add(doc);
                                            }

                                            Random random = new Random();
                                            int winnersToDrawFinal = Math.min(winnersToDraw, waitlist.size());
                                            
                                            WriteBatch batch = db.batch();

                                            for (int i = 0; i < winnersToDrawFinal; i++) {
                                                int index = random.nextInt(waitlist.size());
                                                String winnerId = waitlist.get(index).getId();

                                                // 1. Update EVENT'S guestList
                                                Map<String, Object> eventGuestEntry = new HashMap<>();
                                                eventGuestEntry.put("status", "invited");
                                                eventGuestEntry.put("invitedAt", System.currentTimeMillis());
                                                batch.set(db.collection("events").document(eventId)
                                                        .collection("guestList").document(winnerId), eventGuestEntry);

                                                // 2. Remove from EVENT'S waitlist
                                                batch.delete(db.collection("events").document(eventId)
                                                        .collection("waitlist").document(winnerId));

                                                // 3. Remove from ATTENDEE'S waitlist
                                                batch.delete(db.collection("attendees").document(winnerId)
                                                        .collection("waitListed").document(eventId));

                                                // 4. Add to ATTENDEE'S Selected subcollection
                                                Map<String, Object> selectedEntry = new HashMap<>();
                                                selectedEntry.put("status", "invited");
                                                selectedEntry.put("selectedAt", System.currentTimeMillis());
                                                batch.set(db.collection("attendees").document(winnerId)
                                                        .collection("Selected").document(eventId), selectedEntry);

                                                // 5. Create NOTIFICATION
                                                String notifId = UUID.randomUUID().toString();
                                                Notification notification = new Notification(
                                                        notifId,
                                                        "Congratulations! You've been selected for " + eventName + ". Please confirm your attendance.",
                                                        winnerId,
                                                        eventId,
                                                        "INVITATION",
                                                        new Date()
                                                );
                                                batch.set(db.collection("notifications").document(notifId), notification);

                                                waitlist.remove(index);
                                            }

                                            batch.update(db.collection("events").document(eventId), "status", "lottery_drawn");

                                            batch.commit().addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, winnersToDrawFinal + " winners selected and notified!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }).addOnFailureListener(e -> {
                                                Log.e(TAG, "Error committing lottery batch", e);
                                                Toast.makeText(this, "Error updating winners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Error loading waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error loading confirmed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
