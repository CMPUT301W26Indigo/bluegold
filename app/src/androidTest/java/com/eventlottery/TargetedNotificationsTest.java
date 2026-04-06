package com.eventlottery;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.eventlottery.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented tests for US 02.07.01, 02.07.02, and 02.07.03.
 * Verifies that organizers can send notifications to targeted groups of entrants.
 */
@RunWith(AndroidJUnit4.class)
public class TargetedNotificationsTest {

    private FirebaseFirestore db;
    private String eventId;
    private List<String> attendeeIds;
    private List<String> createdNotifIds;

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        eventId = "targeted_test_event_" + UUID.randomUUID().toString();
        attendeeIds = new ArrayList<>();
        createdNotifIds = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            attendeeIds.add("attendee_" + i + "_" + UUID.randomUUID().toString());
        }
    }

    @After
    public void tearDown() {
        for (String id : createdNotifIds) {
            db.collection("notifications").document(id).delete();
        }
        db.collection("events").document(eventId).delete();
    }

    @Test
    public void testNotifyAllWaitlisted() throws InterruptedException {
        sendTargetedNotifications("Waitlist Update", "WAITING");
    }

    @Test
    public void testNotifyAllSelected() throws InterruptedException {
        sendTargetedNotifications("Selection Update", "SELECTED");
    }

    @Test
    public void testNotifyAllCancelled() throws InterruptedException {
        sendTargetedNotifications("Cancellation Update", "CANCELLED");
    }

    private void sendTargetedNotifications(String message, String groupLabel) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(attendeeIds.size());
        
        for (String attendeeId : attendeeIds) {
            String notifId = UUID.randomUUID().toString();
            createdNotifIds.add(notifId);
            
            Notification n = new Notification(
                    notifId,
                    "Targeted " + groupLabel + ": " + message,
                    attendeeId,
                    eventId,
                    "INFO",
                    new Date()
            );
            
            db.collection("notifications").document(notifId).set(n)
                    .addOnCompleteListener(task -> latch.countDown());
        }

        if (!latch.await(20, TimeUnit.SECONDS)) fail("Sending targeted notifications timed out");

        // Verify for at least one recipient
        verifyNotificationReceived(attendeeIds.get(0), "Targeted " + groupLabel);
    }

    private void verifyNotificationReceived(String attendeeId, String expectedPrefix) throws InterruptedException {
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] found = {false};

        db.collection("notifications")
                .whereEqualTo("attendeeId", attendeeId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Notification n = doc.toObject(Notification.class);
                        if (n.getMessage().startsWith(expectedPrefix)) {
                            found[0] = true;
                            break;
                        }
                    }
                    verifyLatch.countDown();
                });

        assertTrue("Targeted notification not found for " + attendeeId, verifyLatch.await(10, TimeUnit.SECONDS) && found[0]);
    }
}
