package com.eventlottery.view.qr;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.databinding.ActivityQrScannerBinding;

/**
 * QRScannerActivity - View for scanning QR codes.
 * Part of the 'View' in MVC.
 */
public class QRScannerActivity extends AppCompatActivity {

    private ActivityQrScannerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Scanner logic here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
