package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.databinding.ActivityDrawLotteryBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Handles the lottery draw for an event.
 *
 * Randomly selects winners from the waitlist and moves them to the guest list
 * with "invited" status. Winners receive notifications.
 *
 * User stories implemented:
 * - Lottery draw system
 *
 * Issues:
 * - Need to send actual push notifications to winners
 */
public class DrawLotteryActivity extends AppCompatActivity {

    private ActivityDrawLotteryBinding binding;
    private FirebaseFirestore db;
    private String eventId;

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
        // Get event capacity and current confirmed count
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    int capacity = eventDoc.getLong("capacity").intValue();

                    // Get current confirmed count from guestList
                    db.collection("events").document(eventId)
                            .collection("guestList")
                            .whereEqualTo("status", "confirmed")
                            .get()
                            .addOnSuccessListener(confirmedQuery -> {
                                int confirmedCount = confirmedQuery.size();
                                int spotsAvailable = capacity - confirmedCount;

                                if (spotsAvailable <= 0) {
                                    Toast.makeText(this, "Event is full!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    return;
                                }

                                int winnersToDraw = Math.min(numWinners, spotsAvailable);

                                // Get waitlist
                                db.collection("events").document(eventId)
                                        .collection("waitlist")
                                        .get()
                                        .addOnSuccessListener(waitlistQuery -> {
                                            if (waitlistQuery.isEmpty()) {
                                                Toast.makeText(this, "Waitlist is empty", Toast.LENGTH_SHORT).show();
                                                finish();
                                                return;
                                            }

                                            // Convert to list for random selection
                                            List<QueryDocumentSnapshot> waitlist = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : waitlistQuery) {
                                                waitlist.add(doc);
                                            }

                                            // Randomly select winners
                                            Random random = new Random();
                                            int winnersToDrawFinal = Math.min(winnersToDraw, waitlist.size());
                                            int winnersSelected = 0;

                                            for (int i = 0; i < winnersToDrawFinal; i++) {
                                                int index = random.nextInt(waitlist.size());
                                                String winnerId = waitlist.get(index).getId();

                                                // Add to guestList with "invited" status
                                                HashMap<String, Object> guestEntry = new HashMap<>();
                                                guestEntry.put("status", "invited");
                                                guestEntry.put("invitedAt", System.currentTimeMillis());

                                                db.collection("events").document(eventId)
                                                        .collection("guestList").document(winnerId)
                                                        .set(guestEntry);

                                                // Remove from waitlist
                                                db.collection("events").document(eventId)
                                                        .collection("waitlist").document(winnerId)
                                                        .delete();

                                                // Remove from list to avoid duplicate selection
                                                waitlist.remove(index);
                                                winnersSelected++;
                                            }

                                            // Update event status
                                            db.collection("events").document(eventId)
                                                    .update("status", "lottery_drawn");

                                            Toast.makeText(this, winnersSelected + " winners selected!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error loading waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error loading confirmed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}