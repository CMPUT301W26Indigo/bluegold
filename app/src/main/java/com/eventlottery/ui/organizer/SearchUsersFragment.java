package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eventlottery.controller.AdminController;
import com.eventlottery.controller.OrganizerController;
import com.eventlottery.databinding.FragmentSearchUsersBinding;
import com.eventlottery.model.Attendee;
import com.eventlottery.ui.adapters.UserAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying search results and inviting users or adding co-organizers.
 * Part of the 'View' in MVC.
 */
public class SearchUsersFragment extends Fragment implements UserAdapter.OnAttendeeClickListener {

    private static final String TAG = "SearchUsersFragment";
    private FragmentSearchUsersBinding binding;
    private UserAdapter userAdapter;
    private AdminController adminController;
    private OrganizerController organizerController;
    private List<Attendee> allAttendees = new ArrayList<>();
    private String eventId;
    private boolean isCoOrganizerMode = false;

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
        organizerController = new OrganizerController();

        if (getActivity() != null && getActivity().getIntent() != null) {
            eventId = getActivity().getIntent().getStringExtra("EVENT_ID");
            isCoOrganizerMode = getActivity().getIntent().getBooleanExtra("CO_ORGANIZER_MODE", false);
        }

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
                if (isCoOrganizerMode && eventId != null) {
                    addCoOrganizer(attendee);
                } else {
                    // TODO: Actually send the notification for private event invitation
                    Toast.makeText(getContext(), "Invite sent to: " + attendee.getName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onUninviteClick(Attendee attendee) {
                // Handle uninvite click
                Toast.makeText(getContext(), "Invite cancelled for: " + attendee.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        
        if (isCoOrganizerMode) {
            userAdapter.setInviteButtonText("Add Co-Org");
        }
        
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.usersRecyclerView.setAdapter(userAdapter);
    }

    private void addCoOrganizer(Attendee attendee) {
        organizerController.addCoOrganizer(eventId, attendee.getID(), new OrganizerController.OnOperationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), attendee.getName() + " added as co-organizer", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error adding co-organizer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsers() {
        // Updated to only fetch from the 'attendees' collection
        adminController.getAllAttendees(new AdminController.OnDataLoadedListener<Attendee>() {
            @Override
            public void onDataLoaded(List<Attendee> attendees) {
                allAttendees.clear();
                allAttendees.addAll(attendees);
                
                Log.d(TAG, "Loaded " + allAttendees.size() + " attendees from Firebase");
                
                if (isAdded()) {
                    userAdapter.submitList(new ArrayList<>(allAttendees));
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading attendees", e);
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
