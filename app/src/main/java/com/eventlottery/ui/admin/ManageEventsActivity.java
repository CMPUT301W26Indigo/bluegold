package com.eventlottery.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
 * Admin activity to manage all events in the system.
 * Displays both public and private events.
 */
public class ManageEventsActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private static final String TAG = "ManageEventsActivity";
    private ActivityManageEventsBinding binding;
    private EventAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        db = FirebaseFirestore.getInstance();
        setupUI();
        loadAllEvents();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        adapter = new EventAdapter(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
