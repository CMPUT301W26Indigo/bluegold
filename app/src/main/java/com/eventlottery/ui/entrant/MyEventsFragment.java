package com.eventlottery.ui.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.FragmentMyEventsBinding;
import com.eventlottery.model.Attendee;
import com.eventlottery.model.Event;
import com.eventlottery.ui.adapters.EventAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragment for the User's Events.
 * Part of the 'View' in MVC.
 */
public class MyEventsFragment extends Fragment {

    private static final String TAG = "MyEventsFragment";
    private FragmentMyEventsBinding binding;
    private EventAdapter eventAdapter;
    private EventController eventController;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        eventController = new EventController();
        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        setupTabs();
        loadUserEvents();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(event -> {
            Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            startActivity(intent);
        });
        binding.rvMyEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMyEvents.setAdapter(eventAdapter);
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadUserEvents();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadUserEvents() {
        int selectedTab = binding.tabLayout.getSelectedTabPosition();
        
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            switch (selectedTab) {
                case 0: // Waiting
                    fetchFromSubcollection(id, "waitListed");
                    break;
                case 1: // Selected
                    fetchSelectedEvents(id);
                    break;
                case 2: // Confirmed
                    fetchConfirmedEvents(id);
                    break;
                case 3: // History
                    fetchFromSubcollection(id, "EventHistory");
                    break;
                default:
                    updateUI(new ArrayList<>());
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get Firebase ID", e);
            updateUI(new ArrayList<>());
        });
    }

    private void fetchFromSubcollection(String attendeeId, String collectionName) {
        db.collection("attendees").document(attendeeId)
                .collection(collectionName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> eventIds = new HashSet<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        eventIds.add(document.getId());
                    }
                    loadEventsFromIds(eventIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching " + collectionName, e);
                    updateUI(new ArrayList<>());
                });
    }

    private void fetchSelectedEvents(String attendeeId) {
        // Show only events where the user is invited but hasn't confirmed yet
        db.collection("attendees").document(attendeeId)
                .collection("Selected")
                .whereEqualTo("status", "invited")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> eventIds = new HashSet<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        eventIds.add(document.getId());
                    }
                    loadEventsFromIds(eventIds);
                })
                .addOnFailureListener(e -> updateUI(new ArrayList<>()));
    }

    private void fetchConfirmedEvents(String attendeeId) {
        db.collection("attendees").document(attendeeId)
                .collection("Selected")
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> eventIds = new HashSet<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        eventIds.add(document.getId());
                    }
                    loadEventsFromIds(eventIds);
                })
                .addOnFailureListener(e -> updateUI(new ArrayList<>()));
    }

    private void loadEventsFromIds(Set<String> eventIds) {
        if (eventIds.isEmpty()) {
            updateUI(new ArrayList<>());
            return;
        }

        eventController.getEventsByIds(new ArrayList<>(eventIds), new EventController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                updateUI(events);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading event details", e);
                updateUI(new ArrayList<>());
            }
        });
    }

    private void updateUI(List<Event> events) {
        if (binding == null) return;
        eventAdapter.submitList(events);
        
        if (events.isEmpty()) {
            binding.tvNoEvents.setVisibility(View.VISIBLE);
            binding.rvMyEvents.setVisibility(View.GONE);
        } else {
            binding.tvNoEvents.setVisibility(View.GONE);
            binding.rvMyEvents.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
