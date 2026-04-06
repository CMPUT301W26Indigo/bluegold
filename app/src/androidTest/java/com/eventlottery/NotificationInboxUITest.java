package com.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.eventlottery.model.Attendee;
import com.eventlottery.model.Notification;
import com.eventlottery.ui.entrant.NotificationsActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UI Tests for the Notification Inbox.
 * Verifies US 01.04.01 and US 01.04.02 from an end-user perspective.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NotificationInboxUITest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    private FirebaseFirestore db;
    private String deviceId;
    private String notifId;

    @Before
    public void setUp() throws InterruptedException {
        db = FirebaseFirestore.getInstance();
        
        // 1. Get the REAL device ID so the activity's query finds our test data
        CountDownLatch idLatch = new CountDownLatch(1);
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            deviceId = id;
            idLatch.countDown();
        });
        idLatch.await(5, TimeUnit.SECONDS);

        notifId = "ui_test_" + UUID.randomUUID().toString();

        // 2. Pre-inject a notification linked to the real device ID
        Notification n = new Notification(
                notifId,
                "UI Test Message",
                deviceId,
                "test_event_id",
                "INFO",
                new Date()
        );

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("notifications").document(notifId).set(n)
                .addOnCompleteListener(task -> latch.countDown());
        latch.await(10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        db.collection("notifications").document(notifId).delete();
    }

    /**
     * US 01.04.01 / US 01.04.02 (UI): Verifies that notifications correctly appear in the inbox.
     */
    @Test
    public void testNotificationDisplayInInbox() throws InterruptedException {
        // Launch activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), NotificationsActivity.class);
        
        try (androidx.test.core.app.ActivityScenario<NotificationsActivity> scenario = androidx.test.core.app.ActivityScenario.launch(intent)) {
            // Wait for real-time Firestore sync to populate the RecyclerView
            Thread.sleep(5000);

            // Verify the notification message is visible on screen
            onView(withText("UI Test Message")).check(matches(isDisplayed()));
        }
    }
}
