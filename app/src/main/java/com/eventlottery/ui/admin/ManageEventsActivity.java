package com.eventlottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eventlottery.controller.AdminController;
import com.eventlottery.databinding.ActivityManageEventsBinding;
import com.eventlottery.model.Event;
import com.eventlottery.ui.adapters.AdminManageEventsAdapter;

import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.eventlottery.databinding.ActivityManageEventsBinding;
import com.eventlottery.model.Event;
import com.eventlottery.ui.adapters.EventAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * ManageEventsActivity
 *
 * Displays a list of all events for the administrator to browse
 * The admin can tap "View Comments" on any event to see that event's comments for moderation
 *
 * Part of the 'View' in MVC.
 */
public class ManageEventsActivity extends AppCompatActivity {

    private static final String TAG = "ManageEventsActivity";

    private ActivityManageEventsBinding binding;
    private AdminController adminController;
    private List<Event> eventList;
    private AdminManageEventsAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adminController = new AdminController();

        setupUI();
        setupRecyclerView();
        loadEvents();
    }

    /**
     * Sets up the toolbar with a back navigation button.
     */
    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        adapter = new EventAdapter(this);
        adapter.setAdminMode(true);
        binding.rvManageEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvManageEvents.setAdapter(adapter);
    }

    /**
     * Loads all events from Firestore without filtering by privacy status.
     */
    private void loadAllEvents() {
        db.collection("events")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error loading events", error);
                    return;
                }

                if (value != null) {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        Event event = doc.toObject(Event.class);
                        event.setId(doc.getId());
                        events.add(event);
                    }
                    adapter.submitList(events);
                }
            });
    }

    @Override
    public void onEventClick(Event event) {
        // Handle event click, e.g., open event details for management
        Log.d(TAG, "Event clicked: " + event.getName());
    }

    /**
     * Initializes the RecyclerView with an empty adapter.
     * Tapping "View Comments" on an event launches AdminEventCommentsActivity.
     */
    private void setupRecyclerView() {
        eventList = new ArrayList<>();
        eventAdapter = new AdminManageEventsAdapter(eventList, event -> {
            Intent intent = new Intent(this, AdminEventCommentsActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            intent.putExtra("EVENT_NAME", event.getName());
            startActivity(intent);
        });
        binding.rvManageEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvManageEvents.setAdapter(eventAdapter);
    }

    /**
     * Fetches all events from Firestore and populates the RecyclerView.
     */
    private void loadEvents() {
        adminController.getAllEvents(new AdminController.OnDataLoadedListener<Event>() {
            @Override
            public void onDataLoaded(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading events", e);
                Toast.makeText(ManageEventsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(Event event) {
        showDeleteConfirmation(event);
    }

    private void showDeleteConfirmation(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete event: " + event.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    showFinalDeleteConfirmation(event);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFinalDeleteConfirmation(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage("This action is permanent and cannot be undone. Are you REALLY sure you want to delete " + event.getName() + "?")
                .setPositiveButton("YES, DELETE", (dialog, which) -> {
                    performDelete(event);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performDelete(Event event) {
        db.collection("events").document(event.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting event", e);
                    Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
