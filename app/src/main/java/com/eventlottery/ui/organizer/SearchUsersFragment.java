package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eventlottery.controller.AdminController;
import com.eventlottery.databinding.FragmentSearchUsersBinding;
import com.eventlottery.model.Attendee;
import com.eventlottery.model.User;
import com.eventlottery.ui.adapters.UserAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying search results and inviting users.
 * Part of the 'View' in MVC.
 */
public class SearchUsersFragment extends Fragment implements UserAdapter.OnAttendeeClickListener {

    private FragmentSearchUsersBinding binding;
    private UserAdapter userAdapter;
    private AdminController adminController;
    private List<Attendee> allAttendees = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminController = new AdminController();
        setupRecyclerView();
        loadUsers();
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(new UserAdapter.OnAttendeeClickListener() {
            @Override
            public void onAttendeeClick(Attendee attendee) {
                // Handle attendee click
            }

            @Override
            public void onInviteClick(Attendee attendee) {
                // Handle invite click
                Toast.makeText(getContext(), "Invite sent to: " + attendee.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUninviteClick(Attendee attendee) {
                // Handle uninvite click
                Toast.makeText(getContext(), "Invite cancelled for: " + attendee.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.usersRecyclerView.setAdapter(userAdapter);
    }

    private void loadUsers() {
        adminController.getAllUsers(new AdminController.OnDataLoadedListener<User>() {
            @Override
            public void onDataLoaded(List<User> users) {
                allAttendees.clear();
                for (User user : users) {
                    Attendee attendee = new Attendee();
                    attendee.setName(user.getName());
                    try {
                        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                            attendee.setEmail(user.getEmail());
                        }
                    } catch (IllegalArgumentException e) {
                        attendee.setEmail("Unknown");
                    }
                    attendee.setPhoneNumber(user.getPhone());
                    attendee.setAttendeeID(user.getId());
                    allAttendees.add(attendee);
                }
                if (isAdded()) {
                    userAdapter.submitList(new ArrayList<>(allAttendees));
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Filters the user list based on a query string.
     * @param query The search query.
     */
    public void filterUsers(String query) {
        if (userAdapter == null) return;
        
        List<Attendee> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Attendee attendee : allAttendees) {
            boolean matchesName = attendee.getName() != null && attendee.getName().toLowerCase().contains(lowerQuery);
            boolean matchesEmail = attendee.getEmail() != null && attendee.getEmail().toLowerCase().contains(lowerQuery);
            boolean matchesPhone = attendee.getPhoneNumber() != null && attendee.getPhoneNumber().toLowerCase().contains(lowerQuery);
            if (matchesName || matchesEmail || matchesPhone) {
                filtered.add(attendee);
            }
        }
        userAdapter.submitList(filtered);
    }

    @Override
    public void onAttendeeClick(Attendee attendee) {
    }

    @Override
    public void onInviteClick(Attendee attendee) {
    }

    @Override
    public void onUninviteClick(Attendee attendee) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
