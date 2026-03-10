package com.eventlottery.ui.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.FragmentMyEventsBinding;
import com.eventlottery.ui.adapters.EventAdapter;
import java.util.ArrayList;

/**
 * Fragment for the User's Events.
 * Part of the 'View' in MVC.
 */
public class MyEventsFragment extends Fragment {

    private FragmentMyEventsBinding binding;
    private EventAdapter eventAdapter;
    private EventController eventController;

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
        setupRecyclerView();
        loadUserEvents();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(event -> {
            // Navigate to event details
        });
        binding.rvMyEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMyEvents.setAdapter(eventAdapter);
    }

    private void loadUserEvents() {
        // Mock data or load from controller based on user registration
        eventAdapter.submitList(new ArrayList<>());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
