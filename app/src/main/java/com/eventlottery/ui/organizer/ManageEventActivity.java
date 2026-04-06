package com.eventlottery.ui.organizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.eventlottery.R;
import com.eventlottery.databinding.ActivityManageEvent1Binding;
import com.eventlottery.databinding.ActivityManageEventBinding;
import com.eventlottery.model.Event;
import com.eventlottery.model.Notification;
import com.eventlottery.services.Base64EncodeDecode;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


/**
 * Main management dashboard for organizers to control their events.
 *
 * Provides access to organizer functions including viewing invited entrants,
 * viewing confirmed entrants, and exporting attendee lists to CSV
 *
 * User stories implemented:
 * - 02.06.03
 * - 02.06.05
 * - 02.06.01
 *
 * Outstanding issues:
 * - FileProvider needs to be configured in AndroidManifest (?)
 * - Handle cases where user documents doesn't exist in Firestore
 *
 * @see InvitedEntrantsActivity
 * @see ConfirmedEntrantsActivity
 */
public class ManageEventActivity extends AppCompatActivity {

    private @NonNull ActivityManageEvent1Binding binding;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageEvent1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventId = getIntent().getStringExtra("EVENT_ID");

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        event = documentSnapshot.toObject(Event.class);
                        setupUI();
                        loadEventStats();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    /**
     * Sets up the UI elements with event data.
     */
    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }
        Log.d("ManageEvent", "Loading URL: " + event.getPosterImageUrl());
        if (event.getPosterImageUrl() != null) {
            Bitmap bitmap = Base64EncodeDecode.decodeBase64(event.getPosterImageUrl());
            Glide.with(this)
                    .load(bitmap)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.eventPosterImage);
        }
        binding.eventNameText.setText(event.getName());
        binding.statusChip.setText(event.getStatus());
        for (String tag : event.getTags()) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            binding.tagChipGroup.addView(chip);
        }

        if (event.getGeolocationRadius() != null) {
            binding.geolocationCard.setVisibility(View.VISIBLE);
            binding.geolocationRadiusText.setText("Within " + event.getGeolocationRadius() + "km radius");
        }

        binding.eventDateText.setText(event.getDate());
        binding.eventTimeText.setText(event.getTime());
        binding.descriptionText.setText(event.getDescription());
        binding.locationNameText.setText(event.getLocation());

        // Draw lottery button
        binding.btnDrawLottery.setOnClickListener(v -> {
            Intent intent = new Intent(this, DrawLotteryActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        // Notify unselected entrants button
        binding.btnNotifyUnselected.setOnClickListener(v -> {
            notifyUnselectedEntrants();
        });

        // View Invited Entrants button
        binding.btnViewInvited.setOnClickListener(v -> {
            Intent intent = new Intent(this, InvitedEntrantsActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        // Search and invite users button
        if (event.isPrivate()) {
            binding.btnInvitePrivateEntrants.setVisibility(View.VISIBLE);

            binding.btnInvitePrivateEntrants.setOnClickListener(v -> {
                Intent intent = new Intent(this, SearchUsersActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            });
        }

        // Export CSV button
        binding.btnExportCSV.setOnClickListener(v -> {
            exportCSV();
        });

        // Send Notifications button
        binding.btnSendNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, SendNotificationsActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        binding.btnSeeWaitlistMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, WaitlistMapActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            intent.putExtra("EVENT_LAT", event.getLatitude());
            intent.putExtra("EVENT_LON", event.getLongitude());
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /**
     * Loads event statistics from Firestore.
     */
    private void loadEventStats() {
        // Get waitlist count
        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(query -> {
                    binding.tvWaitlistCount.setText(query.size() + " in the waitlist");
                });

        // Get invited count
        db.collection("events").document(eventId)
                .collection("guestList")
                .whereEqualTo("status", "invited")
                .get()
                .addOnSuccessListener(query -> {
                    binding.tvInvitedCount.setText(query.size() + " not confirmed");
                });

        // Get confirmed count
        db.collection("events").document(eventId)
                .collection("guestList")
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(query -> {
                    binding.tvConfirmedCount.setText(query.size() + " / " + event.getCapacity());
                });

    }

    /**
     * Notifies all users currently on the waitlist that they were not selected.
     */
    private void notifyUnselectedEntrants() {
        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(waitlistQuery -> {
                    if (waitlistQuery.isEmpty()) {
                        Toast.makeText(this, "Waitlist is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WriteBatch batch = db.batch();
                    String title = getString(R.string.notification_not_selected_title);
                    String message = getString(R.string.notification_not_selected_msg, event.getName());

                    for (QueryDocumentSnapshot doc : waitlistQuery) {
                        String userId = doc.getId();
                        String notifId = UUID.randomUUID().toString();

                        Notification notification = new Notification(
                                notifId,
                                title,
                                message,
                                userId,
                                eventId,
                                "INFO",
                                new Date()
                        );
                        notification.setSenderName(event.getName());
                        notification.setSenderId(event.getOrganizerId());

                        batch.set(db.collection("notifications").document(notifId), notification);
                    }

                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Notifications sent to unselected entrants", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Error sending notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Exports attendee lists to CSV.
     */
    private void exportCSV() {
        ArrayList<String> names = new ArrayList<>();

        db.collection("events").document(eventId)
                .collection("guestList")
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder csv = new StringBuilder();
                    csv.append("Name,Email,Phone\n");

                    // If no entrants
                    int totalCount = queryDocumentSnapshots.size();
                    if (totalCount == 0) {
                        Toast.makeText(this, "No confirmed entrants", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("name");
                                    String email = userDoc.getString("email");
                                    String phone = userDoc.getString("phone");

                                    // Handle commas in names
                                    if (name != null && name.contains(",")) {
                                        name = "\"" + name + "\"";
                                    }

                                    csv.append(name).append(",")
                                            .append(email).append(",")
                                            .append(phone).append("\n");

                                    names.add(name);

                                    // Share when all are processed
                                    if (names.size() == totalCount) {
                                        shareCSV(csv.toString());
                                    }
                                });
                    }
                });
    }

    /**
     * Shares a CSV file.
     * @param csvContent
     */
    private void shareCSV(String csvContent) {
        try {
            File file = new File(getExternalFilesDir(null), "entrants.csv");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(csvContent.getBytes());
            fos.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share CSV"));
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getCancelledList() {
        db.collection("events").document(eventId)
                .collection("guestList")
                .whereEqualTo("status", "cancelled")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<String> cancelledUsers = new ArrayList<>();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No cancelled entrants", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        cancelledUsers.add(userId);
                    }

                    Toast.makeText(this,
                            cancelledUsers.size() + " cancelled entrants found",
                            Toast.LENGTH_SHORT).show();

                    // If needed later, you now have the cancelled user IDs
                    // Example: send notifications, display list, etc.
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error fetching cancelled entrants",
                                Toast.LENGTH_SHORT).show());
    }

}
