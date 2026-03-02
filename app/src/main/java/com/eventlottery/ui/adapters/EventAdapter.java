package com.eventlottery.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eventlottery.R;
import com.eventlottery.data.models.Event;
import com.eventlottery.databinding.ItemEventCardBinding;
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
    
    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventCardBinding binding;
        
        EventViewHolder(ItemEventCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(Event event, OnEventClickListener listener) {
            // Set event name
            binding.eventNameText.setText(event.getName());
            
            // Set status badge
            binding.statusBadge.setText(getStatusText(event.getStatus()));
            binding.statusBadge.setChipBackgroundColorResource(
                getStatusColor(event.getStatus())
            );
            
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
