package com.eventlottery.ui.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.eventlottery.R;
import com.eventlottery.databinding.ActivityBrowseEventsBinding;
import com.eventlottery.data.models.Event;
import com.eventlottery.ui.adapters.EventAdapter;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BrowseEventsActivity
 * 
 * Screen E1 from storyboard - Main landing page for entrants
 * 
 * Features:
 * - Search events by name/description
 * - Filter by tags (Sports, Music, Arts, etc.)
 * - View event cards with key information
 * - See geolocation requirements
 * - Navigate to event details
 * 
 * Requirements Covered:
 * - Browse and search events using plaintext search
 * - Filter events by tags
 * - View waitlist size and capacity
 * - View geolocation requirements
 */
public class BrowseEventsActivity extends AppCompatActivity {
    
    private ActivityBrowseEventsBinding binding;
    private EventAdapter eventAdapter;
    private List<Event> allEvents;
    private List<Event> filteredEvents;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        allEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();
        
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupBottomNavigation();
        
        // Load events from database (Firebase/Supabase)
        loadEvents();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
    }
    
    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(event -> navigateToEventDetails(event));
        
        binding.eventsRecyclerView.setLayoutManager(
            new LinearLayoutManager(this)
        );
        binding.eventsRecyclerView.setAdapter(eventAdapter);
    }
    
    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }
    
    private void setupFilters() {
        binding.filterButton.setOnClickListener(v -> showFilterDialog());
        
        // Tag chip selection
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
    
    private void setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_events);
        
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_events) {
                // Already on this screen
                return true;
            } else if (itemId == R.id.navigation_my_events) {
                startActivity(new Intent(this, MyEventsActivity.class));
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            
            return false;
        });
    }
    
    private void loadEvents() {
        // TODO: Load events from Firebase/Supabase
        // This is a placeholder - implement actual database query
        
        // Example mock data:
        // Event event = new Event();
        // event.setId("1");
        // event.setName("Summer Basketball Tournament");
        // event.setDescription("Join us for an exciting tournament...");
        // event.setDate("2026-06-15");
        // event.setTime("14:00");
        // event.setLocation("Community Center Arena");
        // event.setWaitlistCount(28);
        // event.setCapacity(50);
        // List<String> tags = new ArrayList<>();
        // tags.add("Sports");
        // tags.add("Tournament");
        // event.setTags(tags);
        // event.setGeolocationEnabled(true);
        // event.setGeolocationRadius(10);
        // event.setStatus("open");
        // allEvents.add(event);
        
        filterEvents("");
    }
    
    private void filterEvents(String query) {
        filteredEvents.clear();
        
        if (query.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            final String lowerQuery = query.toLowerCase();
            for (Event event : allEvents) {
                if (event.getName().toLowerCase().contains(lowerQuery) ||
                    event.getDescription().toLowerCase().contains(lowerQuery)) {
                    filteredEvents.add(event);
                }
            }
        }
        
        eventAdapter.submitList(new ArrayList<>(filteredEvents));
    }
    
    private void filterByTags(List<String> tags) {
        if (tags.isEmpty() || tags.contains("All Events")) {
            filterEvents("");
            return;
        }
        
        filteredEvents.clear();
        for (Event event : allEvents) {
            for (String eventTag : event.getTags()) {
                if (tags.contains(eventTag)) {
                    filteredEvents.add(event);
                    break;
                }
            }
        }
        
        eventAdapter.submitList(new ArrayList<>(filteredEvents));
    }
    
    private void showFilterDialog() {
        // TODO: Implement filter dialog with options:
        // - Event status (Open/Closed)
        // - Date range
        // - Distance (for geolocation)
        // - Price range
    }
    
    private void navigateToEventDetails(Event event) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", event.getId());
        intent.putExtra("EVENT", event);
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
