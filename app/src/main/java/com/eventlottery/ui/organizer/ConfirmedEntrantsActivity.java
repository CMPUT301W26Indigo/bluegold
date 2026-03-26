package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.eventlottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

/**
 * Displays a list of entrants who have confirmed their attendance.
 *
 * This activity shows organizers all users with "confirmed" status in the
 * guest list. It displays the total count and individual names of confirmed entrants.
 *
 * User stories implemented:
 * 02.06.03
 *
 * Layout file: activity_confirmed_entrants.xml
 *
 * Outstanding issues:
 * - No export functionality from this screen (currently in ManageEventActivity)
 *
 * @see ManageEventActivity
 * @see com.google.firebase.firestore.FirebaseFirestore
 */
public class ConfirmedEntrantsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> entrantNames = new ArrayList<>();
    private FirebaseFirestore db;
    private String eventId;
    private TextView tvTotalCount;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmed_entrants);

        eventId = getIntent().getStringExtra("EVENT_ID");
        db = FirebaseFirestore.getInstance();

        listView = findViewById(R.id.listView);

        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entrantNames);
        listView.setAdapter(adapter);

        loadConfirmedEntrants();
    }

    private void loadConfirmedEntrants() {
        db.collection("events").document(eventId)
                .collection("guestList")
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrantNames.clear();

                    int total = queryDocumentSnapshots.size();
                    tvTotalCount.setText("Total Confirmed: " + total);

                    if (total == 0) {
                        tvEmptyState.setVisibility(android.view.View.VISIBLE);
                        listView.setVisibility(android.view.View.GONE);
                        return;
                    }

                    tvEmptyState.setVisibility(android.view.View.GONE);
                    listView.setVisibility(android.view.View.VISIBLE);

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("name");
                                    if (name != null && !name.isEmpty()) {
                                        entrantNames.add(name);
                                    } else {
                                        entrantNames.add(userId);
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}