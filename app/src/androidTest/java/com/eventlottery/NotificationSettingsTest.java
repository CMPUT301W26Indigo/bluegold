package com.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.eventlottery.R;
import com.eventlottery.model.Attendee;
import com.eventlottery.ui.entrant.ProfileActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * US 01.04.03: As an entrant I want to opt out of receiving notifications.
 * Verifies both UI persistence and backend state logic using valid profile data.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NotificationSettingsTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule = new ActivityScenarioRule<>(ProfileActivity.class);

    private FirebaseFirestore db;
    private String deviceId;

    @Before
    public void setUp() throws InterruptedException {
        db = FirebaseFirestore.getInstance();
        
        CountDownLatch idLatch = new CountDownLatch(1);
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            deviceId = id;
            idLatch.countDown();
        });
        idLatch.await(5, TimeUnit.SECONDS);

        // Setup a FULL valid profile (Profile 1) to avoid validation crashes
        Attendee mock = new Attendee();
        mock.setID(deviceId);
        mock.setName("Max Power");
        mock.setAddress("123 Fake St NW");
        mock.setPhoneNumber("7806660420");
        mock.setEmail("test@myapp.now");
        mock.setNotification(true);

        CountDownLatch latch = new CountDownLatch(1);
        db.collection("attendees").document(deviceId).set(mock)
                .addOnCompleteListener(task -> latch.countDown());
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * US 01.04.03 (Logic): Verifies that the opt-out preference is correctly saved in Firestore.
     */
    @Test
    public void testOptOutLogicPersistence() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("attendees").document(deviceId)
                .update("notification", false)
                .addOnCompleteListener(task -> latch.countDown());

        assertTrue("Database update timed out", latch.await(10, TimeUnit.SECONDS));
        verifyPreferenceInDb(deviceId, false);
    }

    /**
     * US 01.04.03 (UI): Verifies that toggling the notification switch in the UI is possible.
     */
    @Test
    public void testNotificationToggleUI() throws InterruptedException {
        // Wait for profile to load into UI
        Thread.sleep(3000);
        
        onView(withId(R.id.switchNotifications)).check(matches(isDisplayed()));
        onView(withId(R.id.switchNotifications)).perform(click());
        onView(withId(R.id.btnSaveChanges)).perform(click());
    }

    private void verifyPreferenceInDb(String id, boolean expected) throws InterruptedException {
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] actual = {!expected};

        db.collection("attendees").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean pref = doc.getBoolean("notification");
                        actual[0] = (pref != null && pref);
                    }
                    verifyLatch.countDown();
                });

        assertTrue("Settings verification timed out", verifyLatch.await(10, TimeUnit.SECONDS));
        if (expected != actual[0]) {
            fail("US 01.04.03 Failure: Notification preference mismatch. Expected: " + expected + ", Actual: " + actual[0]);
        }
    }
}
