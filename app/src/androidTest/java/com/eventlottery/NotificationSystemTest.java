package com.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.eventlottery.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test to replicate the notification failure scenario.
 * 
 * Scenario:
 * 1. User A (Organizer) and User B (Entrant) are on a waitlist.
 * 2. User A withdraws.
 * 3. User B is selected.
 * 4. Verify if User B can retrieve the notification using the app's query logic.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationSystemTest {

    private static final String TAG = "NotificationSystemTest";
    private FirebaseFirestore db;
    private String eventId;
    private String userAId;
    private String userBId;

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        eventId = "test_event_" + UUID.randomUUID().toString();
        userAId = "user_A_" + UUID.randomUUID().toString();
        userBId = "user_B_" + UUID.randomUUID().toString();
    }

    @Test
    public void testNotificationRetrievalForSelectedUser() throws InterruptedException {
        // 1. Setup Event and Waitlist
        Map<String, Object> waitlistData = new HashMap<>();
        waitlistData.put("status", "waiting");

        CountDownLatch setupLatch = new CountDownLatch(2);
        db.collection("events").document(eventId).collection("waitlist").document(userAId).set(waitlistData)
                .addOnCompleteListener(task -> setupLatch.countDown());
        db.collection("events").document(eventId).collection("waitlist").document(userBId).set(waitlistData)
                .addOnCompleteListener(task -> setupLatch.countDown());
        
        if (!setupLatch.await(5, TimeUnit.SECONDS)) fail("Setup timed out");

        // 2. User A Withdraws
        CountDownLatch withdrawLatch = new CountDownLatch(1);
        db.collection("events").document(eventId).collection("waitlist").document(userAId).delete()
                .addOnCompleteListener(task -> withdrawLatch.countDown());
        
        if (!withdrawLatch.await(5, TimeUnit.SECONDS)) fail("Withdraw timed out");

        // 3. User B is Selected (Simulating DrawLotteryActivity logic)
        String notifId = UUID.randomUUID().toString();
        Notification notification = new Notification(
                notifId,
                "You have been selected for Test Event",
                userBId,
                eventId,
                "INVITATION",
                new Date()
        );
        // Explicitly set isRead to false as seen in DrawLotteryActivity
        notification.setRead(false);

        CountDownLatch selectLatch = new CountDownLatch(3);
        // Move to guestList
        Map<String, Object> guestData = new HashMap<>();
        guestData.put("status", "invited");
        db.collection("events").document(eventId).collection("guestList").document(userBId).set(guestData)
                .addOnCompleteListener(task -> selectLatch.countDown());
        // Remove from waitlist
        db.collection("events").document(eventId).collection("waitlist").document(userBId).delete()
                .addOnCompleteListener(task -> selectLatch.countDown());
        // Create Notification
        db.collection("notifications").document(notifId).set(notification)
                .addOnCompleteListener(task -> selectLatch.countDown());

        if (!selectLatch.await(5, TimeUnit.SECONDS)) fail("Selection timed out");

        // 4. Verification: Query notifications as User B (Exactly as in NotificationListFragment)
        // This is the part that fails in production
        CountDownLatch queryLatch = new CountDownLatch(1);
        final boolean[] success = {false};
        final Exception[] errorHolder = {null};

        Query query = db.collection("notifications")
                .whereEqualTo("attendeeId", userBId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int count = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Notification n = document.toObject(Notification.class);
                    if (n.getAttendeeId().equals(userBId)) {
                        count++;
                    }
                }
                if (count > 0) {
                    success[0] = true;
                } else {
                    Log.e(TAG, "No notifications found for attendeeId: " + userBId);
                }
            } else {
                errorHolder[0] = task.getException();
                Log.e(TAG, "Query Failed: ", task.getException());
            }
            queryLatch.countDown();
        });

        if (!queryLatch.await(10, TimeUnit.SECONDS)) fail("Query timed out");

        // Analysis of results
        if (errorHolder[0] != null) {
            String msg = errorHolder[0].getMessage();
            if (msg != null && msg.contains("FAILED_PRECONDITION") && msg.contains("index")) {
                fail("REPLICATED: Missing Firestore Composite Index. You must create the index for attendeeId (Asc) and timestamp (Desc). Error: " + msg);
            } else {
                fail("Query failed with unexpected error: " + msg);
            }
        }

        if (!success[0]) {
            fail("REPLICATED: Query returned 0 results despite notification existing in Firestore. This often happens if 'timestamp' is missing or if there is an ID mismatch.");
        }

        Log.d(TAG, "Test Passed: Notification retrieved successfully for User B");
    }
}
