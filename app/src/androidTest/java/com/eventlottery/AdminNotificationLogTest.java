package com.eventlottery;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.eventlottery.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * US 03.08.01: As an administrator, I want to review logs of all notifications.
 * Verifies that notifications are stored in a way that an admin can retrieve them.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminNotificationLogTest {

    private FirebaseFirestore db;

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * US 03.08.01: Verifies that a newly sent notification appears in the global logs.
     */
    @Test
    public void testAdminCanSeeNotificationLogs() throws InterruptedException {
        String testMessage = "Admin Log Test Message " + UUID.randomUUID().toString();
        String notifId = UUID.randomUUID().toString();

        Notification n = new Notification(
                notifId,
                testMessage,
                "some_entrant_id",
                "some_event_id",
                "INFO",
                new Date()
        );

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("notifications").document(notifId).set(n)
                .addOnCompleteListener(task -> latch.countDown());

        if (!latch.await(10, TimeUnit.SECONDS)) fail("Setup failed");

        // Verify Admin Query (Admins query the entire collection)
        CountDownLatch adminLatch = new CountDownLatch(1);
        final boolean[] found = {false};

        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        if (testMessage.equals(doc.getString("message"))) {
                            found[0] = true;
                            break;
                        }
                    }
                    adminLatch.countDown();
                });

        assertTrue("Admin could not find notification in logs", adminLatch.await(10, TimeUnit.SECONDS) && found[0]);
        
        // Cleanup
        db.collection("notifications").document(notifId).delete();
    }
}
