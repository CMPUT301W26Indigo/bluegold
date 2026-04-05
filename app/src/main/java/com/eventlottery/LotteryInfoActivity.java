package com.eventlottery;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivityLotteryInfoBinding;

/**
 * Activity that provides information on how the event lotteries work.
 */
public class LotteryInfoActivity extends AppCompatActivity {

    private ActivityLotteryInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLotteryInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
