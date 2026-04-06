package com.eventlottery.ui.entrant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.eventlottery.R;
import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.ActivityEventDetailsBinding;
import com.eventlottery.model.Attendee;
import com.eventlottery.model.Event;
import com.eventlottery.services.Base64EncodeDecode;
import com.eventlottery.services.LocationService;
import com.eventlottery.ui.qr.QRDisplayActivity;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

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
    private double userLat = 0;
    private double userLon = 0;

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
            userLat = getIntent().getDoubleExtra("USER_LAT",0);
            userLon = getIntent().getDoubleExtra("USER_LON",0);
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
                                Log.e(TAG, "Current attendee ID: " + currentAttendeeId);
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

                        eventController.checkIfAttendeeOnGuestlist(eventId, currentAttendeeId,
                                new EventController.OnWaitlistStatusListener() {
                                    @Override
                                    public void onStatusChecked(boolean isOnGuestlist) {
                                        updateWaitlistButtonUI(isOnWaitlist);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e(TAG, "Error checking guestlist", e);
                                    }
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
     *
     * @param isOnWaitlist
     */
    private void updateWaitlistButtonUI(boolean isOnWaitlist) {
        binding.joinWaitlistBtn.setEnabled(true);

        eventController.getAttendeeGuestlistStatus(eventId, currentAttendeeId, new EventController.OnGuestlistStatusListener() {
            @Override
            public void onStatusLoaded(String status) {
                Log.e(TAG, "Guestlist status for user " + currentAttendeeId + ": " + status);
                applyWaitlistUI(status, isOnWaitlist);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to fetch guestlist status", e);
                applyWaitlistUI(null, isOnWaitlist);
            }
        });
    }

    private void applyWaitlistUI(String status, boolean isOnWaitlist) {
        // 1. Event closed/completed
        if ("closed".equals(event.getStatus()) || "completed".equals(event.getStatus())) {

            binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.status_closed_gray));
            binding.joinWaitlistBtn.setText("Event Closed");
            binding.joinWaitlistBtn.setEnabled(false);
            return;
        }

        // 2. Confirmed users cannot rejoin waitlist
        if ("confirmed".equals(status)) {

            binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.secondary_orange));
            binding.joinWaitlistBtn.setText("Decline Acceptance");
            return;
        }

        // 3. Invited users can decline
        if ("invited".equals(status)) {

            binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.secondary_red));
            binding.joinWaitlistBtn.setTextColor(getColor(R.color.background_white));
            binding.joinWaitlistBtn.setText("Accept/Decline Invitation to Interact with this Event");
            binding.joinWaitlistBtn.setEnabled(false);
            return;
        }

        // 4. Users already on waitlist can leave
        if (isOnWaitlist) {

            binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.status_open_green));
            binding.joinWaitlistBtn.setText("Leave Waitlist");
            return;
        }

        // 5. Everyone else can join
        binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.primary_blue));
        binding.joinWaitlistBtn.setText("Join Waitlist");
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
        // Button is Greyed out if not within geolocation radius
        if (event.isGeolocationEnabled() && event.getGeolocationRadius() != null) {
            float[] distance = new float[1];
            Location.distanceBetween(userLat, userLon, event.getLatitude(), event.getLongitude(), distance);
            float distanceKm = distance[0] / 1000;

            if (distanceKm <= event.getGeolocationRadius()) {
                binding.joinWaitlistBtn.setOnClickListener(v -> handleWaitlistToggle());
            } else {
                binding.joinWaitlistBtn.setEnabled(false);
                binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.status_closed_gray));
            }
        } else {
            binding.joinWaitlistBtn.setOnClickListener(v -> handleWaitlistToggle());
        }

        // Can only see the QR button in a public event
        if (!event.isPrivate()) {
            binding.viewQrButton.setVisibility(View.VISIBLE);
            binding.viewQrButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, QRDisplayActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            });

            // Launch CommentsActivity for this event
            binding.viewCommentsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, CommentsActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                intent.putExtra("ORGANIZER_ID", event.getOrganizerId());
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

        boolean isJoining = binding.joinWaitlistBtn.getText().toString().equals("Join Waitlist");
        boolean isDeclining = binding.joinWaitlistBtn.getText().toString().equals("Decline Acceptance");


        EventController.OnEventOperationListener listener = new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                updateAttendeeWaitlist(isJoining);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (isDeclining) {
            eventController.leaveWaitlist(eventId, currentAttendeeId, listener);
            eventController.removeFromGuestlist(eventId, currentAttendeeId, listener);
        } else if (isJoining) {
            eventController.joinWaitlist(eventId, currentAttendeeId, listener);
        } else {
            eventController.leaveWaitlist(eventId, currentAttendeeId, listener);
        }
    }

    /**
     * Updates the attendee's waitlist status in the database.
     *
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
                // If attendee doesn't exist, we can't update, but we should still update UI
                // In a real app, you'd ensure the attendee profile exists first
                Log.e(TAG, "Error updating attendee waitlist", e);
                finishToggle(isJoining);
            }
        });
    }

    /**
     * Finishes the toggle and updates the UI.
     *
     * @param isJoining
     */
    private void finishToggle(boolean isJoining) {
        checkWaitlistStatus();
        loadEventStats();
        Toast.makeText(EventDetailsActivity.this,
                isJoining ? "Joined waitlist" : "Left waitlist", Toast.LENGTH_SHORT).show();
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

    /**
     * Gets the text for the status chip.
     * @param status
     * @return
     */
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

    /**
     * Gets the color for the status chip.
     * @param status
     * @return
     */
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
