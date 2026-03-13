package com.eventlottery.ui.organizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.eventlottery.databinding.ActivityManageEventBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ManageEventActivity extends AppCompatActivity {

    private ActivityManageEventBinding binding;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra("EVENT_ID");
        eventName = getIntent().getStringExtra("EVENT_NAME");

        setupUI();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        binding.btnDrawLottery.setOnClickListener(v -> {
            startActivity(new Intent(this, DrawLotteryActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }


    private void exportCSV() {
        ArrayList<String> names = new ArrayList<>();

        db.collection("events").document(eventId)
                .collection("guestList")
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder csv = new StringBuilder();
                    csv.append("Name,Email,Phone\n");

                    // If no entrants
                    int totalCount = queryDocumentSnapshots.size();
                    if (totalCount == 0) {
                        Toast.makeText(this, "No confirmed entrants", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("name");
                                    String email = userDoc.getString("email");
                                    String phone = userDoc.getString("phone");

                                    // Handle commas in names
                                    if (name != null && name.contains(",")) {
                                        name = "\"" + name + "\"";
                                    }

                                    csv.append(name).append(",")
                                            .append(email).append(",")
                                            .append(phone).append("\n");

                                    // Share when done
                                    if (names.size() == queryDocumentSnapshots.size() - 1) {
                                        shareCSV(csv.toString());
                                    }
                                    names.add(name);
                                });
                    }
                });
    }

    private void shareCSV(String csvContent) {
        try {
            File file = new File(getExternalFilesDir(null), "entrants.csv");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(csvContent.getBytes());
            fos.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share CSV"));
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getCancelledList() {
        // find all the attendees on the guestlist with the cancelled status
    }

}
