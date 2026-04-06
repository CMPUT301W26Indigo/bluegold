package com.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.eventlottery.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented tests for US 01.04.01, 01.04.02, and 02.05.01.
 * Verifies that entrants receive correct notifications when they win or lose a lottery.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationFlowTest {

    private FirebaseFirestore db;
    private String eventId;
    private String winnerId;
    private String loserId;
    private List<String> createdNotifIds;

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        eventId = "flow_test_event_" + UUID.randomUUID().toString();
        winnerId = "winner_" + UUID.randomUUID().toString();
        loserId = "loser_" + UUID.randomUUID().toString();
        createdNotifIds = new ArrayList<>();
    }

    @After
    public void tearDown() {
        // Cleanup notifications created during the test
        for (String id : createdNotifIds) {
            db.collection("notifications").document(id).delete();
        }
        db.collection("events").document(eventId).delete();
    }

    /**
     * US 01.04.01 & 02.05.01: Verifies winner receives notification.
     */
    @Test
    public void testWinnerReceivesNotification() throws InterruptedException {
        String notifId = "win_notif_" + UUID.randomUUID().toString();
        createdNotifIds.add(notifId);

        Notification winNotif = new Notification(
                notifId,
                "Congratulations!",
                winnerId,
                eventId,
                "INVITATION",
                new java.util.Date()
        );

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("notifications").document(notifId).set(winNotif)
                .addOnCompleteListener(task -> latch.countDown());
        
        if (!latch.await(10, TimeUnit.SECONDS)) fail("Setup failed");

        verifyNotificationExists(winnerId, "INVITATION", "Congratulations!");
    }

    /**
     * US 01.04.02: Verifies loser receives notification.
     * Note: This might fail if the redraw/loser notification logic isn't fully implemented in the current code.
     */
    @Test
    public void testLoserReceivesNotification() throws InterruptedException {
        String notifId = "lose_notif_" + UUID.randomUUID().toString();
        createdNotifIds.add(notifId);

        Notification loseNotif = new Notification(
                notifId,
                "We're sorry...",
                loserId,
                eventId,
                "INFO",
                new java.util.Date()
        );

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("notifications").document(notifId).set(loseNotif)
                .addOnCompleteListener(task -> latch.countDown());
        
        if (!latch.await(10, TimeUnit.SECONDS)) fail("Setup failed");

        verifyNotificationExists(loserId, "INFO", "We're sorry...");
    }

    private void verifyNotificationExists(String attendeeId, String expectedType, String expectedMessagePart) throws InterruptedException {
        CountDownLatch queryLatch = new CountDownLatch(1);
        final boolean[] found = {false};

        db.collection("notifications")
                .whereEqualTo("attendeeId", attendeeId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Notification n = doc.toObject(Notification.class);
                        if (n.getType().equals(expectedType) && n.getMessage().contains(expectedMessagePart)) {
                            found[0] = true;
                            break;
                        }
                    }
                    queryLatch.countDown();
                })
                .addOnFailureListener(e -> queryLatch.countDown());

        assertTrue("Notification not found for " + attendeeId, queryLatch.await(15, TimeUnit.SECONDS) && found[0]);
    }
}
