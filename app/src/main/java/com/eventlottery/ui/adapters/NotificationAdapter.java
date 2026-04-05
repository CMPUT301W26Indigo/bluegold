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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying Notifications in a RecyclerView.
 * Handles binding notification data and user interaction with visual indicators.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationActionListener actionListener;
    private boolean isAdminMode = false;

    /**
     * Interface for handling actions on notification items.
     */
    public interface OnNotificationActionListener {
        void onNotificationClick(Notification notification);
        void onAcceptInvitation(Notification notification);
        void onDeclineInvitation(Notification notification);
    }

    public NotificationAdapter(OnNotificationActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setAdminMode(boolean adminMode) {
        this.isAdminMode = adminMode;
        notifyDataSetChanged();
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
        holder.bind(notifications.get(position), actionListener, isAdminMode);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Notification notification, OnNotificationActionListener actionListener, boolean isAdminMode) {
            if (notification.getTitle() != null && !notification.getTitle().isEmpty()) {
                binding.tvNotificationTitle.setVisibility(View.VISIBLE);
                binding.tvNotificationTitle.setText(notification.getTitle());
            } else {
                binding.tvNotificationTitle.setVisibility(View.GONE);
            }

            binding.tvNotificationMessage.setText(notification.getMessage());
            binding.tvNotificationType.setText(notification.getType());

            // 1. Unread Dot and Background Tint
            if (notification.isRead()) {
                binding.unreadDot.setVisibility(View.GONE);
                binding.notificationCard.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), android.R.color.white)
                );
            } else {
                binding.unreadDot.setVisibility(View.VISIBLE);
                binding.notificationCard.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.background_blue_50)
                );
            }

            if (isAdminMode) {
                binding.layoutAdminInfo.setVisibility(View.VISIBLE);
                binding.layoutActions.setVisibility(View.GONE); // No actions in admin log
                binding.unreadDot.setVisibility(View.GONE);
                
                binding.tvSenderInfo.setText("Sent by: " + (notification.getSenderName() != null ? notification.getSenderName() : "Unknown"));
                binding.tvRecipientInfo.setText("Sent to: " + (notification.getRecipientName() != null ? notification.getRecipientName() : "Unknown"));
                
                if (notification.getTimestamp() != null) {
                    binding.tvTimestamp.setText("Time: " + dateFormat.format(notification.getTimestamp()));
                }
            } else {
                binding.layoutAdminInfo.setVisibility(View.GONE);
                
                // 3. Handle Actionable Invitations (Entrant Mode only)
                boolean isInvitation = "INVITATION".equals(notification.getType()) || "CO_ORGANIZER_INVITE".equals(notification.getType());
                boolean isPending = "PENDING".equals(notification.getStatus());

                if (isInvitation && isPending) {
                    binding.layoutActions.setVisibility(View.VISIBLE);
                    binding.tvNotificationMessage.setTypeface(null, Typeface.BOLD);
                    binding.tvNotificationStatus.setVisibility(View.GONE);
                } else {
                    binding.layoutActions.setVisibility(View.GONE);
                    binding.tvNotificationMessage.setTypeface(null, Typeface.NORMAL);
                    
                    if (!isPending) {
                        binding.tvNotificationStatus.setVisibility(View.VISIBLE);
                        binding.tvNotificationStatus.setText(notification.getStatus());
                        
                        if ("ACCEPTED".equals(notification.getStatus())) {
                            binding.tvNotificationStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_confirmed_green));
                        } else {
                            binding.tvNotificationStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_flagged_red));
                        }
                    } else {
                        binding.tvNotificationStatus.setVisibility(View.GONE);
                    }
                }
            }

            // Set Action Listeners
            binding.btnAccept.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onAcceptInvitation(notification);
            });

            binding.btnDecline.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onDeclineInvitation(notification);
            });

            itemView.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onNotificationClick(notification);
            });
        }
    }
}
