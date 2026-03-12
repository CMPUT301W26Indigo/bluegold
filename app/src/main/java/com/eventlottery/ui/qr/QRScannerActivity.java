package com.eventlottery.ui.qr;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.R;
import com.eventlottery.databinding.ActivityQrScannerBinding;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScannerActivity extends AppCompatActivity {

    private CaptureManager capture;
    private ActivityQrScannerBinding binding;
    Button btn_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        btn_scan = findViewById(R.id.btn_scan);
//        capture = new CaptureManager(this, binding.zxingBarcodeScanner);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        btn_scan.setOnClickListener(v -> {
            scanCode();
        });
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(QRScannerActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    });

//    @Override
//    protected void onResume() {
//        super.onResume();
//        capture.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        capture.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        capture.onDestroy();
//        binding = null;
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        capture.onSaveInstanceState(outState);
//    }
}
