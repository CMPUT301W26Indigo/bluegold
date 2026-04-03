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
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        setupSearch();
        setupFilters();
        loadEvents();
    }

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
     * Sets up the search functionality.
     */
    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Sets up the filter functionality.
     */
    private void setupFilters() {
//        binding.tagChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
//            List<String> selectedTags = new ArrayList<>();
//            for (int id : checkedIds) {
//                Chip chip = group.findViewById(id);
//                if (chip != null) {
//                    selectedTags.add(chip.getText().toString());
//                }
//            }
//            filterByTags(selectedTags);
//        });
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
     * Filters the event list based on a query string.
     * @param query The search query.
     */
    private void filterEvents(String query) {
        List<Event> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Event event : allEvents) {
            if (event.getName().toLowerCase().contains(lowerQuery) ||
                event.getDescription().toLowerCase().contains(lowerQuery)) {
                filtered.add(event);
            }
        }
        eventAdapter.submitList(filtered);
    }

    /**
     * Filters the event list based on selected tags.
     */
    private void filterByTags(List<String> tags) {
        if (tags.isEmpty()) {
            eventAdapter.submitList(new ArrayList<>(allEvents));
            return;
        }
        List<Event> filtered = new ArrayList<>();
        for (Event event : allEvents) {
            for (String tag : event.getTags()) {
                if (tags.contains(tag)) {
                    filtered.add(event);
                    break;
                }
            }
        }
        eventAdapter.submitList(filtered);
    }

    /*
     * Navigates to the EventDetailsActivity when an event is clicked.
     * @param event The selected event.
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

    // TODO: What is this for? It had no usages: should we delete?
//    private void loadJoinableEvents() {
//        long now = System.currentTimeMillis();
//
//        db.collection("events")
//                .whereEqualTo("status", "open")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<Event> joinable = new ArrayList<>();
//                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                        Event event = doc.toObject(Event.class);
//                        if (now >= event.getRegistrationOpens() && now <= event.getRegistrationCloses()) {
//                            joinable.add(event);
//                        }
//                    }
//                    eventAdapter.submitList(joinable);
//                });
//    }


}
