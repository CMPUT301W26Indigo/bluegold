package com.eventlottery.model;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.eventlottery.R;
import com.eventlottery.controller.EventController;
import com.eventlottery.ui.entrant.EventDetailsActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * UI Tests for the Entrant side of the app.
 * Testing to make sure Joining Waitlist is working as expected.
 * Written by Google Gemini, Prompt: "could you write in
 * testEventEntrantSide.java tests that create events which
 * should be unable for the entrant to enter say, not in
 * geolocation range, registration has ended,
 * waitlist limit full? and then check if its still
 * possible for the entrant to enter?"
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class testEventEntrantSide {

    private EventController eventController;
    private final Context context = ApplicationProvider.getApplicationContext();

    private String deviceId;

    @Before
    public void setUp() throws InterruptedException {
        eventController = new EventController();

        // Ensure we have a valid device ID
        CountDownLatch idLatch = new CountDownLatch(1);
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            deviceId = id;
            idLatch.countDown();
        });
        idLatch.await(5, TimeUnit.SECONDS);

        // Seed a valid profile so we can actually "join" (or attempt to)
        Attendee attendee = new Attendee();
        attendee.setID(deviceId);
        attendee.setName("Test Entrant");
        attendee.setEmail("test@example.com");
        attendee.setPhoneNumber("1234567890");

        CountDownLatch profileLatch = new CountDownLatch(1);
        FirebaseFirestore.getInstance().collection("attendees").document(deviceId).set(attendee)
                .addOnCompleteListener(task -> profileLatch.countDown());
        profileLatch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Helper to create an event programmatically for testing
     */
    private String createTestEvent(Event event) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] eventId = new String[1];
        eventController.addEvent(event, new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                eventId[0] = event.getId();
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
        return eventId[0];
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test for making sure users can join when all conditions are met
     * @throws InterruptedException if the thread is interrupted
     */
    @Test
    public void testJoinWaitlist_Success() throws InterruptedException {
        Event event = new Event();
        event.setName("Valid Event");
        event.setRegistrationOpens(System.currentTimeMillis() - 10000);
        event.setRegistrationCloses(System.currentTimeMillis() + 100000);
        event.setStatus("open");

        String eventId = createTestEvent(event);

        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario.launch(intent);

        sleep(2000);

        onView(withId(R.id.joinWaitlistBtn)).check(matches(isEnabled()));
        onView(withId(R.id.joinWaitlistBtn)).check(matches(withText("Join Waitlist")));
        onView(withId(R.id.joinWaitlistBtn)).perform(scrollTo(), click());

        sleep(3000);

        onView(withId(R.id.joinWaitlistBtn)).check(matches(withText("Leave Waitlist")));

        // Verify in Firestore
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] found = {false};
        FirebaseFirestore.getInstance().collection("events").document(eventId)
                .collection("waitlist").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        found[0] = true;
                    }
                    verifyLatch.countDown();
                });
        verifyLatch.await(5, TimeUnit.SECONDS);
        assert(found[0]);
    }

    /**
     * Test for making sure users cannot join after registration end
     * @throws InterruptedException if the thread is interrupted
     */
    @Test
    public void testCannotJoinWhenRegistrationClosed() throws InterruptedException {
        Event event = new Event();
        event.setName("Closed Reg Event");
        event.setDescription("This event has closed registration.");
        event.setRegistrationOpens(System.currentTimeMillis() - 20000);
        event.setRegistrationCloses(System.currentTimeMillis() - 10000); // Closed 10s ago
        event.setStatus("open");

        String eventId = createTestEvent(event);

        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        androidx.test.core.app.ActivityScenario.launch(intent);

        sleep(2000);

        onView(withId(R.id.joinWaitlistBtn)).check(matches(not(isEnabled())));
        onView(withId(R.id.joinWaitlistBtn)).check(matches(withText("Registration Closed")));
    }

    /**
     * Test for making sure users cannot join when waitlist is full
     * @throws InterruptedException if the thread is interrupted
     */
    @Test
    public void testCannotJoinWhenWaitlistFull() throws InterruptedException {
        Event event = new Event();
        event.setName("Full Waitlist Event");
        event.setWaitlistLimit(1);
        event.setRegistrationOpens(System.currentTimeMillis() - 10000);
        event.setRegistrationCloses(System.currentTimeMillis() + 100000);
        event.setStatus("open");

        String eventId = createTestEvent(event);

        // Manually fill the waitlist by adding another user
        CountDownLatch fillLatch = new CountDownLatch(1);
        FirebaseFirestore.getInstance().collection("events").document(eventId)
                .collection("waitlist").document("other_user_id").set(new java.util.HashMap<>())
                .addOnCompleteListener(t -> fillLatch.countDown());
        fillLatch.await(5, TimeUnit.SECONDS);

        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        androidx.test.core.app.ActivityScenario.launch(intent);

        sleep(3000); // Wait for loadEventStats to run

        onView(withId(R.id.joinWaitlistBtn)).check(matches(not(isEnabled())));
        onView(withId(R.id.joinWaitlistBtn)).check(matches(withText("Event Full")));
    }

    /**
     * Test for making sure users cannot join when outside geolocation radius
     * @throws InterruptedException if the thread is interrupted
     */
    @Test
    public void testCannotJoinWhenOutsideGeolocation() throws InterruptedException {
        Event event = new Event();
        event.setName("Geo-fenced Event");
        event.setGeolocationEnabled(true);
        event.setGeolocationRadius(5); // 5km radius
        event.setLatitude(40.7128); // NYC
        event.setLongitude(-74.0060);
        event.setRegistrationOpens(System.currentTimeMillis() - 10000);
        event.setRegistrationCloses(System.currentTimeMillis() + 100000);
        event.setStatus("open");

        String eventId = createTestEvent(event);

        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        // Set user location far away (e.g., LA)
        intent.putExtra("USER_LAT", 34.0522);
        intent.putExtra("USER_LON", -118.2437);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        androidx.test.core.app.ActivityScenario.launch(intent);

        sleep(2000);

        onView(withId(R.id.joinWaitlistBtn)).check(matches(not(isEnabled())));
        onView(withId(R.id.joinWaitlistBtn)).check(matches(withText("Event Outside Geolocation Radius")));
    }
}
