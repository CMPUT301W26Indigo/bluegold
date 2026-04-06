package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display the list of entrants on the waitlist for a specific event.
 */
public class ViewWaitlistActivity extends AppCompatActivity {

    private static final String TAG = "ViewWaitlistActivity";
    private ListView listView;
    private EntrantAdapter adapter;
    private FirebaseFirestore db;
    private String eventId;
    private List<WaitlistedEntrant> entrants = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_waitlist);

        eventId = getIntent().getStringExtra("EVENT_ID");
        db = FirebaseFirestore.getInstance();

        listView = findViewById(R.id.listView);
        adapter = new EntrantAdapter();
        listView.setAdapter(adapter);

        loadWaitlist();
    }

    private void loadWaitlist() {
        if (eventId == null) {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrants.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        
                        // Fetch the user's details from the attendees collection
                        db.collection("attendees").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String name = userDoc.getString("name");
                                        String email = userDoc.getString("email");
                                        String phone = userDoc.getString("phoneNumber");

                                        WaitlistedEntrant entrant = new WaitlistedEntrant(
                                                userId,
                                                name != null ? name : "Unknown User",
                                                email != null ? email : "No email",
                                                phone != null ? phone : "No phone"
                                        );
                                        entrants.add(entrant);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading waitlist", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private static class WaitlistedEntrant {
        String id, name, email, phone;

        WaitlistedEntrant(String id, String name, String email, String phone) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
    }

    private class EntrantAdapter extends ArrayAdapter<WaitlistedEntrant> {
        EntrantAdapter() {
            super(ViewWaitlistActivity.this, 0, entrants);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_invited_entrant, parent, false);
            }

            WaitlistedEntrant entrant = getItem(position);

            TextView tvName = convertView.findViewById(R.id.tvEntrantName);
            TextView tvEmail = convertView.findViewById(R.id.tvEntrantEmail);
            TextView tvPhone = convertView.findViewById(R.id.tvEntrantPhone);
            
            // Hide action buttons as they are not needed for waitlist viewing
            View btnConfirm = convertView.findViewById(R.id.btnConfirm);
            View btnDecline = convertView.findViewById(R.id.btnDecline);
            if (btnConfirm != null) btnConfirm.setVisibility(View.GONE);
            if (btnDecline != null) btnDecline.setVisibility(View.GONE);

            if (tvName != null) tvName.setText(entrant.name);
            if (tvEmail != null) tvEmail.setText(entrant.email);
            if (tvPhone != null) tvPhone.setText(entrant.phone);

            return convertView;
        }
    }
}
