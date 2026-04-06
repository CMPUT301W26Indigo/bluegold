package com.eventlottery.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.eventlottery.R;
import com.eventlottery.model.Comment;
import com.eventlottery.ui.admin.AdminEventCommentsActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;



/**
 * Tests for admin comment moderation
 * Admin can remove event comments
 */

@RunWith(AndroidJUnit4.class)
@LargeTest

public class AdminCommentTest {

    private static final String TEST_EVENT_ID = "test_admin_comment_event";
    private static final String TEST_EVENT_NAME = "Admin Test Event";
    private static final String TEST_COMMENT_TEXT = "This comment violates policy";

    private FirebaseFirestore db;

    @Before
    public void setUp() throws InterruptedException {
        db = FirebaseFirestore.getInstance();

        // Clear all existing comments on the test event
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
     *
     * @param authorId    The ID of the comment author.
     * @param authorName  The display name of the comment author.
     * @param text        The comment text.
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
     * Builds an Intent to launch AdminEventCommentsActivity with the test event.
     */
    private Intent buildAdminCommentsIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminEventCommentsActivity.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        intent.putExtra("EVENT_NAME", TEST_EVENT_NAME);
        return intent;
    }

    /**
     * Existing comments are displayed when admin opens the screen
     * Seeds a comment into Firestore before launching the activity
     */
    @Test
    public void testAdminViewComments_ShowsExistingComments() throws InterruptedException {
        seedComment("some_entrant_id", "Fortnite Battlepass", TEST_COMMENT_TEXT, false);

        ActivityScenario.launch(buildAdminCommentsIntent());
        Thread.sleep(3000);

        // Verify the seeded comment's text is visible
        onView(withText(TEST_COMMENT_TEXT)).check(matches(isDisplayed()));
    }

    /**
     * Edge case: no comments on the event
     * Screen loads without crashing and the RecyclerView is displayed
     */
    @Test
    public void testAdminViewComments_EmptyList() throws InterruptedException {
        // No comments seeded — clean state guaranteed by @Before
        ActivityScenario.launch(buildAdminCommentsIntent());
        Thread.sleep(3000);

        // Verify the activity loaded successfully and the list is visible
        onView(withId(R.id.rv_admin_comments)).check(matches(isDisplayed()));
    }

    /**
     * Delete button is visible on comments posted by entrants.
     * Admin should be able to delete any comment regardless of who posted it.
     */
    @Test
    public void testAdminDeleteButton_VisibleOnEntrantComment() throws InterruptedException {
        seedComment("some_entrant_id", "SpongeBob Squarepants", TEST_COMMENT_TEXT, false);

        ActivityScenario.launch(buildAdminCommentsIntent());
        Thread.sleep(3000);

        // Verify delete button is visible (isOrganizer = true is always passed for admin)
        onView(withId(R.id.btn_delete_comment)).check(matches(isDisplayed()));
    }

    /**
     * Delete button is visible on comments posted by organizer.
     * Admin should be able to delete any comment regardless of who posted it.
     */
    @Test
    public void testAdminDeleteButton_VisibleOnOrganizerComment() throws InterruptedException {
        seedComment("some_organizer_id", "Fraud Kuna", TEST_COMMENT_TEXT, true);

        ActivityScenario.launch(buildAdminCommentsIntent());
        Thread.sleep(3000);

        // Verify delete button is also visible on organizer comments
        onView(withId(R.id.btn_delete_comment)).check(matches(isDisplayed()));
    }

    /**
     * Admin taps delete.
     */
    @Test
    public void testAdminDeleteComment_ConfirmationDialogAppears() throws InterruptedException {
        seedComment("some_entrant_id", "Gojo SoleException", TEST_COMMENT_TEXT, false);

        ActivityScenario.launch(buildAdminCommentsIntent());
        Thread.sleep(3000);

        // Tap the delete button
        onView(withId(R.id.btn_delete_comment)).perform(click());

        // Verify the confirmation dialog appears with the correct title
        onView(withText("Remove Comment")).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    /**
     * Admin confirms removal.
     * Verifies the comment is deleted from Firestore and no longer shown in the list.
     */
    @Test
    public void testAdminDeleteComment_Success() throws InterruptedException {
        seedComment("some_entrant_id", "Yuta Notafraud", TEST_COMMENT_TEXT, false);

        ActivityScenario.launch(buildAdminCommentsIntent());
        Thread.sleep(3000);

        // Tap delete then confirm
        onView(withId(R.id.btn_delete_comment)).perform(click());
        onView(withText("Remove")).inRoot(isDialog()).perform(click());

        Thread.sleep(5000);

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
        assertEquals("Comment should be deleted from Firestore by admin", 0, count[0]);
    }

    /**
     * Admin cancels removal.
     * Verifies the comment is still present in Firestore after cancelling.
     */
    @Test
    public void testAdminDeleteComment_Cancel() throws InterruptedException {
        seedComment("some_entrant_id", "Higuruma Mygoat", TEST_COMMENT_TEXT, false);

        ActivityScenario.launch(buildAdminCommentsIntent());
        Thread.sleep(3000);

        // Tap delete then cancel
        onView(withId(R.id.btn_delete_comment)).perform(click());
        onView(withText("Cancel")).inRoot(isDialog()).perform(click());

        Thread.sleep(3000);

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
        assertEquals("Comment should still exist after admin cancels removal", 1, count[0]);
    }

}
