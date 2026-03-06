package com.eventlottery.ui.organizer;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivityOrganizerDashboardBinding;

public class OrganizerDashboardActivity extends AppCompatActivity {

    private ActivityOrganizerDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
    }

    private void setupUI() {
        binding.btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            startActivity(intent);
        });
        
        // Setup other UI components like RecyclerView for events
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
