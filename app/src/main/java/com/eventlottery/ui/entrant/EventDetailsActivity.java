package com.eventlottery.ui.entrant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.eventlottery.R;
import com.eventlottery.databinding.ActivityEventDetailsBinding;
import com.eventlottery.model.Event;
import com.eventlottery.services.Base64EncodeDecode;
import com.eventlottery.ui.qr.QRDisplayActivity;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;

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
 *
 * TODO: Implement full functionality
 * - Load event data from Intent extras
 * - Display event poster with Glide
 * - Handle join/leave waitlist actions
 * - Check geolocation requirements
 */
public class EventDetailsActivity extends AppCompatActivity {

    private @NonNull ActivityEventDetailsBinding binding;
    private Event event;
    private String eventId;
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                        setupUI();
                        loadEventStats();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void setupToolbar() {
        // TODO: Setup toolbar with back button
    }

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
            binding.geolocationRadiusText.setText("Entry Limited Within " + event.getGeolocationRadius() + "km radius");
        }

        binding.eventDateText.setText(event.getDate());
        binding.eventTimeText.setText(event.getTime());
        binding.descriptionText.setText(event.getDescription());
        binding.locationNameText.setText(event.getLocation());

        // Buttons only appear if event is not private
        if(!event.isPrivate()) {
            binding.joinWaitlistBtn.setOnClickListener(v -> {
                // Waitlist btn
                if(binding.joinWaitlistBtn.getText() == "Join Waiting List") {
                    // TODO Add attendee
                    binding.joinWaitlistBtn.setBackgroundColor(getColor(com.eventlottery.R.color.status_open_green));
                    binding.joinWaitlistBtn.setText("Leave Waiting List");
                } else {
                    // TODO Delete Attendee
                    binding.joinWaitlistBtn.setBackgroundColor(getColor(R.color.primary_blue));
                    binding.joinWaitlistBtn.setText("Join Waiting List");
                }
            });

            // View QR button
            binding.viewQrButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, QRDisplayActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            });
        }
    }

    private void loadEventStats() {
        // Get waitlist count and fill out capacity card in UI
        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(query -> {
                    binding.tvWaitlistCount.setText(query.size() + " in the waitlist");
                    binding.capacityText.setText(query.size() + " / " + event.getCapacity());
                    binding.spotsAvailableText.setText(event.getCapacity() - query.size() + " spots available");
                });



    }
}
