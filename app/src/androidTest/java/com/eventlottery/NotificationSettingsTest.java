package com.eventlottery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented tests for US 01.04.03.
 * Verifies the opt-out mechanism for notifications.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationSettingsTest {

    private FirebaseFirestore db;
    private String attendeeId;

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        attendeeId = "settings_test_" + UUID.randomUUID().toString();
    }

    /**
     * US 01.04.03: Verifies that setting notification preference to false is persisted.
     */
    @Test
    public void testOptOutPersistence() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        // Simulate opting out in the profile
        db.collection("attendees").document(attendeeId)
                .update("notification", false)
                .addOnCompleteListener(task -> latch.countDown());

        // Note: We use update, but if the doc doesn't exist, we should create it
        if (!latch.await(5, TimeUnit.SECONDS)) {
             db.collection("attendees").document(attendeeId).set(new java.util.HashMap<String, Object>(){{
                 put("notification", false);
             }}).addOnCompleteListener(t -> {});
        }

        verifyPreference(false);
    }

    private void verifyPreference(boolean expected) throws InterruptedException {
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] actual = {!expected};

        db.collection("attendees").document(attendeeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean pref = documentSnapshot.getBoolean("notification");
                        actual[0] = (pref != null && pref);
                    }
                    verifyLatch.countDown();
                });

        assertTrue("Settings check timed out", verifyLatch.await(10, TimeUnit.SECONDS));
        assertEquals("Notification preference mismatch", expected, actual[0]);
    }
    
    private void assertEquals(String msg, boolean expected, boolean actual) {
        if (expected != actual) fail(msg + " Expected: " + expected + ", Actual: " + actual);
    }
}
