package com.eventlottery.ui.entrant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.eventlottery.databinding.FragmentNotificationsBinding;
import com.eventlottery.model.Notification;
import com.eventlottery.ui.adapters.NotificationAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reusable Fragment for displaying a list of notifications.
 * Supports both ENTRANT (Inbox) and ADMIN (System Logs) modes.
 */
public class NotificationListFragment extends Fragment implements NotificationAdapter.OnNotificationActionListener {

    private static final String TAG = "NotificationListFragment";
    public enum Mode { ENTRANT, ADMIN }
    
    private static final String ARG_MODE = "mode";
    private static final String ARG_ID = "attendeeId";

    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private FirebaseFirestore db;
    private Mode currentMode = Mode.ENTRANT;
    private String attendeeId;

    public static NotificationListFragment newInstance(Mode mode, @Nullable String attendeeId) {
        NotificationListFragment fragment = new NotificationListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MODE, mode);
        args.putString(ARG_ID, attendeeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentMode = (Mode) getArguments().getSerializable(ARG_MODE);
            attendeeId = getArguments().getString(ARG_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setupUI();
        loadNotifications();
    }

    private void setupUI() {
        adapter = new NotificationAdapter(this);
        if (currentMode == Mode.ADMIN) {
            adapter.setAdminMode(true);
        }
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        Query query;
        if (currentMode == Mode.ADMIN) {
            query = db.collection("notifications").orderBy("timestamp", Query.Direction.DESCENDING);
        } else {
            query = db.collection("notifications")
                    .whereEqualTo("attendeeId", attendeeId != null ? attendeeId : "mock_user_id")
                    .orderBy("timestamp", Query.Direction.DESCENDING);
        }

        query.addSnapshotListener((value, error) -> {
            if (error != null || binding == null) return;
            if (value != null) {
                List<Notification> notifications = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    Notification notification = doc.toObject(Notification.class);
                    notification.setId(doc.getId());
                    notifications.add(notification);
                }
                
                if (currentMode == Mode.ADMIN) {
                    enrichNotifications(notifications);
                } else {
                    adapter.setNotifications(notifications);
                    binding.tvEmptyState.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    /**
     * Dynamically pulls names from Firestore for notifications missing sender/recipient details.
     * Supports US 03.08.01 requirements for administrative logs.
     */
    private void enrichNotifications(List<Notification> list) {
        Set<String> attendeeIdsToFetch = new HashSet<>();
        Set<String> eventIdsToFetch = new HashSet<>();

        for (Notification n : list) {
            if (n.getRecipientName() == null || n.getRecipientName().isEmpty()) {
                if (n.getAttendeeId() != null) attendeeIdsToFetch.add(n.getAttendeeId());
            }
            if (n.getSenderName() == null || n.getSenderName().isEmpty()) {
                if (n.getSenderId() != null) attendeeIdsToFetch.add(n.getSenderId());
                else if (n.getEventId() != null) eventIdsToFetch.add(n.getEventId());
            }
        }

        if (attendeeIdsToFetch.isEmpty() && eventIdsToFetch.isEmpty()) {
            adapter.setNotifications(list);
            binding.tvEmptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        
        // Fetch missing attendee names in chunks (limit of 10 for whereIn)
        List<String> attendeeList = new ArrayList<>(attendeeIdsToFetch);
        for (int i = 0; i < attendeeList.size(); i += 10) {
            tasks.add(db.collection("attendees").whereIn(FieldPath.documentId(), 
                    attendeeList.subList(i, Math.min(i + 10, attendeeList.size()))).get());
        }

        // Fetch missing event names (often used as fallback for senderName)
        List<String> eventList = new ArrayList<>(eventIdsToFetch);
        for (int i = 0; i < eventList.size(); i += 10) {
            tasks.add(db.collection("events").whereIn(FieldPath.documentId(), 
                    eventList.subList(i, Math.min(i + 10, eventList.size()))).get());
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
            if (binding == null) return;
            
            Map<String, String> resolvedNames = new HashMap<>();
            for (Task<QuerySnapshot> task : tasks) {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        resolvedNames.put(doc.getId(), doc.getString("name"));
                    }
                }
            }

            for (Notification n : list) {
                if (n.getRecipientName() == null || n.getRecipientName().isEmpty()) {
                    String name = resolvedNames.get(n.getAttendeeId());
                    n.setRecipientName(name != null ? name : "Unknown ID: " + n.getAttendeeId());
                }
                if (n.getSenderName() == null || n.getSenderName().isEmpty()) {
                    if (n.getSenderId() != null) {
                        String name = resolvedNames.get(n.getSenderId());
                        n.setSenderName(name != null ? name : "Unknown ID: " + n.getSenderId());
                    } else if (n.getEventId() != null) {
                        String name = resolvedNames.get(n.getEventId());
                        n.setSenderName(name != null ? name : "System / Event ID: " + n.getEventId());
                    }
                }
            }
            
            adapter.setNotifications(list);
            binding.tvEmptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if (currentMode == Mode.ENTRANT && !notification.isRead()) {
            db.collection("notifications").document(notification.getId()).update("read", true);
        }
    }

    @Override
    public void onAcceptInvitation(Notification notification) {
        if (currentMode == Mode.ENTRANT) {
            if ("CO_ORGANIZER_INVITE".equals(notification.getType())) {
                processCoOrganizerResponse(notification, "ACCEPTED");
            } else {
                processInvitationResponse(notification, "confirmed", "ACCEPTED");
            }
        }
    }

    @Override
    public void onDeclineInvitation(Notification notification) {
        if (currentMode == Mode.ENTRANT) {
            if ("CO_ORGANIZER_INVITE".equals(notification.getType())) {
                processCoOrganizerResponse(notification, "DECLINED");
            } else {
                processInvitationResponse(notification, "declined", "DECLINED");
            }
        }
    }

    private void processCoOrganizerResponse(Notification n, String status) {
        WriteBatch batch = db.batch();
        DocumentReference notifRef = db.collection("notifications").document(n.getId());
        batch.update(notifRef, "status", status, "read", true);

        if ("ACCEPTED".equals(status)) {
            DocumentReference eventRef = db.collection("events").document(n.getEventId());
            batch.update(eventRef, "coOrganizerIds", FieldValue.arrayUnion(n.getAttendeeId()));
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Co-organizer invitation " + status.toLowerCase(), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error processing co-organizer response", e);
            Toast.makeText(getContext(), "Error sending response", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Updates status in three places:
     * 1. Notification document (status: ACCEPTED/DECLINED)
     * 2. Event's guestList (status: confirmed/declined)
     * 3. Attendee's Selected sub-collection (status: confirmed/declined)
     */
    private void processInvitationResponse(Notification n, String businessStatus, String notificationStatus) {
        WriteBatch batch = db.batch();
        
        // 1. Update Notification status and mark as read
        batch.update(db.collection("notifications").document(n.getId()), 
                "status", notificationStatus, "read", true);
        
        // 2. Update Event's guestList
        batch.update(db.collection("events").document(n.getEventId())
                .collection("guestList").document(n.getAttendeeId()), 
                "status", businessStatus);
        
        // 3. Update Attendee's Selected sub-collection
        batch.update(db.collection("attendees").document(n.getAttendeeId())
                .collection("Selected").document(n.getEventId()), 
                "status", businessStatus);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Response sent: " + notificationStatus, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error processing invitation response", e);
            Toast.makeText(getContext(), "Error sending response", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
