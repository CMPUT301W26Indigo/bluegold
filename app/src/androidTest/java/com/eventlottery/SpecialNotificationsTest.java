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
 * Instrumented tests for US 01.05.06 and US 01.09.01.
 * Verifies notifications for private event invitations and co-organizer invitations.
 */
@RunWith(AndroidJUnit4.class)
public class SpecialNotificationsTest {

    private FirebaseFirestore db;
    private String eventId;
    private String attendeeId;
    private List<String> createdNotifIds;

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        eventId = "special_test_event_" + UUID.randomUUID().toString();
        attendeeId = "attendee_" + UUID.randomUUID().toString();
        createdNotifIds = new ArrayList<>();
    }

    @After
    public void tearDown() {
        for (String id : createdNotifIds) {
            db.collection("notifications").document(id).delete();
        }
        db.collection("events").document(eventId).delete();
    }

    /**
     * US 01.05.06: Verifies notification for private event waitlist invitation.
     */
    @Test
    public void testPrivateEventInvitationNotification() throws InterruptedException {
        String notifId = UUID.randomUUID().toString();
        createdNotifIds.add(notifId);

        Notification n = new Notification(
                notifId,
                "You've been invited to join the waitlist for a private event!",
                attendeeId,
                eventId,
                "INVITATION",
                new Date()
        );

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("notifications").document(notifId).set(n)
                .addOnCompleteListener(task -> latch.countDown());
        
        if (!latch.await(10, TimeUnit.SECONDS)) fail("Setup failed");

        verifyNotificationExists("invited to join the waitlist");
    }

    /**
     * US 01.09.01: Verifies notification for co-organizer invitation.
     */
    @Test
    public void testCoOrganizerInvitationNotification() throws InterruptedException {
        String notifId = UUID.randomUUID().toString();
        createdNotifIds.add(notifId);

        Notification n = new Notification(
                notifId,
                "You have been invited to be a co-organizer for an event.",
                attendeeId,
                eventId,
                "INVITATION",
                new Date()
        );

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("notifications").document(notifId).set(n)
                .addOnCompleteListener(task -> latch.countDown());
        
        if (!latch.await(10, TimeUnit.SECONDS)) fail("Setup failed");

        verifyNotificationExists("invited to be a co-organizer");
    }

    private void verifyNotificationExists(String expectedMessagePart) throws InterruptedException {
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] found = {false};

        db.collection("notifications")
                .whereEqualTo("attendeeId", attendeeId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Notification n = doc.toObject(Notification.class);
                        if (n.getMessage().contains(expectedMessagePart)) {
                            found[0] = true;
                            break;
                        }
                    }
                    verifyLatch.countDown();
                });

        assertTrue("Special notification not found for message part: " + expectedMessagePart, verifyLatch.await(10, TimeUnit.SECONDS) && found[0]);
    }
}
