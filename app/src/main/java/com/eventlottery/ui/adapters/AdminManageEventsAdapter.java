package com.eventlottery.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventlottery.R;
import com.eventlottery.model.Event;

import java.util.List;

/**
 * Adapter for displaying all events in the admin panel.
 *
 * Each row shows the event name and date, with a "View Comments" button
 * that allows the admin to navigate to that event's comments for moderation
 *
 * Part of the 'View' in MVC.
 *
 */
public class AdminManageEventsAdapter extends RecyclerView.Adapter<AdminManageEventsAdapter.EventViewHolder> {

    /**
     * Listener interface for when the admin taps "View Comments" on an event
     */
    public interface OnViewCommentsClickListener {
        void onViewCommentsClick(Event event);
    }

    private final List<Event> events;
    private final OnViewCommentsClickListener listener;

    /**
     * Constructs an AdminManageEventsAdapter.
     *
     * @param events   The list of events to display
     * @param listener Callback when the admin taps "View Comments" on an event
     */
    public AdminManageEventsAdapter(List<Event> events, OnViewCommentsClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.eventName.setText(event.getName());
        holder.eventDate.setText(event.getDate());

        holder.viewCommentsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewCommentsClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder for a single event row in the admin events list
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView eventDate;
        Button viewCommentsButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.tv_admin_event_name);
            eventDate = itemView.findViewById(R.id.tv_admin_event_date);
            viewCommentsButton = itemView.findViewById(R.id.btn_view_comments);
        }
    }
}
