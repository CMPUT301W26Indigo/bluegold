package com.eventlottery.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventlottery.model.EventTemp;
import com.eventlottery.databinding.ItemEventCardBinding;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

/**
 * EventAdapter - View component in MVC.
 * Responsible for rendering Event models into the UI.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    
    private List<EventTemp> events;
    private OnEventClickListener listener;
    
    public interface OnEventClickListener {
        void onEventClick(EventTemp event);
    }
    
    public EventAdapter(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
    }
    
    public void submitList(List<EventTemp> newEvents) {
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
        EventTemp event = events.get(position);
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
        
        void bind(EventTemp event, OnEventClickListener listener) {
            binding.eventNameText.setText(event.getName());
            
            // Note: In a full MVC, some of this formatting logic might move to the Model or a Presenter
            binding.statusBadge.setText(event.getStatus());
            
            String dateTime = String.format("%s • %s", event.getDate(), event.getTime());
            binding.dateTimeText.setText(dateTime);
            binding.locationText.setText(event.getLocation());
            
            String waitlistText = event.getWaitlistLimit() != null 
                ? String.format("%d / %d on waiting list", event.getWaitlistCount(), event.getWaitlistLimit())
                : String.format("%d on waiting list", event.getWaitlistCount());
            binding.waitlistCountText.setText(waitlistText);
            
            binding.tagChips.removeAllViews();
            for (String tag : event.getTags()) {
                Chip chip = new Chip(binding.getRoot().getContext());
                chip.setText(tag);
                binding.tagChips.addView(chip);
            }
            
            if (event.isGeolocationEnabled() && event.getGeolocationRadius() != null) {
                binding.geolocationBadge.setVisibility(View.VISIBLE);
                binding.geolocationBadge.setText(String.format("Within %dkm", event.getGeolocationRadius()));
            } else {
                binding.geolocationBadge.setVisibility(View.GONE);
            }
            
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}
