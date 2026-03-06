package com.eventlottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivityAdminDashboardBinding;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
    }

    private void setupNavigation() {
        binding.cardManageEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageEventsActivity.class));
        });

        binding.cardManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageUsersActivity.class));
        });

        binding.cardReviewImages.setOnClickListener(v -> {
            startActivity(new Intent(this, ReviewImagesActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
