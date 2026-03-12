package com.eventlottery.ui.qr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.databinding.ActivityQrScannerBinding;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScannerActivity extends AppCompatActivity {

    private ActivityQrScannerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        scanCode();
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setCaptureActivity(CaptureAct.class);

        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedURL = result.getContents();
                    new AlertDialog.Builder(QRScannerActivity.this)
                            .setTitle("Result")
                            .setMessage(scannedURL)
                            .setPositiveButton("View Event Details",
                                    (dialog, which) -> goToEvent(scannedURL))
                            .setNegativeButton("Cancel",
                                    (dialog, which) -> finish())
                            .show();
                } else {
                    finish();  // User cancelled
                }
            });

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

    private void goToEvent(String scannedURL) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedURL));
        startActivity(intent);
        finish();
    }
}
