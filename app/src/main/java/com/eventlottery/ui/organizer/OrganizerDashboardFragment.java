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
import com.eventlottery.model.Event;
import com.eventlottery.model.EventTemp;
import com.eventlottery.ui.adapters.EventAdapter;

import java.util.List;

/**
 * Fragment for the OrganizerDashboardActivity which displays the organizer's events
 */
public class OrganizerDashboardFragment extends Fragment {
    private FragmentOrganizerDashboardBinding binding;
    private EventAdapter eventAdapter;
    private EventController eventController;

    /**
     * Inflates Fragments xml layout
     * @param inflater Inflates any views in the fragment
     * @param container What this fragment should be attached to
     * @param savedInstanceState Saved data when the instance was last closed
     * @return View of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * sets up functionality after the view is inflated
     * @param view the view of the fragment
     * @param savedInstanceState Saved data when the instance was last closed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventController = new EventController();
        setupRecyclerView();
        setupListeners();
    }

    /**
     * What to do when the fragment resumes
     */
    @Override
    public void onResume() {
        super.onResume();
        // Reload events
        loadOrganizerEvents();
    }

    /**
     * Sets up the recycler view for the events
     */
    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(this::navigateToManageEvent);
        binding.rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrganizerEvents.setAdapter(eventAdapter);
    }

    /**
     * Sets up the listeners for the buttons
     */
    private void setupListeners() {
        binding.btnCreateEvent.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CreateEventActivity.class));
        });
    }

    /**
     * Loads the organizer's events
     */
    private void loadOrganizerEvents() {
        // todo filter by organizerId
        eventController.getAllEvents(new EventController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<EventTemp> events) {
                // For now, showing all events
                eventAdapter.submitList(events);
            }

            @Override
            public void onError(Exception e) {
                // todo Handle error
            }
        });
    }

    /**
     * Navigates to the manage event activity
     * @param event event to be navigated to
     */
    private void navigateToManageEvent(Event event) {
        // Intent to manage event activity
    }

    /**
     * Destroys the fragment and sets the binding to null
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
