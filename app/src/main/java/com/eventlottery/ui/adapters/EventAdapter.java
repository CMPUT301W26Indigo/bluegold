package com.eventlottery.ui.adapters;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eventlottery.R;
import com.eventlottery.model.Event;
import com.eventlottery.databinding.ItemEventCardBinding;
import com.eventlottery.services.Base64EncodeDecode;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * EventAdapter - RecyclerView adapter for displaying event cards
 * 
 * Displays event information in a card layout with:
 * - Event name and status badge
 * - Date, time, location
 * - Waitlist count
 * - Tags
 * - Geolocation badge (if enabled)
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    
    private List<Event> events;
    private OnEventClickListener listener;
    private static double userLat = 0;
    private static double userLon = 0;
    
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }
    
    public EventAdapter(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
    }
    
    public void submitList(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventCardBinding binding = ItemEventCardBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new EventViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setCoordinates(double lat, double lon) {
        userLat = lat;
        userLon = lon;
    }
    
    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventCardBinding binding;
        
        EventViewHolder(ItemEventCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(Event event, OnEventClickListener listener) {
            Log.e("EventAdapter", "Waitlist count: " + event.getWaitlistCount());
            // Set event name
            binding.eventNameText.setText(event.getName());
            
            // Set status badge
            binding.statusBadge.setText(getStatusText(event.getStatus()));
            binding.statusBadge.setChipBackgroundColorResource(
                getStatusColor(event.getStatus())
            );

            //set image
            if (event.getPosterImageUrl() != null) {
                Bitmap bitmap = Base64EncodeDecode.decodeBase64(event.getPosterImageUrl());
                Glide.with(binding.getRoot().getContext())
                    .load(bitmap)
                    .into(binding.eventPosterImage);
            }
            
            // Set date and time
            String dateTime = String.format("%s • %s", event.getDate(), event.getTime());
            binding.dateTimeText.setText(dateTime);
            
            // Set location
            binding.locationText.setText(event.getLocation());
            
            // Set waitlist count
            FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(event.getId())
                    .collection("waitlist")
                    .get()
                    .addOnSuccessListener(query -> {

                        int count = query.size();

                        String waitlistText;

                        if (event.getWaitlistLimit() != null && event.getWaitlistLimit() > 0) {
                            waitlistText = String.format("%d / %d on Waitlist",
                                    count,
                                    event.getWaitlistLimit());
                        } else {
                            waitlistText = String.format("%d on Waitlist", count);
                        }

                        binding.waitlistCountText.setText(waitlistText);
                    });

            if (event.getStatus().equals("lottery_drawn")) {
                FirebaseFirestore.getInstance()
                        .collection("events")
                        .document(event.getId())
                        .collection("guestList")
                        .get()
                        .addOnSuccessListener(query -> {

                            int count = query.size();

                            String guestlistText;
                            guestlistText = String.format("%d / %d Confirmed Attendees",
                                    count,
                                    event.getCapacity());


                            binding.guestlistCountText.setText(guestlistText);
                        });
            } else {
                binding.guestlistLayout.setVisibility(View.GONE);
            }

            // Set tags
            binding.tagChips.removeAllViews();
            for (String tag : event.getTags()) {
                Chip chip = new Chip(binding.getRoot().getContext());
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.background_blue_50);
                chip.setTextColor(binding.getRoot().getContext()
                    .getColor(R.color.text_blue_900));
                binding.tagChips.addView(chip);
            }
            
            // Show geolocation badge if enabled
            if (event.isGeolocationEnabled() && event.getGeolocationRadius() != null) {
                binding.geolocationBadge.setVisibility(View.VISIBLE);
                binding.geolocationBadge.setText(
                    String.format("Within %dkm", event.getGeolocationRadius())
                );
                binding.joinableBadge.setVisibility(View.VISIBLE);
                if (userLat == 0 || userLon == 0) {
                    binding.joinableBadge.setText("Unknown");
                    binding.joinableBadge.setBackgroundColor(binding.getRoot().getContext().getColor(R.color.status_closed_gray));
                } else {
                    float[] distance = new float[1];
                    Location.distanceBetween(userLat, userLon, event.getLatitude(), event.getLongitude(), distance);
                    float distanceKm = distance[0] / 1000;
                    if (distanceKm <= event.getGeolocationRadius()) {
                        binding.joinableBadge.setText(
                                String.format("Joinable", event.getGeolocationRadius())
                        );
                    } else {
                        binding.joinableBadge.setText(
                                String.format("Not Joinable", event.getGeolocationRadius())
                        );
                        binding.joinableBadge.setTextColor(binding.getRoot().getContext().getColor(R.color.text_red_900));
                        binding.joinableBadge.setChipStrokeColorResource(R.color.text_red_900);
                        binding.joinableBadge.setChipBackgroundColorResource(R.color.status_flagged_red);
                    }
                }
            } else {
                binding.geolocationBadge.setVisibility(View.GONE);
            }
            
            // Set click listener
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
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
}
