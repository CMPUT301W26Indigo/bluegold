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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;

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
                adapter.setNotifications(notifications);
                binding.tvEmptyState.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
            }
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
            processInvitationResponse(notification, "confirmed", "ACCEPTED");
        }
    }

    @Override
    public void onDeclineInvitation(Notification notification) {
        if (currentMode == Mode.ENTRANT) {
            processInvitationResponse(notification, "declined", "DECLINED");
        }
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
