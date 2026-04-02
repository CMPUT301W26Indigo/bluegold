package com.eventlottery.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
 * Updated to verify "Closed Account" behavior: scrubbing associations from all collections.
 * This test suite verifies that when a user deletes their profile, they are scrubbed from
 * all associated event collections (Waitlists and GuestLists).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DeleteUserTest {

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
     * Helper to set up a mock user with optional association data.
     */
    private void setupMockUser(boolean withWaitlists, boolean withGuestLists) throws InterruptedException {
        Attendee mockAttendee = new Attendee();
        mockAttendee.setAttendeeID(deviceId);
        mockAttendee.setName("Max Power");
        mockAttendee.setEmail("test@myapp.com");
        mockAttendee.setPhoneNumber("7805551234");
        mockAttendee.setAddress("123 Fake St");

        CountDownLatch setupLatch = new CountDownLatch(2);

        if (withWaitlists) {
            ArrayList<String> waitlists = new ArrayList<>();
            waitlists.add("event_wait_1");
            mockAttendee.setWaitListed(waitlists);
            
            // Create the waitlist documents in Firestore
            Waitlist wl = new Waitlist("event_wait_1");
            wl.addAttendee(deviceId);
            wl.saveToFirebase();
        }

        if (withGuestLists) {
            ArrayList<AttendeeEventHistory> history = new ArrayList<>();
            history.add(new AttendeeEventHistory("event_guest_1"));
            mockAttendee.setEventHistory(history);
            
            // Create the guestlist document
            GuestList gl = new GuestList("event_guest_1");
            gl.addGuestAttendee(deviceId); // Default status is "maybe"
            gl.saveToFirebase();
        }

        db.collection("attendees").document(deviceId).set(mockAttendee)
                .addOnCompleteListener(task -> setupLatch.countDown());
        db.collection("users").document(deviceId).set(mockAttendee)
                .addOnCompleteListener(task -> setupLatch.countDown());
        setupLatch.await(10, TimeUnit.SECONDS);
        
        // Brief pause to allow UI to sync with Firestore updates
        Thread.sleep(2000);
    }

    /**
     * Use Case: Entrant deletes profile successfully.
     * Verifies basic document removal.
     */
    @Test
    public void testDeleteProfile_Success() throws InterruptedException {
        setupMockUser(false, false);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(5000); 

        CountDownLatch verifyLatch = new CountDownLatch(2);
        db.collection("users").document(deviceId).get().addOnCompleteListener(task -> {
            assertFalse("User doc should be deleted", task.getResult().exists());
            verifyLatch.countDown();
        });
        db.collection("attendees").document(deviceId).get().addOnCompleteListener(task -> {
            assertFalse("Attendee doc should be deleted", task.getResult().exists());
            verifyLatch.countDown();
        });
        
        assertTrue("Deletion verification timed out", verifyLatch.await(10, TimeUnit.SECONDS));
    }

    /**
     * Use Case: Entrant cancels deletion.
     */
    @Test
    public void testDeleteProfile_CancelledByUser() throws InterruptedException {
        setupMockUser(false, false);

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
     * Use Case: Cleanup - Only Waitlist.
     * Verifies user is cleared from waitlist.
     */
    @Test
    public void testDeleteProfile_OnlyWaitlist() throws InterruptedException {
        setupMockUser(true, false);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(6000);

        CountDownLatch verifyLatch = new CountDownLatch(1);
        db.collection("waitlists").document("event_wait_1").get().addOnSuccessListener(doc -> {
            Waitlist wl = doc.toObject(Waitlist.class);
            if (wl != null) {
                assertFalse("User should be removed from waitlist", wl.getAttendeeIds().contains(deviceId));
            }
            verifyLatch.countDown();
        });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
    }

    /**
     * Use Case: Cleanup - Only GuestList.
     * Verifies user ID is scrubbed from GuestList (Defunct Account handling).
     * EXPECTED TO FAIL until the Controller is updated to remove the ID.
     */
    @Test
    public void testDeleteProfile_OnlyGuestListScrubbing() throws InterruptedException {
        setupMockUser(false, true);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(6000);

        CountDownLatch verifyLatch = new CountDownLatch(1);
        db.collection("guestlists").document("event_guest_1").get().addOnSuccessListener(doc -> {
            GuestList gl = doc.toObject(GuestList.class);
            if (gl != null) {
                // Assert that the specific deviceId key is NO LONGER in the attendees map
                assertFalse("User ID should be scrubbed from guest list (Closed Account behavior)", 
                        gl.getAttendees().containsKey(deviceId));
            }
            verifyLatch.countDown();
        });
        assertTrue("GuestList scrubbing verification failed (Expected failure)", verifyLatch.await(10, TimeUnit.SECONDS));
    }

    /**
     * Use Case: Cleanup - Both Waitlist and GuestList.
     * EXPECTED TO FAIL on the GuestList scrubbing portion until implemented.
     */
    @Test
    public void testDeleteProfile_ComplexCleanupScrubbing() throws InterruptedException {
        setupMockUser(true, true);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(8000); 

        CountDownLatch verifyLatch = new CountDownLatch(3);

        db.collection("waitlists").document("event_wait_1").get().addOnSuccessListener(doc -> {
            Waitlist wl = doc.toObject(Waitlist.class);
            if (wl != null) {
                assertFalse("User ID should be missing from waitlist", wl.getAttendeeIds().contains(deviceId));
            }
            verifyLatch.countDown();
        });

        db.collection("guestlists").document("event_guest_1").get().addOnSuccessListener(doc -> {
            GuestList gl = doc.toObject(GuestList.class);
            if (gl != null) {
                // Verify original ID is scrubbed from guestlist map
                assertFalse("User ID should be scrubbed from guest list map", 
                        gl.getAttendees().containsKey(deviceId));
            }
            verifyLatch.countDown();
        });

        db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
            assertFalse("Primary user document should be gone", doc.exists());
            verifyLatch.countDown();
        });

        assertTrue("Cleanup verification timed out", verifyLatch.await(15, TimeUnit.SECONDS));
    }

    /**
     * Use Case: Resilience test (Partial Data).
     */
    @Test
    public void testDeleteProfile_ResiliencePartialData() throws InterruptedException {
        setupMockUser(false, false);
        
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
