package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Displays a list of entrants who have been invited but haven't responded yet.
 *
 * This activity implements Deliverable 1, showing organizers all users with
 * "invited" status. Organizers can manually update status or send reminders.
 *
 * User stories implemented:
 * - As an organizer, I want to view a list of all chosen entrants who are invited (Deliverable 1)
 */
public class InvitedEntrantsActivity extends AppCompatActivity {

    private ListView listView;
    private EntrantAdapter adapter;
    private FirebaseFirestore db;
    private String eventId;
    private List<InvitedEntrant> entrants = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_entrants);

        eventId = getIntent().getStringExtra("EVENT_ID");
        db = FirebaseFirestore.getInstance();

        listView = findViewById(R.id.listView);
        adapter = new EntrantAdapter();
        listView.setAdapter(adapter);

        loadInvitedEntrants();
    }

    private void loadInvitedEntrants() {
        db.collection("events").document(eventId)
                .collection("guestList")
                .whereEqualTo("status", "invited")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrants.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView emptyView = findViewById(R.id.tvEmptyState);
                        if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        long invitedAt = doc.getLong("invitedAt") != null ? doc.getLong("invitedAt") : 0;

                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("name");
                                    String email = userDoc.getString("email");
                                    String phone = userDoc.getString("phone");

                                    InvitedEntrant entrant = new InvitedEntrant(
                                            userId,
                                            name != null ? name : "Unknown",
                                            email != null ? email : "",
                                            phone != null ? phone : "",
                                            invitedAt
                                    );
                                    entrants.add(entrant);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStatus(String userId, String newStatus) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        if ("confirmed".equals(newStatus)) {
            updates.put("confirmedAt", System.currentTimeMillis());
        } else if ("declined".equals(newStatus)) {
            updates.put("declinedAt", System.currentTimeMillis());
        }

        db.collection("events").document(eventId)
                .collection("guestList").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    loadInvitedEntrants(); // Refresh list

                    // If declined, trigger replacement draw
                    if ("declined".equals(newStatus)) {
                        triggerReplacementDraw();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show();
                });
    }

    private void triggerReplacementDraw() {
        // Get event capacity
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    int capacity = eventDoc.getLong("capacity").intValue();

                    // Get current confirmed count
                    db.collection("events").document(eventId)
                            .collection("guestList")
                            .whereEqualTo("status", "confirmed")
                            .get()
                            .addOnSuccessListener(confirmedQuery -> {
                                int confirmedCount = confirmedQuery.size();

                                if (confirmedCount < capacity) {
                                    // Need to draw replacement
                                    db.collection("events").document(eventId)
                                            .collection("waitlist")
                                            .get()
                                            .addOnSuccessListener(waitlistQuery -> {
                                                if (!waitlistQuery.isEmpty()) {
                                                    // Randomly select replacement
                                                    int randomIndex = (int) (Math.random() * waitlistQuery.size());
                                                    QueryDocumentSnapshot replacement = (QueryDocumentSnapshot) waitlistQuery.getDocuments().get(randomIndex);
                                                    String replacementId = replacement.getId();

                                                    // Move to guestList as invited
                                                    HashMap<String, Object> guestEntry = new HashMap<>();
                                                    guestEntry.put("status", "invited");
                                                    guestEntry.put("invitedAt", System.currentTimeMillis());

                                                    db.collection("events").document(eventId)
                                                            .collection("guestList").document(replacementId)
                                                            .set(guestEntry)
                                                            .addOnSuccessListener(aVoid -> {
                                                                // Remove from waitlist
                                                                db.collection("events").document(eventId)
                                                                        .collection("waitlist").document(replacementId)
                                                                        .delete();

                                                                Toast.makeText(this, "Replacement selected!", Toast.LENGTH_SHORT).show();
                                                                loadInvitedEntrants();
                                                            });
                                                }
                                            });
                                }
                            });
                });
    }

    // Inner class for entrant data
    private static class InvitedEntrant {
        String id, name, email, phone;
        long invitedAt;

        InvitedEntrant(String id, String name, String email, String phone, long invitedAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.invitedAt = invitedAt;
        }
    }

    // Custom adapter with confirm/decline buttons
    private class EntrantAdapter extends ArrayAdapter<InvitedEntrant> {

        EntrantAdapter() {
            super(InvitedEntrantsActivity.this, 0, entrants);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_invited_entrant, parent, false);
            }

            InvitedEntrant entrant = getItem(position);

            TextView tvName = convertView.findViewById(R.id.tvEntrantName);
            TextView tvEmail = convertView.findViewById(R.id.tvEntrantEmail);
            TextView tvPhone = convertView.findViewById(R.id.tvEntrantPhone);
            Button btnConfirm = convertView.findViewById(R.id.btnConfirm);
            Button btnDecline = convertView.findViewById(R.id.btnDecline);

            if (tvName != null) tvName.setText(entrant.name);
            if (tvEmail != null) tvEmail.setText(entrant.email);
            if (tvPhone != null) tvPhone.setText(entrant.phone);

            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(v -> updateStatus(entrant.id, "confirmed"));
            }
            if (btnDecline != null) {
                btnDecline.setOnClickListener(v -> updateStatus(entrant.id, "declined"));
            }

            return convertView;
        }
    }
}