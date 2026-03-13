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

/**
 * Opens the camera to scan QR Codes.
 * Upon scanning a valid QR Code, user can choose to be taken to the specific event page.
 */
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

    /**
     * Opens the camera with a QR scanner and waits for one to come into the frame
     */
    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setCaptureActivity(CaptureAct.class);

        barLauncher.launch(options);
    }

    /**
     * When a QR is scanned, opens a prompt that allows the user to either go to the event page or cancel
     */
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

    /**
     * Sets up UI
     */
    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    /**
     * Hands any functions upon destruction of activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /**
     * Goes to the respective event details page when user confirms the scan.
     * @param scannedURL
     */
    private void goToEvent(String scannedURL) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedURL));
        startActivity(intent);
        finish();
    }
}
