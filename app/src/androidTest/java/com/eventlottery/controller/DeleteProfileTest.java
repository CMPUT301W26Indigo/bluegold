package com.eventlottery.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.Manifest;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.eventlottery.R;
import com.eventlottery.model.Attendee;
import com.eventlottery.model.AttendeeEventHistory;
import com.eventlottery.model.GuestList;
import com.eventlottery.model.Waitlist;
import com.eventlottery.ui.entrant.ProfileActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Robust UI Instrumented Test Suite for US 01.02.04 (Delete Profile).
 * Verifies cascading cleanup, redraw signaling, and identifier-based safety.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DeleteProfileTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    private ActivityScenario<ProfileActivity> scenario;
    private FirebaseFirestore db;
    private String deviceId;
    private String otherUserId;

    @Before
    public void setUp() throws InterruptedException {
        db = FirebaseFirestore.getInstance();
        otherUserId = "other_user_" + UUID.randomUUID().toString();
        
        CountDownLatch idLatch = new CountDownLatch(1);
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            deviceId = id;
            idLatch.countDown();
        });
        idLatch.await(5, TimeUnit.SECONDS);

        cleanUpTestData();
    }

    @After
    public void tearDown() throws InterruptedException {
        if (scenario != null) {
            scenario.close();
        }
        cleanUpTestData();
    }

    /**
     * Wipes all possible test documents to ensure no pollution in Firebase.
     */
    private void cleanUpTestData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10);
        db.collection("users").document(deviceId).delete().addOnCompleteListener(t -> latch.countDown());
        db.collection("attendees").document(deviceId).delete().addOnCompleteListener(t -> latch.countDown());
        db.collection("users").document(otherUserId).delete().addOnCompleteListener(t -> latch.countDown());
        db.collection("attendees").document(otherUserId).delete().addOnCompleteListener(t -> latch.countDown());
        
        for (int i = 0; i < 3; i++) {
            db.collection("waitlists").document("event_wait_" + i).delete().addOnCompleteListener(t -> latch.countDown());
            db.collection("guestlists").document("event_guest_" + i).delete().addOnCompleteListener(t -> latch.countDown());
        }
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Helper to setup two users with same name but different IDs using valid data provided by the user.
     */
    private void setupMockUsers(int numWaitlists, int numGuestLists) throws InterruptedException {
        // 1. Create Profile 1: Max Power (Primary)
        Attendee primary = new Attendee();
        primary.setID(deviceId);
        primary.setName("Max Power");
        primary.setAddress("123 Fake St NW");
        primary.setPhoneNumber("7806660420");
        primary.setEmail("test@myapp.now");

        // 2. Create Profile 2: Max Power (Secondary)
        Attendee secondary = new Attendee();
        secondary.setID(otherUserId);
        secondary.setName("Max Power");
        secondary.setAddress("123 Fake St SW");
        secondary.setPhoneNumber("7804200666");
        secondary.setEmail("test@myapp.com");

        CountDownLatch setupLatch = new CountDownLatch(4);

        // Add primary user to waitlists
        for (int i = 0; i < numWaitlists; i++) {
            String eid = "event_wait_" + i;
            Waitlist wl = new Waitlist(eid);
            wl.addAttendee(deviceId);
            wl.saveToFirebase();
        }

        // Add primary user to guest lists
        for (int i = 0; i < numGuestLists; i++) {
            String eid = "event_guest_" + i;
            GuestList gl = new GuestList(eid);
            gl.addGuestAttendee(deviceId);
            gl.saveToFirebase();
        }

        db.collection("attendees").document(deviceId).set(primary).addOnCompleteListener(t -> setupLatch.countDown());
        db.collection("users").document(deviceId).set(primary).addOnCompleteListener(t -> setupLatch.countDown());
        db.collection("attendees").document(otherUserId).set(secondary).addOnCompleteListener(t -> setupLatch.countDown());
        db.collection("users").document(otherUserId).set(secondary).addOnCompleteListener(t -> setupLatch.countDown());
        
        setupLatch.await(10, TimeUnit.SECONDS);
        scenario = ActivityScenario.launch(ProfileActivity.class);
        Thread.sleep(3000);
    }

    /**
     * Verifies that only the profile matching the device ID is deleted when names are identical.
     */
    @Test
    public void testDeleteProfile_OnlyDeletesOwnID() throws InterruptedException {
        setupMockUsers(0, 0);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(5000);

        CountDownLatch verifyLatch = new CountDownLatch(2);
        db.collection("users").document(deviceId).get().addOnSuccessListener(doc -> {
            assertFalse("My user profile should be gone", doc.exists());
            verifyLatch.countDown();
        });
        db.collection("users").document(otherUserId).get().addOnSuccessListener(doc -> {
            assertTrue("Other user with same name should still exist", doc.exists());
            verifyLatch.countDown();
        });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
    }

    /**
     * Verifies user is scrubbed from legacy Waitlists and GuestLists.
     */
    @Test
    public void testDeleteProfile_LegacyScrubbing() throws InterruptedException {
        setupMockUsers(2, 2);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(7000);

        CountDownLatch verifyLatch = new CountDownLatch(4);
        for (int i = 0; i < 2; i++) {
            String wid = "event_wait_" + i;
            String gid = "event_guest_" + i;
            db.collection("waitlists").document(wid).get().addOnSuccessListener(doc -> {
                Waitlist wl = doc.toObject(Waitlist.class);
                assertFalse(wl != null && wl.getAttendeeIds().contains(deviceId));
                verifyLatch.countDown();
            });
            db.collection("guestlists").document(gid).get().addOnSuccessListener(doc -> {
                GuestList gl = doc.toObject(GuestList.class);
                assertFalse(gl != null && gl.getAttendees().containsKey(deviceId));
                verifyLatch.countDown();
            });
        }
        assertTrue(verifyLatch.await(15, TimeUnit.SECONDS));
    }

    /**
     * Verifies that deleting a confirmed user opens a spot for a redraw.
     */
    @Test
    public void testDeleteProfile_RedrawSignaling() throws InterruptedException {
        String eventId = "redraw_event_" + UUID.randomUUID().toString();
        // Setup event with 1 confirmed attendee
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("confirmedCount", 1);
        db.collection("events").document(eventId).set(eventData).addOnCompleteListener(t -> {});
        
        // Mock our status as confirmed in the sub-collection using valid data
        Attendee primary = new Attendee();
        primary.setID(deviceId);
        primary.setName("Max Power");
        primary.setAddress("123 Fake St NW");
        primary.setPhoneNumber("7806660420");
        primary.setEmail("test@myapp.now");

        ArrayList<AttendeeEventHistory> historyList = new ArrayList<>();
        historyList.add(new AttendeeEventHistory(eventId));
        primary.setEventHistory(historyList);
        
        db.collection("attendees").document(deviceId).set(primary).addOnCompleteListener(t -> {});
        db.collection("events").document(eventId).collection("guestList").document(deviceId).set(new HashMap<String, Object>(){{
            put("status", "confirmed");
        }}).addOnCompleteListener(t -> {});

        Thread.sleep(2000);
        scenario = ActivityScenario.launch(ProfileActivity.class);

        onView(withId(R.id.btnDeleteProfile)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(7000);

        CountDownLatch verifyLatch = new CountDownLatch(1);
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            Long count = doc.getLong("confirmedCount");
            assertEquals("Event should show 0 confirmed after deletion to prompt redraw", 0L, count.longValue());
            verifyLatch.countDown();
        });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
        
        // Final cleanup of the test event
        db.collection("events").document(eventId).delete();
    }
}
