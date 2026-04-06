package com.eventlottery.ui.adapters;

import android.graphics.Bitmap;
import android.location.Location;
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

    /**
     * Interface for handling event clicks
     */
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    /**
     * Constructor for EventAdapter
     * @param listener The listener to handle event clicks
     */
    public EventAdapter(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Submits a list of events to the adapter
     * @param newEvents The list of events to submit
     */
    public void submitList(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    /**
     * Creates a new EventViewHolder
     * @param parent The parent view group
     * @param viewType The view type
     * @return A new EventViewHolder
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventCardBinding binding = ItemEventCardBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new EventViewHolder(binding);
    }

    /**
     * Binds an event to a view holder
     * @param holder The view holder to bind to
     * @param position The position of the event in the list
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    /**
     * Returns the number of events in the list
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Sets the user's coordinates
     * @param lat
     * @param lon
     */
    public void setCoordinates(double lat, double lon) {
        userLat = lat;
        userLon = lon;
    }

    /**
     * ViewHolder for an event card
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventCardBinding binding;
        
        EventViewHolder(ItemEventCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds an event to the view holder
         * @param event The event to bind
         * @param listener The listener to handle event clicks
         */
        void bind(Event event, OnEventClickListener listener) {
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
            } else {
                binding.eventPosterImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
            
            // Set date and time
            String dateTime = String.format("%s • %s", event.getDate(), event.getTime());
            binding.dateTimeText.setText(dateTime);
            
            // Set location
            binding.locationText.setText(event.getLocation());
            
            // Set waitlist count
            String waitlistText;
            if (event.getWaitlistLimit() != null) {
                waitlistText = String.format("%d / %d on waiting list", 
                    event.getWaitlistCount(), event.getWaitlistLimit());
            } else {
                waitlistText = String.format("%d on waiting list", 
                    event.getWaitlistCount());
            }
            binding.waitlistCountText.setText(waitlistText);
            
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

        /**
         * Gets the status text for the event
         * @param status The status of the event
         * @return The status text
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
         * Gets the color for the status badge
         * @param status
         * @return The color
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
}
