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

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.eventlottery.R;
import com.eventlottery.controller.EventController;
import com.eventlottery.ui.entrant.EventDetailsActivity;

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

    @Before
    public void setUp() {
        eventController = new EventController();
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
     * Test for making sure users cannot join after registration end
     * @throws InterruptedException if the thread is interrupted
     */
    @Test
    public void testCannotJoinWhenRegistrationClosed() throws InterruptedException {
        // Create an event where registration ended 1 hour ago
        Event event = new Event();
        event.setName("Closed Registration Event");
        event.setDescription("Testing closed registration");
        event.setRegistrationOpens(System.currentTimeMillis() - 7200000); // 2 hours ago
        event.setRegistrationCloses(System.currentTimeMillis() - 3600000); // 1 hour ago
        event.setStatus("open");
        event.setTags(new ArrayList<>());

        String id = createTestEvent(event);

        // Launch Activity directly with this event
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        androidx.test.core.app.ActivityScenario.launch(intent);

        sleep(2000);

        // Verify button is disabled and shows "Registration Closed"
        onView(withId(R.id.joinWaitlistBtn))
                .check(matches(not(isEnabled())))
                .check(matches(withText("Registration Closed")));
    }

    /**
     * Test for making sure users cannot join when waitlist is full
     * @throws InterruptedException if the thread is interrupted
     */
    @Test
    public void testCannotJoinWhenWaitlistFull() throws InterruptedException {
        // Create an event with a waitlist limit of 1 and 1 person already on it
        Event event = new Event();
        event.setName("Full Waitlist Event");
        event.setWaitlistLimit(1);
        event.setWaitlistCount(1); // Simulate full count
        event.setRegistrationOpens(System.currentTimeMillis() - 3600000);
        event.setRegistrationCloses(System.currentTimeMillis() + 3600000);
        event.setStatus("open");
        event.setTags(new ArrayList<>());

        String id = createTestEvent(event);

        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        androidx.test.core.app.ActivityScenario.launch(intent);

        sleep(2000);

        onView(withId(R.id.joinWaitlistBtn))
                .check(matches(not(isEnabled())))
                .check(matches(withText("Waitlist Full")));
    }

    /**
     * Test for making sure users cannot join when outside geolocation radius
     * @throws InterruptedException if the thread is interrupted
     */
    @Test
    public void testCannotJoinWhenOutsideGeolocation() throws InterruptedException {
        // Create an event in Edmonton with 1km radius
        Event event = new Event();
        event.setName("Geo-restricted Event");
        event.setGeolocationEnabled(true);
        event.setGeolocationRadius(1);
        event.setLatitude(53.5461);  // Edmonton
        event.setLongitude(-113.4938);
        event.setRegistrationOpens(System.currentTimeMillis() - 3600000);
        event.setRegistrationCloses(System.currentTimeMillis() + 3600000);
        event.setStatus("open");
        event.setTags(new ArrayList<>());

        String id = createTestEvent(event);

        // Launch with a location far away (e.g. Calgary)
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", id);
        intent.putExtra("USER_LAT", 51.0447); // Calgary
        intent.putExtra("USER_LON", -114.0719);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        androidx.test.core.app.ActivityScenario.launch(intent);

        sleep(2000);

        onView(withId(R.id.joinWaitlistBtn))
                .check(matches(not(isEnabled())))
                .check(matches(withText("Event Outside Geolocation Radius")));
    }
}
