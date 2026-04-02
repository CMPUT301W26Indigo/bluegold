package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays a list of entrants who have been invited but haven't responded yet.
 */
public class InvitedEntrantsActivity extends AppCompatActivity {

    private static final String TAG = "InvitedEntrantsActivity";
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
                    TextView emptyView = findViewById(R.id.tvEmptyState);

                    if (queryDocumentSnapshots.isEmpty()) {
                        if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    if (emptyView != null) emptyView.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        long invitedAt = doc.getLong("invitedAt") != null ? doc.getLong("invitedAt") : 0;

                        // Important: Check 'attendees' collection (not 'users') as that's where profiles are stored
                        db.collection("attendees").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("name");
                                    String email = userDoc.getString("email");
                                    String phone = userDoc.getString("phoneNumber"); // Using phoneNumber from Attendee model

                                    InvitedEntrant entrant = new InvitedEntrant(
                                            userId,
                                            name != null ? name : "Unknown ID: " + userId,
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
        WriteBatch batch = db.batch();

        // 1. Update EVENT'S guestList
        Map<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("status", newStatus);
        if ("confirmed".equals(newStatus)) {
            eventUpdates.put("confirmedAt", System.currentTimeMillis());
        } else if ("declined".equals(newStatus)) {
            eventUpdates.put("declinedAt", System.currentTimeMillis());
        }
        batch.update(db.collection("events").document(eventId)
                .collection("guestList").document(userId), eventUpdates);

        // 2. Update ATTENDEE'S Selected sub-collection
        Map<String, Object> attendeeUpdates = new HashMap<>();
        attendeeUpdates.put("status", newStatus);
        batch.update(db.collection("attendees").document(userId)
                .collection("Selected").document(eventId), attendeeUpdates);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
            loadInvitedEntrants(); // Refresh list

            if ("declined".equals(newStatus)) {
                triggerReplacementDraw();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error updating status batch", e);
            Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show();
        });
    }

    private void triggerReplacementDraw() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) return;
                    
                    Long capacityLong = eventDoc.getLong("capacity");
                    int capacity = (capacityLong != null) ? capacityLong.intValue() : 0;
                    String eventName = eventDoc.getString("name");

                    db.collection("events").document(eventId)
                            .collection("guestList")
                            .whereEqualTo("status", "confirmed")
                            .get()
                            .addOnSuccessListener(confirmedQuery -> {
                                int confirmedCount = confirmedQuery.size();

                                if (confirmedCount < capacity) {
                                    db.collection("events").document(eventId)
                                            .collection("waitlist")
                                            .get()
                                            .addOnSuccessListener(waitlistQuery -> {
                                                if (!waitlistQuery.isEmpty()) {
                                                    List<DocumentSnapshot> docs = waitlistQuery.getDocuments();
                                                    int randomIndex = (int) (Math.random() * docs.size());
                                                    DocumentSnapshot replacement = docs.get(randomIndex);
                                                    String replacementId = replacement.getId();

                                                    // Call performReplacementDraw to handle all sub-collections
                                                    performReplacementDraw(replacementId, eventName);
                                                }
                                            });
                                }
                            });
                });
    }

    private void performReplacementDraw(String userId, String eventName) {
        WriteBatch batch = db.batch();

        // 1. Event guestList (invited)
        Map<String, Object> guestEntry = new HashMap<>();
        guestEntry.put("status", "invited");
        guestEntry.put("invitedAt", System.currentTimeMillis());
        batch.set(db.collection("events").document(eventId)
                .collection("guestList").document(userId), guestEntry);

        // 2. Event waitlist (delete)
        batch.delete(db.collection("events").document(eventId)
                .collection("waitlist").document(userId));

        // 3. Attendee waitlist (delete)
        batch.delete(db.collection("attendees").document(userId)
                .collection("waitListed").document(eventId));

        // 4. Attendee Selected (add)
        Map<String, Object> selectedEntry = new HashMap<>();
        selectedEntry.put("status", "invited");
        selectedEntry.put("selectedAt", System.currentTimeMillis());
        batch.set(db.collection("attendees").document(userId)
                .collection("Selected").document(eventId), selectedEntry);

        // 5. Notification
        Map<String, Object> notif = new HashMap<>();
        notif.put("attendeeId", userId);
        notif.put("eventId", eventId);
        notif.put("message", "Good news! A spot opened up for " + eventName + ". You've been selected!");
        notif.put("type", "INVITATION");
        notif.put("status", "PENDING");
        notif.put("isRead", false);
        notif.put("timestamp", new java.util.Date());
        batch.set(db.collection("notifications").document(java.util.UUID.randomUUID().toString()), notif);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Replacement selected and notified!", Toast.LENGTH_SHORT).show();
            loadInvitedEntrants();
        });
    }

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
