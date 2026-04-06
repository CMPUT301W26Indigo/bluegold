package com.eventlottery.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.eventlottery.R;
import com.eventlottery.model.Attendee;
import com.eventlottery.model.AttendeeEventHistory;
import com.eventlottery.model.GuestList;
import com.eventlottery.model.Waitlist;
import com.eventlottery.ui.entrant.ProfileActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Extensive UI Instrumented Test Suite for US 01.02.04 (Delete Profile).
 * Verifies that when a user deletes their profile, they are scrubbed from
 * all associated event collections (Waitlists and GuestLists), effectively
 * wiping the profile from existence.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DeleteProfileTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

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

        // Clear existing data for this device ID to ensure a clean state
        CountDownLatch clearLatch = new CountDownLatch(2);
        db.collection("users").document(deviceId).delete().addOnCompleteListener(t -> clearLatch.countDown());
        db.collection("attendees").document(deviceId).delete().addOnCompleteListener(t -> clearLatch.countDown());
        clearLatch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Helper to set up a mock user with multiple association data points.
     */
    private void setupMockUser(int numWaitlists, int numGuestLists) throws InterruptedException {
        Attendee mockAttendee = new Attendee();
        mockAttendee.setID(deviceId);
        mockAttendee.setName("Max Power");
        mockAttendee.setEmail("test@myapp.com");
        mockAttendee.setPhoneNumber("7805551234");
        mockAttendee.setAddress("123 Fake St");

        CountDownLatch setupLatch = new CountDownLatch(2);

        // Add to multiple waitlists
        ArrayList<String> waitlistIds = new ArrayList<>();
        for (int i = 0; i < numWaitlists; i++) {
            String eid = "event_wait_" + i;
            waitlistIds.add(eid);
            Waitlist wl = new Waitlist(eid);
            wl.addAttendee(deviceId);
            wl.saveToFirebase();
        }
        mockAttendee.setWaitListed(waitlistIds);

        // Add to multiple guest lists
        ArrayList<AttendeeEventHistory> history = new ArrayList<>();
        for (int i = 0; i < numGuestLists; i++) {
            String eid = "event_guest_" + i;
            history.add(new AttendeeEventHistory(eid));
            GuestList gl = new GuestList(eid);
            gl.addGuestAttendee(deviceId);
            gl.saveToFirebase();
        }
        mockAttendee.setEventHistory(history);

        db.collection("attendees").document(deviceId).set(mockAttendee)
                .addOnCompleteListener(task -> setupLatch.countDown());
        db.collection("users").document(deviceId).set(mockAttendee)
                .addOnCompleteListener(task -> setupLatch.countDown());
        setupLatch.await(10, TimeUnit.SECONDS);
        
        // Brief pause to allow UI to sync with Firestore updates
        Thread.sleep(2000);
    }

    /**
     * Use Case: Entrant deletes profile successfully (No associations).
     * Also verifies the Activity finishes.
     */
    @Test
    public void testDeleteProfile_Success() throws InterruptedException {
        setupMockUser(0, 0);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(5000); 

        // Verify documents are gone
        CountDownLatch verifyLatch = new CountDownLatch(2);
        db.collection("users").document(deviceId).get().addOnCompleteListener(task -> {
            assertFalse("User doc should be deleted", task.getResult().exists());
            verifyLatch.countDown();
        });
        db.collection("attendees").document(deviceId).get().addOnCompleteListener(task -> {
            assertFalse("Attendee doc should be deleted", task.getResult().exists());
            verifyLatch.countDown();
        });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));

        // Verify the activity is finishing/finished
        activityRule.getScenario().onActivity(activity -> {
            assertTrue("Activity should be finishing after deletion", activity.isFinishing() || activity.isDestroyed());
        });
    }

    /**
     * Use Case: Entrant cancels deletion.
     */
    @Test
    public void testDeleteProfile_CancelledByUser() throws InterruptedException {
        setupMockUser(0, 0);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Cancel")).inRoot(isDialog()).perform(click());

        Thread.sleep(2000);

        CountDownLatch verifyLatch = new CountDownLatch(1);
        db.collection("users").document(deviceId).get().addOnCompleteListener(task -> {
            assertTrue("User doc should still exist after cancellation", task.getResult().exists());
            verifyLatch.countDown();
        });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
    }

    /**
     * Use Case: Extensive Cleanup - Multiple Waitlists.
     * Verifies user is scrubbed from ALL waitlists they joined.
     */
    @Test
    public void testDeleteProfile_MultipleWaitlistScrubbing() throws InterruptedException {
        int numEvents = 3;
        setupMockUser(numEvents, 0);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(7000);

        CountDownLatch verifyLatch = new CountDownLatch(numEvents);
        for (int i = 0; i < numEvents; i++) {
            String eid = "event_wait_" + i;
            db.collection("waitlists").document(eid).get().addOnSuccessListener(doc -> {
                Waitlist wl = doc.toObject(Waitlist.class);
                if (wl != null) {
                    assertFalse("User must be removed from waitlist " + eid, wl.getAttendeeIds().contains(deviceId));
                }
                verifyLatch.countDown();
            });
        }
        assertTrue("Waitlist scrubbing timed out", verifyLatch.await(15, TimeUnit.SECONDS));
    }

    /**
     * Use Case: Extensive Cleanup - Multiple GuestLists.
     * Verifies user is scrubbed from ALL guest lists map.
     */
    @Test
    public void testDeleteProfile_MultipleGuestListScrubbing() throws InterruptedException {
        int numEvents = 3;
        setupMockUser(0, numEvents);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(7000);

        CountDownLatch verifyLatch = new CountDownLatch(numEvents);
        for (int i = 0; i < numEvents; i++) {
            String eid = "event_guest_" + i;
            db.collection("guestlists").document(eid).get().addOnSuccessListener(doc -> {
                GuestList gl = doc.toObject(GuestList.class);
                if (gl != null) {
                    assertFalse("User ID should be scrubbed from guest list " + eid, 
                            gl.getAttendees().containsKey(deviceId));
                }
                verifyLatch.countDown();
            });
        }
        assertTrue("GuestList scrubbing timed out", verifyLatch.await(15, TimeUnit.SECONDS));
    }

    /**
     * Use Case: Cleanup - Both Waitlist and GuestList.
     */
    @Test
    public void testDeleteProfile_ComplexCleanupScrubbing() throws InterruptedException {
        setupMockUser(2, 2);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(8000); 

        CountDownLatch verifyLatch = new CountDownLatch(5); // 2 WL + 2 GL + 1 UserDoc

        db.collection("waitlists").document("event_wait_0").get().addOnSuccessListener(doc -> {
            Waitlist wl = doc.toObject(Waitlist.class);
            assertFalse(wl != null && wl.getAttendeeIds().contains(deviceId));
            verifyLatch.countDown();
        });

        db.collection("guestlists").document("event_guest_0").get().addOnSuccessListener(doc -> {
            GuestList gl = doc.toObject(GuestList.class);
            assertFalse(gl != null && gl.getAttendees().containsKey(deviceId));
            verifyLatch.countDown();
        });
        
        // Check second set
        db.collection("waitlists").document("event_wait_1").get().addOnSuccessListener(doc -> {
            Waitlist wl = doc.toObject(Waitlist.class);
            assertFalse(wl != null && wl.getAttendeeIds().contains(deviceId));
            verifyLatch.countDown();
        });

        db.collection("guestlists").document("event_guest_1").get().addOnSuccessListener(doc -> {
            GuestList gl = doc.toObject(GuestList.class);
            assertFalse(gl != null && gl.getAttendees().containsKey(deviceId));
            verifyLatch.countDown();
        });

        db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
            assertFalse("Primary user document should be gone", doc.exists());
            verifyLatch.countDown();
        });

        assertTrue("Cleanup verification timed out", verifyLatch.await(20, TimeUnit.SECONDS));
    }

    /**
     * Use Case: Resilience test (Partial Data).
     */
    @Test
    public void testDeleteProfile_ResiliencePartialData() throws InterruptedException {
        setupMockUser(0, 0);
        
        CountDownLatch breakLatch = new CountDownLatch(1);
        db.collection("attendees").document(deviceId).delete().addOnCompleteListener(t -> breakLatch.countDown());
        breakLatch.await(5, TimeUnit.SECONDS);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(5000);

        CountDownLatch verifyLatch = new CountDownLatch(1);
        db.collection("users").document(deviceId).get().addOnCompleteListener(task -> {
            assertFalse("Remaining user doc should be deleted", task.getResult().exists());
            verifyLatch.countDown();
        });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
    }
}
