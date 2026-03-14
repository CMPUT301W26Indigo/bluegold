package com.eventlottery.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eventlottery.databinding.ItemNotificationBinding;
import com.eventlottery.model.Notification;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying Notifications in a RecyclerView.
 * Handles binding notification data and user interaction.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    /**
     * Interface for handling clicks on notification items.
     */
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the list of notifications and refreshes the UI.
     *
     * @param newNotifications The new list of notifications.
     */
    public void setNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Notification notification, OnNotificationClickListener listener) {
            binding.tvNotificationMessage.setText(notification.getMessage());
            binding.tvNotificationType.setText(notification.getType());

            // Handle status visibility
            String status = notification.getStatus();
            if (status != null && !status.equals("PENDING")) {
                binding.tvNotificationStatus.setVisibility(View.VISIBLE);
                binding.tvNotificationStatus.setText(status);
            } else {
                binding.tvNotificationStatus.setVisibility(View.GONE);
            }

            // Set item click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
    }
}
