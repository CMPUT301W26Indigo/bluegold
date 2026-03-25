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
import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.FragmentBrowseEventsBinding;
import com.eventlottery.model.Event;
import com.eventlottery.ui.adapters.EventAdapter;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private FirebaseFirestore db;


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
        setupRecyclerView();
        setupSearch();
        setupFilters();
        loadEvents();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(this::navigateToEventDetails);
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.eventsRecyclerView.setAdapter(eventAdapter);
    }

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

    private void setupFilters() {
        binding.tagChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> selectedTags = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    selectedTags.add(chip.getText().toString());
                }
            }
            filterByTags(selectedTags);
        });
    }

    private void loadEvents() {
        eventController.getAllEvents(new EventController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                allEvents = events;
                eventAdapter.submitList(new ArrayList<>(allEvents));
            }

            @Override
            public void onError(Exception e) {
                // Handle error (e.g., show Toast)
            }
        });
    }

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

    private void navigateToEventDetails(Event event) {
        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        // GO BACK AND FIX THIS LUCIA!!!
        intent.putExtra("EVENT_ID", event.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadJoinableEvents() {
        long now = System.currentTimeMillis();

        db.collection("events")
                .whereEqualTo("status", "open")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> joinable = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (now >= event.getRegistrationOpens() && now <= event.getRegistrationCloses()) {
                            joinable.add(event);
                        }
                    }
                    eventAdapter.submitList(joinable);
                });
    }


}
