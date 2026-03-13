package com.eventlottery.data.models;
import com.eventlottery.R;
import com.eventlottery.ui.organizer.OrganizerDashboardActivity;

import androidx.test.espresso.Espresso;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.is;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.eventlottery.ui.organizer.OrganizerDashboardActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class testEventCreation {
    @Rule
    public ActivityScenarioRule<OrganizerDashboardActivity> activityRule = new ActivityScenarioRule<OrganizerDashboardActivity>(OrganizerDashboardActivity.class);

    @Test
    public void testingEventCreation() {
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.createEventButton)).perform(click());
        onView(withId(R.id.eventNameEditText)).perform(typeText("Test Event"));
        onView(withId(R.id.descriptionEditText)).perform(typeText("Test Description"));
        onView(withId(R.id.eventDateEditText)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.eventTimeEditText)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.registrationOpensEditText)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.registrationClosesEditText)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.capacityEditText)).perform(typeText("100"));
        onView(withId(R.id.waitlistLimitSwitch)).perform(click());
        onView(withId(R.id.waitlistLimitEditText)).perform(typeText("100"));

        onView(withId(R.id.geolocationSwitch)).perform(click());
        onView(withId(R.id.locationEditText)).perform(typeText("Test Location"));
        onView(withId(R.id.radiusEditText)).perform(typeText("10"));

        onView(withId(R.id.chipSports)).perform(click());
        onView(withId(R.id.chipMusic)).perform(click());

        onView(withId(R.id.priceEditText)).perform(typeText("1"));

        onView(withId(R.id.createEventButton)).perform(click());

        onView(withText("Test Event")).check(matches(isDisplayed()));

    }


}
