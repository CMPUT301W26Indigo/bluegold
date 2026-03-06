package com.eventlottery.ui.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivityReviewImagesBinding;

public class ReviewImagesActivity extends AppCompatActivity {

    private ActivityReviewImagesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewImagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
