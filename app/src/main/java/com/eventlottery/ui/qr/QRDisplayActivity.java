package com.eventlottery.ui.qr;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.databinding.ActivityQrDisplayBinding;
import com.eventlottery.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * QRDisplayActivity - View for viewing QR codes.
 * Part of the 'View' in MVC.
 */
public class QRDisplayActivity extends AppCompatActivity {

    private ActivityQrDisplayBinding binding;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrDisplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("EVENT_ID");

        if (eventId == null) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /**
     * Sets up the UI elements with the QR Code
     */
    private void setupUI() {
        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);

                        if (event != null) {
                            binding.ivQr.setImageBitmap(event.generateQRBitmap(event.getQrCodeUrl()));
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
