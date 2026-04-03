package com.eventlottery.ui.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.eventlottery.R;
import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.FragmentBrowseEventsBinding;
import com.eventlottery.model.Event;
import com.eventlottery.ui.adapters.EventAdapter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for browsing events.
 * Part of the 'View' in MVC.
 */
public class BrowseEventsFragment extends Fragment {

    private FragmentBrowseEventsBinding binding;
    private EventAdapter eventAdapter;
    private EventController eventController;
    private List<Event> allEvents = new ArrayList<>();
    private CarouselFragment carouselFragment;
    private String searchQuery = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBrowseEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        eventController = new EventController();
        setupCarousel();
        setupRecyclerView();
        setupFilters();
        loadEvents();
    }

    /**
     * Sets up event carousel WOW factor
     */
    private void setupCarousel() {
        carouselFragment = new CarouselFragment();
        carouselFragment.setOnEventClickListener(this::navigateToEventDetails);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.carousel_container, carouselFragment)
                .commit();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(this::navigateToEventDetails);
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.eventsRecyclerView.setAdapter(eventAdapter);
        binding.eventsRecyclerView.setNestedScrollingEnabled(false);
    }


    /**
     * Sets up the filter functionality.
     * Users can filter by search query, date, time, and capacity.
     */
    private void setupFilters() {

        // Clear filters button
        binding.clearFiltersButton.setOnClickListener(v -> {
            binding.eventDateEditText.setText("");
            binding.eventTimeEditText.setText("");
            binding.eventCapacityEditText.setText("");
            binding.searchEditText.setText("");
            applyFilters();
        });

        // Search bar
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Date picker
        binding.eventDateEditText.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Event Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                binding.eventDateEditText.setText(datePicker.getHeaderText());
                applyFilters();
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        // Time picker
        binding.eventTimeEditText.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Event Time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                binding.eventTimeEditText.setText(
                        String.format("%02d:%02d",
                                timePicker.getHour(),
                                timePicker.getMinute())
                );
                applyFilters();
            });

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        });

        // Minimum capacity picker
        binding.eventCapacityEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });
    }

    /**
     * Applies the current filters (date, time, capacity) to the event list.
     */
    private void applyFilters() {
        List<Event> filtered = new ArrayList<>();

        String searchQuery = binding.searchEditText.getText().toString().toLowerCase();
        String selectedDate = binding.eventDateEditText.getText().toString();
        String selectedTime = binding.eventTimeEditText.getText().toString();
        String capacityText = binding.eventCapacityEditText.getText().toString();

        // Convert capacity to integer and ignore the filter if nonintegers are inputted
        Integer maxCapacity = null;
        if (!capacityText.isEmpty()) {
            try {
                maxCapacity = Integer.parseInt(capacityText);
            } catch (NumberFormatException ignored) {}
        }

        for (Event event : allEvents) {

            boolean matchesQuery = true;
            boolean matchesDate = true;
            boolean matchesTime = true;
            boolean matchesCapacity = true;

            // Query Filter
            if (!searchQuery.isEmpty()) {
                matchesQuery = event.getName().toLowerCase().contains(searchQuery) ||
                                event.getDescription().toLowerCase().contains(searchQuery) ||
                                matchesTags(event, searchQuery);
            }

            // Date filter
            if (!selectedDate.isEmpty()) {
                matchesDate = event.getDate() != null &&
                        event.getDate().equals(selectedDate);
            }

            // Time filter
            if (!selectedTime.isEmpty()) {
                matchesTime = event.getTime() != null &&
                        event.getTime().equals(selectedTime);
            }

            // Capacity filter
            if (maxCapacity != null) {
                matchesCapacity = event.getCapacity() <= maxCapacity;
            }

            // Combine all filters to select matching events
            if (matchesQuery && matchesDate && matchesTime && matchesCapacity) {
                filtered.add(event);
            }
        }

        eventAdapter.submitList(filtered);
    }

    /**
     * Lets us know if an event matches a tag for searching purposes
     * @param event
     * @param query
     * @return boolean
     */
    private boolean matchesTags(Event event, String query) {
        for (String tag : event.getTags()) {
            if (tag.toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads all PUBLIC events from the DB
     */
    private void loadEvents() {
        // TODO: Currently, entrants only see public events.
        // If you need to see all the events from the entrant POV for debugging
        // Replace getAllPublicEvents with getAllEvents
        eventController.getAllPublicEvents(new EventController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                allEvents = events;
                eventAdapter.submitList(new ArrayList<>(allEvents));
                if (carouselFragment != null) {
                    carouselFragment.setEvents(new ArrayList<>(allEvents));
                }
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    /**
     * Navigates to the event details page
     * @param event
     */
    private void navigateToEventDetails(Event event) {
        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", event.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
