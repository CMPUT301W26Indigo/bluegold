package com.eventlottery.ui.organizer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivityCreateEventBinding;

public class CreateEventActivity extends AppCompatActivity {

    private ActivityCreateEventBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
    }

    private void setupUI() {
        binding.cancelButton.setOnClickListener(v -> finish());
        
        binding.createEventButton.setOnClickListener(v -> {
            // Logic to save event and generate QR
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
