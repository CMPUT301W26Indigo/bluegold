package com.eventlottery.ui.qr;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.databinding.ActivityQrDisplayBinding;
import com.eventlottery.model.Event;

/**
 * QRDisplayActivity - View for viewing QR codes.
 * Part of the 'View' in MVC.
 */
public class QRDisplayActivity extends AppCompatActivity {

    private ActivityQrDisplayBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrDisplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }
        Event event = getIntent().getParcelableExtra("EVENT");
        Bitmap qrBitmap = event.generateQRBitmap(event.getQrCodeUrl());
        if (qrBitmap != null) {
            binding.ivQr.setImageBitmap(qrBitmap);
        }
    }
}
