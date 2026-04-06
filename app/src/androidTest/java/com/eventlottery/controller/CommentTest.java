package com.eventlottery.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.eventlottery.R;
import com.eventlottery.model.Attendee;
import com.eventlottery.model.Comment;
import com.eventlottery.ui.entrant.CommentsActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test suite for comment-related user stories:
 *   Entrant posts a comment on an event
 *   Entrant views comments on an event
 *   Organizer views and deletes comments on their event
 *   Organizer posts a comment on their event
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CommentTest {

    private static final String TEST_EVENT_ID  = "test_comment_event";
    private static final String TEST_COMMENT_TEXT = "This is a test comment";
    private static final String FAKE_ORGANIZER_ID = "fake_organizer_id_12345";

    private FirebaseFirestore db;
    private String deviceId;

    @Before
    public void setUp() throws InterruptedException {
        db = FirebaseFirestore.getInstance();

        // Retrieve the current device's Firebase ID
        CountDownLatch idLatch = new CountDownLatch(1);
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            deviceId = id;
            idLatch.countDown();
        });
        idLatch.await(5, TimeUnit.SECONDS);

        // Clear existing comments on the test event for a clean start
        CountDownLatch clearLatch = new CountDownLatch(1);
        db.collection("events").document(TEST_EVENT_ID)
                .collection("comments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    if (total == 0) {
                        clearLatch.countDown();
                        return;
                    }
                    final int[] deleted = {0};
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete().addOnCompleteListener(t -> {
                            deleted[0]++;
                            if (deleted[0] == total) {
                                clearLatch.countDown();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> clearLatch.countDown());
        clearLatch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Seeds a comment into Firestore for the test event.
     * Used in tests that need comments to be present.
     *
     * @param authorId   The ID of the comment author.
     * @param authorName The display name of the comment author.
     * @param text       The comment text.
     * @param isOrganizer Whether the author is the organizer.
     */
    private void seedComment(String authorId, String authorName, String text, boolean isOrganizer)
            throws InterruptedException {
        Comment comment = new Comment(authorId, authorName, text, isOrganizer);
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("events").document(TEST_EVENT_ID)
                .collection("comments")
                .add(comment)
                .addOnCompleteListener(task -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
        // Brief pause so Firestore settles before the activity loads
        Thread.sleep(1000);
    }

    /**
     * Builds an Intent to launch CommentsActivity with the test event and the given organizer ID.
     *
     * @param organizerId The ID to set as the event organizer.
     */
    private Intent buildCommentsIntent(String organizerId) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CommentsActivity.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        intent.putExtra("ORGANIZER_ID", organizerId);
        return intent;
    }

    /**
     * Entrant types a comment and taps Send.
     * Verifies the comment appears in the list and is saved to Firestore.
     */
    @Test
    public void testPostComment_Success() throws InterruptedException {
        // Launch as a regular entrant (FAKE_ORGANIZER_ID ensures current user is NOT organizer)
        ActivityScenario.launch(buildCommentsIntent(FAKE_ORGANIZER_ID));
        Thread.sleep(2000);

        onView(withId(R.id.et_comment_input)).perform(typeText(TEST_COMMENT_TEXT), closeSoftKeyboard());
        onView(withId(R.id.btn_send_comment)).perform(click());

        Thread.sleep(3000);

        // Verify the comment appears in the RecyclerView
        onView(withText(TEST_COMMENT_TEXT)).check(matches(isDisplayed()));

        // Verify the comment was written to Firestore
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] found = {false};
        db.collection("events").document(TEST_EVENT_ID)
                .collection("comments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = doc.toObject(Comment.class);
                        if (TEST_COMMENT_TEXT.equals(comment.getText())) {
                            found[0] = true;
                        }
                    }
                    verifyLatch.countDown();
                });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
        assertTrue("Comment should be saved in Firestore", found[0]);
    }

    /**
     * Entrant taps Send with an empty input field.
     * Verifies nothing is written to Firestore.
     */
    @Test
    public void testPostComment_EmptyInput() throws InterruptedException {
        ActivityScenario.launch(buildCommentsIntent(FAKE_ORGANIZER_ID));
        Thread.sleep(2000);

        // Tap Send without typing anything
        onView(withId(R.id.btn_send_comment)).perform(click());
        Thread.sleep(2000);

        // Verify no comments were written to Firestore
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final int[] count = {0};
        db.collection("events").document(TEST_EVENT_ID)
                .collection("comments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    count[0] = queryDocumentSnapshots.size();
                    verifyLatch.countDown();
                });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
        assertEquals("No comments should be saved for empty input", 0, count[0]);
    }

    /**
     * Existing comments are displayed when the screen loads.
     * Seeds a comment into Firestore before launching the activity.
     */
    @Test
    public void testViewComments_ShowsExistingComments() throws InterruptedException {
        seedComment("some_user_id", "Alice", TEST_COMMENT_TEXT, false);

        ActivityScenario.launch(buildCommentsIntent(FAKE_ORGANIZER_ID));
        Thread.sleep(2000);

        // Verify the seeded comment's text is visible
        onView(withText(TEST_COMMENT_TEXT)).check(matches(isDisplayed()));
    }

    /**
     * No comments yet, screen loads without crashing.
     */
    @Test
    public void testViewComments_EmptyList() throws InterruptedException {
        // No comments seeded — clean state guaranteed by @Before
        ActivityScenario.launch(buildCommentsIntent(FAKE_ORGANIZER_ID));
        Thread.sleep(2000);

        // Verify the activity loaded successfully (input and send button are visible)
        onView(withId(R.id.et_comment_input)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_send_comment)).check(matches(isDisplayed()));
    }

    /**
     * Organizer taps delete
     * Seeds a comment and launches as the organizer (deviceId == organizerId)
     */
    @Test
    public void testOrganizerDeleteComment_ConfirmationDialogAppears() throws InterruptedException {
        seedComment("some_user_id", "Bob", TEST_COMMENT_TEXT, false);

        // Launch as organizer by passing deviceId as the ORGANIZER_ID
        ActivityScenario.launch(buildCommentsIntent(deviceId));
        Thread.sleep(2000);

        // Tap the delete button on the comment
        onView(withId(R.id.btn_delete_comment)).perform(click());

        // Verify the confirmation dialog appears
        onView(withText("Delete Comment")).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    /**
     * Organizer confirms deletion.
     * Verifies the comment is removed from Firestore
     */
    @Test
    public void testOrganizerDeleteComment_Success() throws InterruptedException {
        seedComment("some_user_id", "Bob", TEST_COMMENT_TEXT, false);

        ActivityScenario.launch(buildCommentsIntent(deviceId));
        Thread.sleep(2000);

        // Tap delete then confirm
        onView(withId(R.id.btn_delete_comment)).perform(click());
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        Thread.sleep(3000);

        // Verify the comment is gone from Firestore
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final int[] count = {0};
        db.collection("events").document(TEST_EVENT_ID)
                .collection("comments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    count[0] = queryDocumentSnapshots.size();
                    verifyLatch.countDown();
                });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
        assertEquals("Comment should be deleted from Firestore", 0, count[0]);
    }

    /**
     * Organizer cancels deletion
     * Verifies the comment is still present in Firestore
     */
    @Test
    public void testOrganizerDeleteComment_Cancel() throws InterruptedException {
        seedComment("some_user_id", "Bob", TEST_COMMENT_TEXT, false);

        ActivityScenario.launch(buildCommentsIntent(deviceId));
        Thread.sleep(2000);

        // Tap delete then cancel
        onView(withId(R.id.btn_delete_comment)).perform(click());
        onView(withText("Cancel")).inRoot(isDialog()).perform(click());

        Thread.sleep(2000);

        // Verify the comment is still in Firestore
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final int[] count = {0};
        db.collection("events").document(TEST_EVENT_ID)
                .collection("comments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    count[0] = queryDocumentSnapshots.size();
                    verifyLatch.countDown();
                });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
        assertEquals("Comment should still exist after cancellation", 1, count[0]);
    }

    /**
     * Organizer posts a comment
     * Verifies the comment is saved to Firestore with isOrganizer = true.
     */
    @Test
    public void testOrganizerPostComment_SavedWithOrganizerFlag() throws InterruptedException {
        // Launch as organizer
        ActivityScenario.launch(buildCommentsIntent(deviceId));
        Thread.sleep(2000);

        onView(withId(R.id.et_comment_input)).perform(typeText(TEST_COMMENT_TEXT), closeSoftKeyboard());
        onView(withId(R.id.btn_send_comment)).perform(click());

        Thread.sleep(3000);

        // Verify the comment is saved in Firestore with isOrganizer == true
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] isOrganizerFlagCorrect = {false};
        db.collection("events").document(TEST_EVENT_ID)
                .collection("comments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = doc.toObject(Comment.class);
                        if (TEST_COMMENT_TEXT.equals(comment.getText()) && comment.isOrganizer()) {
                            isOrganizerFlagCorrect[0] = true;
                        }
                    }
                    verifyLatch.countDown();
                });
        assertTrue(verifyLatch.await(10, TimeUnit.SECONDS));
        assertTrue("Comment posted by organizer should have isOrganizer = true", isOrganizerFlagCorrect[0]);
    }

}
