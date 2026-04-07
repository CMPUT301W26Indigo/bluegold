package com.eventlottery.ui.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.ActivityManageEventsBinding;
import com.eventlottery.model.Event;
import com.eventlottery.ui.adapters.EventAdapter;

import java.util.List;

/**
 * Admin activity for managing all events in the system.
 * Allows administrators to view a list of all events and delete them if necessary.
 */
public class ManageEventsActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private ActivityManageEventsBinding binding;
    private EventController eventController;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventController = new EventController();
        setupUI();
        loadAllEvents();
    }

    /**
     * Sets up the RecyclerView and Toolbar.
     */
    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        adapter = new EventAdapter(this);
        adapter.setAdminMode(true); // Enables the delete button in the adapter
        binding.rvManageEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvManageEvents.setAdapter(adapter);
    }

    /**
     * Loads all events from the database to display to the admin.
     */
    private void loadAllEvents() {
        eventController.getAllEvents(new EventController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                adapter.submitList(events);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageEventsActivity.this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles clicking on an event (could open details if needed).
     */
    @Override
    public void onEventClick(Event event) {
        // Optional: Admin could view details here
    }

    /**
     * Handles the admin deleting an event.
     */
    @Override
    public void onDeleteClick(Event event) {
        eventController.deleteEvent(event.getId(), new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ManageEventsActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                loadAllEvents(); // Refresh the list
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageEventsActivity.this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
