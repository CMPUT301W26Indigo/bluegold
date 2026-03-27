package com.eventlottery.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventlottery.R;
import com.eventlottery.model.Attendee;
import com.eventlottery.databinding.ItemUserCardBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * UserAdapter - RecyclerView adapter for displaying attendee cards
 *
 * Displays attendee information in a card layout with:
 * - Attendee name and invite button
 * - Email and phone
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<Attendee> attendees;
    private OnAttendeeClickListener listener;

    public interface OnAttendeeClickListener {
        void onAttendeeClick(Attendee attendee);
        void onInviteClick(Attendee attendee);
    }

    public UserAdapter(OnAttendeeClickListener listener) {
        this.attendees = new ArrayList<>();
        this.listener = listener;
    }

    public void submitList(List<Attendee> newAttendees) {
        this.attendees = newAttendees;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserCardBinding binding = ItemUserCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Attendee attendee = attendees.get(position);
        holder.bind(attendee, listener);
    }

    @Override
    public int getItemCount() {
        return attendees.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserCardBinding binding;

        UserViewHolder(ItemUserCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Attendee attendee, OnAttendeeClickListener listener) {
            // Set attendee name
            binding.attendeeNameText.setText(attendee.getName());

            // Set email
            binding.emailText.setText(attendee.getEmail());

            // Set phone
            binding.phoneText.setText(attendee.getPhoneNumber());

            // Reset button state (default: "Invite", grey, enabled)
            binding.statusBadge.setText("Invite");
            binding.statusBadge.setBackgroundTintList(
                    binding.getRoot().getContext().getColorStateList(R.color.status_closed_gray)
            );
            binding.statusBadge.setEnabled(true);

            // Set click listener for the card
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAttendeeClick(attendee);
                }
            });

            // Set invite button click listener
            binding.statusBadge.setOnClickListener(v -> {
                // Update UI immediately
                binding.statusBadge.setText("Invited");
                binding.statusBadge.setBackgroundTintList(
                        binding.getRoot().getContext().getColorStateList(R.color.status_open_green)
                );
                binding.statusBadge.setEnabled(false);

                if (listener != null) {
                    listener.onInviteClick(attendee);
                }
            });
        }
    }
}
