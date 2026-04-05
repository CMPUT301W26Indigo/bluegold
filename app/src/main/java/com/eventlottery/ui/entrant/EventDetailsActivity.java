package com.eventlottery.ui.entrant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.eventlottery.R;
import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.ActivityEventDetailsBinding;
import com.eventlottery.model.Attendee;
import com.eventlottery.model.Event;
import com.eventlottery.model.GuestList;
import com.eventlottery.services.Base64EncodeDecode;
import com.eventlottery.services.LocationService;
import com.eventlottery.ui.qr.QRDisplayActivity;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.List;

/**
 * EventDetailsActivity
 * 
 * Screen E2 from storyboard - Detailed event information
 * 
 * Features:
 * - Display full event information
 * - Show event poster image
 * - Display geolocation requirements
 * - Join/Leave waitlist button
 * - Show capacity and spots available
 */
public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";
    private @NonNull ActivityEventDetailsBinding binding;
    private Event event;
    private String eventId;
    private FirebaseFirestore db;
    private EventController eventController;
    private String currentAttendeeId;
    private LocationService locationService = new LocationService(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventController = new EventController();

        // Get event from Intent
        if (getIntent().getData() != null) {
            eventId = getIntent().getData().getLastPathSegment();  // get ID from QR scan
        } else {
            eventId = getIntent().getStringExtra("EVENT_ID");  // get ID normally
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            event.setId(documentSnapshot.getId());
                            setupUI();
                            loadEventStats();

                            // Get the current attendee ID asynchronously
                            Attendee.getFirebaseId().addOnSuccessListener(id -> {
                                currentAttendeeId = id;
                                checkWaitlistStatus();
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to get Firebase ID", e);
                                Toast.makeText(this, "Error identifying user", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });


        //just here for testing purposes
        locationService.requestLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationReady(double lat, double lon) {
                Log.d("Location", "Latitude: " + lat + ", Longitude: " + lon);
            }

            @Override
            public void onPermissionDenied() {
                Log.d("Location", "Permission denied");
            }
        });
        //just here for testing purposes
    }

    /**
     * Checks if the current attendee is on the waitlist for the event
     */
    private void checkWaitlistStatus() {
        if (eventId == null || currentAttendeeId == null) return;

        eventController.checkIfAttendeeOnWaitlist(eventId, currentAttendeeId,
                new EventController.OnWaitlistStatusListener() {
                    @Override
                    public void onStatusChecked(boolean isOnWaitlist) {
                        // Fetch the guestlist status directly to check for winners
                        db.collection("events").document(eventId)
                                .collection("guestlist").document(currentAttendeeId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String guestStatus = null;
                                    if (documentSnapshot.exists()) {
                                        guestStatus = documentSnapshot.getString("status");
                                    }
                                    updateWaitlistButtonUI(isOnWaitlist, guestStatus);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error checking guestlist status", e);
                                    updateWaitlistButtonUI(isOnWaitlist, null);
                                });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error checking waitlist", e);
                    }
                });
    }

    /**
     * Updates the UI to reflect the current waitlist and event status.
     * @param isOnWaitlist True if user is on the waitlist
     * @param guestStatus The status of the user on the guestlist (invited, confirmed, etc.)
     */
    private void updateWaitlistButtonUI(boolean isOnWaitlist, String guestStatus) {
        binding.joinWaitlistBtn.setEnabled(true);

        // 1. Event closed/completed
        if ("closed".equals(event.getStatus()) || "completed".equals(event.getStatus())) {
            binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.status_closed_gray));
            binding.joinWaitlistBtn.setText("Event Closed");
            binding.joinWaitlistBtn.setEnabled(false);
            return;
        }

        // 2. Confirmed users cannot rejoin waitlist
        if ("confirmed".equals(guestStatus) || "accepted".equals(guestStatus)) {
            binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.secondary_orange));
            binding.joinWaitlistBtn.setText("Decline Acceptance");
            binding.joinWaitlistBtn.setEnabled(true);
            return;
        }

        // 3. Invited users can decline
        if ("invited".equals(guestStatus)) {
            binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.secondary_orange));
            binding.joinWaitlistBtn.setText("Decline Invitation");
            binding.joinWaitlistBtn.setEnabled(true);
            return;
        }

        // 4. Users already on waitlist can leave
        if (isOnWaitlist) {
            binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.status_open_green));
            binding.joinWaitlistBtn.setText("Leave Waitlist");
            binding.joinWaitlistBtn.setEnabled(true);
            return;
        }

        // 5. If user was previously invited/confirmed but is no longer (e.g. they declined or lottery reset)
        // AND the event is in lottery_drawn state, they should NOT be able to join.
        // Wait, the rule is: anyone who HAS been confirmed or invited after a lottery is drawn should NOT be able to join.
        // If guestStatus is not null, they were part of the guestlist logic.
        // If status is "declined" or "cancelled", they shouldn't rejoin if lottery was drawn.
        if ("lottery_drawn".equals(event.getStatus()) && guestStatus != null) {
            binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.status_closed_gray));
            binding.joinWaitlistBtn.setText("Lottery In Progress");
            binding.joinWaitlistBtn.setEnabled(false);
            return;
        }

        // 6. Everyone else can join (including those who haven't been invited yet, regardless of lottery state)
        binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.primary_blue));
        binding.joinWaitlistBtn.setText("Join Waitlist");
        binding.joinWaitlistBtn.setEnabled(true);
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
        
        if (event.getPosterImageUrl() != null) {
            Bitmap bitmap = Base64EncodeDecode.decodeBase64(event.getPosterImageUrl());
            Glide.with(this)
                    .load(bitmap)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.eventPosterImage);
        }
        binding.eventNameText.setText(event.getName());
        binding.statusChip.setText(getStatusText(event.getStatus()));
        binding.statusChip.setChipBackgroundColorResource(getStatusColor(event.getStatus()));

        DecimalFormat df = new DecimalFormat("0.00");
        if (event.getPrice() == 0 || event.getPrice() == 0.0) {
            binding.priceChip.setText("Price: Free");
        } else {
            binding.priceChip.setText("Price: $" + df.format(event.getPrice()));
        }
        binding.tagChipGroup.removeAllViews();
        for (String tag : event.getTags()) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            binding.tagChipGroup.addView(chip);
        }

        if (event.getGeolocationRadius() != null) {
            binding.geolocationCard.setVisibility(View.VISIBLE);
            binding.geolocationRadiusText.setText("Entry Limited Within " + event.getGeolocationRadius() + "km radius");
        }

        binding.eventDateText.setText(event.getDate());
        binding.eventTimeText.setText(event.getTime());
        binding.descriptionText.setText(event.getDescription());
        binding.locationNameText.setText(event.getLocation());

        // Buttons only appear if event is not private
        binding.joinWaitlistBtn.setOnClickListener(v -> handleWaitlistToggle());

        // Can only see the QR button in a public event
        if(!event.isPrivate()) {
            binding.viewQrButton.setVisibility(View.VISIBLE);
            binding.viewQrButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, QRDisplayActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            });
        }
    }

    /**
     * Handles the toggle between joining and leaving the waitlist.
     */
    private void handleWaitlistToggle() {
        if (currentAttendeeId == null) {
            Toast.makeText(this, "Identifying user...", Toast.LENGTH_SHORT).show();
            return;
        }

        String buttonText = binding.joinWaitlistBtn.getText().toString();
        boolean isJoining = buttonText.equals("Join Waitlist");
        boolean isDeclining = buttonText.equals("Decline Acceptance") || buttonText.equals("Decline Invitation");


        EventController.OnEventOperationListener listener = new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                // Now update the Attendee's profile as well
                updateAttendeeWaitlist(isJoining);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (isDeclining) {
            // Remove from BOTH waitlist and guestlist to prevent re-joining bug
            eventController.leaveWaitlist(eventId, currentAttendeeId, new EventController.OnEventOperationListener() {
                @Override
                public void onSuccess() {
                    eventController.removeFromGuestlist(eventId, currentAttendeeId, listener);
                }

                @Override
                public void onError(Exception e) {
                    eventController.removeFromGuestlist(eventId, currentAttendeeId, listener);
                }
            });
        }
        else if (isJoining) {
            eventController.joinWaitlist(eventId, currentAttendeeId, listener);
        }
        else {
            eventController.leaveWaitlist(eventId, currentAttendeeId, listener);
        }
    }

    /**
     * Updates the attendee's waitlist status in the database.
     * @param isJoining
     */
    private void updateAttendeeWaitlist(boolean isJoining) {
        Attendee attendee = new Attendee();
        attendee.setID(currentAttendeeId);
        
        attendee.fetchFromFirebase(new Attendee.OnAttendeeLoadedListener() {
            @Override
            public void onSuccess(Attendee loadedAttendee) {
                if (isJoining) {
                    loadedAttendee.joinWaitList(eventId);
                } else {
                    loadedAttendee.leaveWaitList(eventId);
                }
                finishToggle(isJoining);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error updating attendee waitlist", e);
                finishToggle(isJoining);
            }
        });
    }

    /**
     * Finishes the toggle and updates the UI.
     * @param isJoining
     */
    private void finishToggle(boolean isJoining) {
        checkWaitlistStatus();
        loadEventStats();
        Toast.makeText(EventDetailsActivity.this, 
            isJoining ? "Joined waitlist" : "Removed from event", Toast.LENGTH_SHORT).show();
    }

    /**
     * Loads the current waitlist count and capacity.
     */
    private void loadEventStats() {
        // Get waitlist count and fill out capacity card in UI
        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(query -> {
                    binding.tvWaitlistCount.setText(query.size() + " in the waitlist");
                    binding.capacityText.setText(query.size() + " / " + event.getCapacity());
                    binding.spotsAvailableText.setText((event.getCapacity() - query.size()) + " spots available");
                });
    }

    private String getStatusText(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case "open":
                return "Open";
            case "closed":
                return "Closed";
            case "lottery_drawn":
                return "Lottery Drawn";
            case "completed":
                return "Completed";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return R.color.status_closed_gray;
        switch (status) {
            case "open":
                return R.color.status_open_green;
            case "closed":
                return R.color.status_closed_gray;
            case "lottery_drawn":
                return R.color.status_waiting_yellow;
            case "completed":
                return R.color.status_closed_gray;
            default:
                return R.color.status_closed_gray;
        }
    }
}
