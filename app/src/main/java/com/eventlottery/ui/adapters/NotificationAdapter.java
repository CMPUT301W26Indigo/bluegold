package com.eventlottery.ui.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.eventlottery.R;
import com.eventlottery.databinding.ItemNotificationBinding;
import com.eventlottery.model.Notification;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying Notifications in a RecyclerView.
 * Handles binding notification data and user interaction with visual indicators.
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

        // The handling of the various types of notifications for the app
        // was helped created with the assistance of Gemini.
        void bind(Notification notification, OnNotificationClickListener listener) {
            binding.tvNotificationMessage.setText(notification.getMessage());
            binding.tvNotificationType.setText(notification.getType());

            // 1. Unread Dot and Background Tint
            if (notification.isRead()) {
                binding.unreadDot.setVisibility(View.GONE);
                binding.notificationCard.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.background_white)
                );
            } else {
                binding.unreadDot.setVisibility(View.VISIBLE);
                binding.notificationCard.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.background_blue_50)
                );
            }

            // 2. Bold Text for Actionable Items (Invitations)
            // Text remains bold if it's an INVITATION and status is PENDING
            if ("INVITATION".equals(notification.getType()) && "PENDING".equals(notification.getStatus())) {
                binding.tvNotificationMessage.setTypeface(null, Typeface.BOLD);
            } else {
                binding.tvNotificationMessage.setTypeface(null, Typeface.NORMAL);
            }

            // 3. Status Badge (ACCEPTED / DECLINED)
            String status = notification.getStatus();
            if (status != null && !"PENDING".equals(status)) {
                binding.tvNotificationStatus.setVisibility(View.VISIBLE);
                binding.tvNotificationStatus.setText(status);
                
                // Color coding for the badge
                if ("ACCEPTED".equals(status)) {
                    binding.tvNotificationStatus.setTextColor(
                            ContextCompat.getColor(itemView.getContext(), R.color.status_confirmed_green)
                    );
                } else if ("DECLINED".equals(status)) {
                    binding.tvNotificationStatus.setTextColor(
                            ContextCompat.getColor(itemView.getContext(), R.color.status_flagged_red)
                    );
                }
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
