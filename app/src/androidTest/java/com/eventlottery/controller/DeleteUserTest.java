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
import com.eventlottery.ui.entrant.ProfileActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UI Instrumented Test for US 01.02.04.
 * Verifies the full user flow for profile deletion, including UI prompts and Firestore cleanup.
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
        
        // Get the actual device ID used by the app to create a mock record
        CountDownLatch idLatch = new CountDownLatch(1);
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            deviceId = id;
            idLatch.countDown();
        });
        idLatch.await(5, TimeUnit.SECONDS);

        // Create a mock attendee record with valid data to satisfy model validation
        Attendee mockAttendee = new Attendee();
        mockAttendee.setAttendeeID(deviceId);
        mockAttendee.setName("Max Power");
        mockAttendee.setEmail("test@myapp.com");
        mockAttendee.setPhoneNumber("7805551234"); // Valid 10-digit format
        mockAttendee.setAddress("123 Fake St");
        
        CountDownLatch setupLatch = new CountDownLatch(2);
        db.collection("attendees").document(deviceId).set(mockAttendee)
                .addOnCompleteListener(task -> setupLatch.countDown());
        db.collection("users").document(deviceId).set(mockAttendee)
                .addOnCompleteListener(task -> setupLatch.countDown());
        setupLatch.await(10, TimeUnit.SECONDS);
    }

    /**
     * US 01.02.04: As an entrant, I want to delete my profile.
     * This test mimics the user clicking the delete button and confirming.
     */
    @Test
    public void testDeleteProfileFlow() throws InterruptedException {
        // 1. Check if the profile screen is displayed
        onView(withId(R.id.btnDeleteProfile)).check(matches(isDisplayed()));

        // 2. Click the Delete Profile button
        onView(withId(R.id.btnDeleteProfile)).perform(click());

        // 3. Verify the confirmation dialog appears (Requirement: "with a prompt asking if they are sure")
        onView(withText("Delete Profile")).check(matches(isDisplayed()));
        onView(withText("Are you sure you want to delete your profile? This action cannot be undone."))
                .check(matches(isDisplayed()));

        // 4. Click the "Delete" button in the dialog
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        // 5. Wait for Firestore operations to complete and verify document deletion
        Thread.sleep(5000); 

        CountDownLatch verifyLatch = new CountDownLatch(1);
        db.collection("users").document(deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                assertFalse("User document should be deleted from Firestore", task.getResult().exists());
            }
            verifyLatch.countDown();
        });
        
        assertTrue("Firestore verification timed out", verifyLatch.await(10, TimeUnit.SECONDS));
    }
}
