package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class InvitedEntrantsActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> entrantNames = new ArrayList<>();
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_entrants);

        eventId = getIntent().getStringExtra("EVENT_ID");
        db = FirebaseFirestore.getInstance();

        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entrantNames);
        listView.setAdapter(adapter);

        loadInvitedEntrants();
    }

    private void loadInvitedEntrants() {
        db.collection("events").document(eventId)
                .collection("guestList")
                .whereEqualTo("status", "invited")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrantNames.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("name");
                                    if (name != null) entrantNames.add(name);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
