package com.eventlottery.ui.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.FragmentOrganizerDashboardBinding;
import com.eventlottery.model.EventTemp;
import com.eventlottery.ui.adapters.EventAdapter;

import java.util.List;

/**
 * Fragment for the Organizer Dashboard.
 * Part of the 'View' in MVC.
 */
public class OrganizerDashboardFragment extends Fragment {

    private FragmentOrganizerDashboardBinding binding;
    private EventAdapter eventAdapter;
    private EventController eventController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventController = new EventController();
        setupRecyclerView();
        setupListeners();
        loadOrganizerEvents();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(this::navigateToManageEvent);
        binding.rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrganizerEvents.setAdapter(eventAdapter);
    }

    private void setupListeners() {
        binding.btnCreateEvent.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CreateEventActivity.class));
        });
    }

    private void loadOrganizerEvents() {
        // In a real app, we would filter by organizerId
        eventController.getAllEvents(new EventController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<EventTemp> events) {
                // For now, showing all events
                eventAdapter.submitList(events);
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    private void navigateToManageEvent(EventTemp event) {
        // Intent to manage event activity
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
