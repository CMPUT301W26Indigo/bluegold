package com.eventlottery.model;

import com.eventlottery.R;
import com.eventlottery.ui.organizer.OrganizerDashboardActivity;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * UI Tests for the Event Creation flow.
 * Includes stability fixes to handle activity transitions and NestedScrollView.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class testEventCreation {

    @Rule
    public ActivityScenarioRule<OrganizerDashboardActivity> activityRule = 
            new ActivityScenarioRule<>(OrganizerDashboardActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Small helper to add artificial delays where Espresso synchronization fails.
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to wait for a view to appear in the hierarchy.
     */
    private void waitForView(int viewId) {
        long timeout = 8000;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return;
            } catch (NoMatchingViewException | AssertionError e) {
                sleep(200);
            }
        }
        onView(withId(viewId)).check(matches(isDisplayed()));
    }

    @Test
    public void testGeolocationSwitchTogglesRadius() {
        onView(withId(R.id.btnCreateEvent)).perform(click());
        waitForView(R.id.eventNameEditText);
        sleep(500);

        onView(withId(R.id.radiusEditText))
                .perform(scrollTo())
                .check(matches(not(isEnabled())));
        
        onView(withId(R.id.geolocationSwitch))
                .perform(scrollTo(), click());
        
        sleep(500);
        onView(withId(R.id.radiusEditText)).check(matches(isEnabled()));
    }

    @Test
    public void testWaitlistLimitSwitchTogglesInput() {
        onView(withId(R.id.btnCreateEvent)).perform(click());
        waitForView(R.id.eventNameEditText);
        sleep(500);

        onView(withId(R.id.waitlistLimitSwitch)).perform(scrollTo());
        onView(withId(R.id.waitlistLimitEditText)).perform(scrollTo()).check(matches(not(isEnabled())));
        
        onView(withId(R.id.waitlistLimitSwitch)).perform(click());
        sleep(500);
        onView(withId(R.id.waitlistLimitEditText)).check(matches(isEnabled()));
    }

    @Test
    public void testValidationPreventsEmptySubmission() {
        onView(withId(R.id.btnCreateEvent)).perform(click());
        waitForView(R.id.eventNameEditText);
        sleep(500);
        
        onView(withId(R.id.createEventButton)).perform(scrollTo(), click());
        sleep(1000);
        onView(withId(R.id.eventNameEditText)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void testingEventCreationFullFlow() {
        onView(withId(R.id.btnCreateEvent)).perform(click());
        waitForView(R.id.eventNameEditText);
        sleep(1000);

        onView(withId(R.id.eventNameEditText)).perform(scrollTo(), typeText("Full Flow Event"), closeSoftKeyboard());
        onView(withId(R.id.descriptionEditText)).perform(scrollTo(), typeText("Description for full flow."), closeSoftKeyboard());

        // Select Dates
        onView(withId(R.id.eventDateEditText)).perform(scrollTo(), click());
        sleep(1000);
        onView(withText("OK")).perform(click());
        sleep(500);

        onView(withId(R.id.eventTimeEditText)).perform(scrollTo(), click());
        sleep(1000);
        onView(withText("OK")).perform(click());
        sleep(500);

        onView(withId(R.id.registrationOpensEditText)).perform(scrollTo(), click());
        sleep(1000);
        onView(withText("OK")).perform(click());
        sleep(500);

        onView(withId(R.id.registrationClosesEditText)).perform(scrollTo(), click());
        sleep(1000);
        onView(withText("OK")).perform(click());
        sleep(500);

        // Location
        onView(withId(R.id.locationEditText)).perform(scrollTo(), typeText("Edmonton"), closeSoftKeyboard());
        onView(withId(R.id.locationSearchButton)).perform(click());
        sleep(2000);
        onData(anything()).inAdapterView(withId(R.id.locationResultsList)).atPosition(0).perform(click());
        sleep(1000);

        onView(withId(R.id.capacityEditText)).perform(scrollTo(), typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.chipSports)).perform(scrollTo(), click());
        onView(withId(R.id.priceEditText)).perform(scrollTo(), typeText("0"), closeSoftKeyboard());

        onView(withId(R.id.createEventButton)).perform(scrollTo(), click());
        sleep(3000);
        onView(withId(R.id.btnCreateEvent)).check(matches(isDisplayed()));
    }

    @Test
    public void testCreatedEventDetailsMatch() {
        String uniqueName = "DetailTest" + System.currentTimeMillis();
        String description = "Matching parameter verification test.";
        String capacity = "75";
        String locationSearch = "Calgary";

        // 1. Create the event
        onView(withId(R.id.btnCreateEvent)).perform(click());
        waitForView(R.id.eventNameEditText);
        sleep(1000);

        onView(withId(R.id.eventNameEditText)).perform(scrollTo(), typeText(uniqueName), closeSoftKeyboard());
        onView(withId(R.id.descriptionEditText)).perform(scrollTo(), typeText(description), closeSoftKeyboard());

        // Required dates
        onView(withId(R.id.eventDateEditText)).perform(scrollTo(), click());
        sleep(800);
        onView(withText("OK")).perform(click());
        onView(withId(R.id.eventTimeEditText)).perform(scrollTo(), click());
        sleep(800);
        onView(withText("OK")).perform(click());
        onView(withId(R.id.registrationOpensEditText)).perform(scrollTo(), click());
        sleep(800);
        onView(withText("OK")).perform(click());
        onView(withId(R.id.registrationClosesEditText)).perform(scrollTo(), click());
        sleep(800);
        onView(withText("OK")).perform(click());

        // Location
        onView(withId(R.id.locationEditText)).perform(scrollTo(), typeText(locationSearch), closeSoftKeyboard());
        onView(withId(R.id.locationSearchButton)).perform(click());
        sleep(2000);
        onData(anything()).inAdapterView(withId(R.id.locationResultsList)).atPosition(0).perform(click());

        onView(withId(R.id.capacityEditText)).perform(scrollTo(), typeText(capacity), closeSoftKeyboard());
        onView(withId(R.id.chipMusic)).perform(scrollTo(), click());

        onView(withId(R.id.createEventButton)).perform(scrollTo(), click());
        sleep(3000); // Wait for Firestore sync

        // 2. Find and click the event on the dashboard
        // We look for a view with the unique name and click it. 
        // Scroll inside the recycler view if necessary.
        onView(withText(uniqueName)).perform(scrollTo(), click());
        sleep(2000); // Wait for ManageEventActivity to load from Firestore

        // 3. Verify parameters in ManageEventActivity (using activity_manage_event1 IDs)
        onView(withId(R.id.eventNameText)).check(matches(withText(uniqueName)));
        onView(withId(R.id.descriptionText)).check(matches(withText(description)));
        // Capacity in ManageEventActivity is shown in tvConfirmedCount as "0 / 75" (initially)
        onView(withId(R.id.tvConfirmedCount)).check(matches(withText(containsString(capacity))));
        // Tag verification
        onView(withText("Music")).check(matches(isDisplayed()));
    }
}
