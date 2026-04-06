package com.eventlottery.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.eventlottery.R;
import com.eventlottery.model.Event;
import com.eventlottery.ui.organizer.ManageEventActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * DeleteEventTest
 *
 * Verifies that an event can be successfully deleted both through the OrganizerController
 * and through the ManageEventActivity UI.
 *
 * This test ensures that the "Delete Event" functionality correctly removes the event
 * document from Firestore and handles the UI flow as expected.
 */
@RunWith(AndroidJUnit4.class)
public class DeleteEventTest {

    private OrganizerController organizerController;
    private FirebaseFirestore db;

    @Before
    public void setUp() {
        organizerController = new OrganizerController();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Verifies that OrganizerController.deleteEvent removes the document from Firestore.
     */
    @Test
    public void testControllerDeleteEvent() throws InterruptedException {
        String eventId = createTestEvent("Controller Delete Test");
        assertNotNull("Failed to create test event", eventId);

        CountDownLatch latch = new CountDownLatch(1);
        organizerController.deleteEvent(eventId, new OrganizerController.OnOperationListener() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
            }
        });

        assertTrue("Timeout deleting event via controller", latch.await(10, TimeUnit.SECONDS));

        // Verify it's gone from Firestore
        boolean exists = checkEventExists(eventId);
        assertFalse("Event should be deleted from Firestore by the controller", exists);
    }

    /**
     * Verifies that clicking the Delete button in ManageEventActivity
     * triggers the deletion flow and removes the event.
     */
    @Test
    public void testActivityDeleteEvent() throws InterruptedException {
        String eventId = createTestEvent("Activity Delete Test");
        assertNotNull("Failed to create test event", eventId);

        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, ManageEventActivity.class);
        intent.putExtra("EVENT_ID", eventId);

        try (ActivityScenario<ManageEventActivity> scenario = ActivityScenario.launch(intent)) {
            // Click the delete button in the activity - use scrollTo() first
            onView(withId(R.id.btnDeleteEvent)).perform(scrollTo(), click());

            // Click "Delete" in the confirmation dialog
            onView(withText("Delete")).perform(click());

            // Give some time for the deletion and activity finish
            Thread.sleep(2000);
        }

        // Verify it's gone from Firestore
        boolean exists = checkEventExists(eventId);
        assertFalse("Event should be deleted after clicking delete in UI", exists);
    }

    /**
     * Helper method to create a test event in Firestore.
     */
    private String createTestEvent(String name) throws InterruptedException {
        Event event = new Event();
        event.setName(name);
        event.setDescription("Integration Test Event");
        event.setOrganizerId("test_organizer");

        final String[] id = {null};
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").add(event)
                .addOnSuccessListener(documentReference -> {
                    id[0] = documentReference.getId();
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());
        
        latch.await(10, TimeUnit.SECONDS);
        return id[0];
    }

    /**
     * Helper method to check if an event exists in Firestore.
     */
    private boolean checkEventExists(String eventId) throws InterruptedException {
        final boolean[] exists = {false};
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").document(eventId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        exists[0] = doc.exists();
                    }
                    latch.countDown();
                });
        
        latch.await(10, TimeUnit.SECONDS);
        return exists[0];
    }
}
