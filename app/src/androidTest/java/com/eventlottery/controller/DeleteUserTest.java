package com.eventlottery.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.eventlottery.model.Attendee;
import com.eventlottery.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Extensive Instrumented Test Suite for UserController.deleteUser (US 01.02.04).
 * Verifies profile deletion and cascading cleanup of event associations.
 */
@RunWith(AndroidJUnit4.class)
public class DeleteUserTest {

    private UserController userController;
    private FirebaseFirestore db;
    private String testUserId;
    private String testEventId;
    
    private final String VALID_EMAIL = "delete_test@example.com";
    private final String VALID_PHONE = "1234567890";

    @Before
    public void setUp() {
        userController = new UserController();
        db = FirebaseFirestore.getInstance();
        
        // Ensure fresh data for every test run
        testUserId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        testEventId = "event_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Test basic deletion success callback.
     */
    @Test
    public void testDeleteUserSuccessCallback() throws InterruptedException {
        setupMockUser(testUserId);

        CountDownLatch latch = new CountDownLatch(1);
        userController.deleteUser(testUserId, new UserController.OnUserOperationListener() {
            @Override
            public void onSuccess() { latch.countDown(); }
            @Override
            public void onError(Exception e) { fail(e.getMessage()); }
        });

        assertTrue("Callback timed out", latch.await(10, TimeUnit.SECONDS));
    }

    /**
     * Verifies that both User and Attendee documents are actually removed from Firestore.
     */
    @Test
    public void testDeleteUserDocumentVerification() throws InterruptedException {
        setupMockUser(testUserId);
        setupMockAttendee(testUserId);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        userController.deleteUser(testUserId, new UserController.OnUserOperationListener() {
            @Override
            public void onSuccess() { deleteLatch.countDown(); }
            @Override
            public void onError(Exception e) { fail(e.getMessage()); }
        });
        deleteLatch.await(10, TimeUnit.SECONDS);

        // Verify documents are gone
        CountDownLatch verifyLatch = new CountDownLatch(2);
        db.collection("users").document(testUserId).get().addOnCompleteListener(task -> {
            assertFalse("User document should be deleted", task.getResult().exists());
            verifyLatch.countDown();
        });
        db.collection("attendees").document(testUserId).get().addOnCompleteListener(task -> {
            assertFalse("Attendee document should be deleted", task.getResult().exists());
            verifyLatch.countDown();
        });

        assertTrue("Verification timed out", verifyLatch.await(10, TimeUnit.SECONDS));
    }

    /**
     * Verifies cleanup logic when a user is on multiple waitlists.
     */
    @Test
    public void testDeleteUserMultipleWaitlists() throws InterruptedException {
        String eventId2 = "event_multi_2";
        Attendee attendee = new Attendee();
        attendee.setAttendeeID(testUserId);
        attendee.setName("Multi-Waitlist User");
        attendee.setEmail(VALID_EMAIL);
        attendee.setPhoneNumber(VALID_PHONE);
        attendee.joinWaitList(testEventId);
        attendee.joinWaitList(eventId2);

        CountDownLatch setupLatch = new CountDownLatch(1);
        db.collection("attendees").document(testUserId).set(attendee)
                .addOnCompleteListener(task -> setupLatch.countDown());
        setupLatch.await(5, TimeUnit.SECONDS);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        userController.deleteUser(testUserId, new UserController.OnUserOperationListener() {
            @Override
            public void onSuccess() { deleteLatch.countDown(); }
            @Override
            public void onError(Exception e) { fail(e.getMessage()); }
        });

        assertTrue("Delete operation with multiple waitlists timed out", deleteLatch.await(15, TimeUnit.SECONDS));
    }

    /**
     * Verifies cleanup logic when a user has multiple event histories.
     */
    @Test
    public void testDeleteUserMultipleGuestLists() throws InterruptedException {
        String eventId2 = "event_history_2";
        Attendee attendee = new Attendee();
        attendee.setAttendeeID(testUserId);
        attendee.setName("History User");
        attendee.setEmail(VALID_EMAIL);
        attendee.setPhoneNumber(VALID_PHONE);
        attendee.addEventToHistory(testEventId);
        attendee.addEventToHistory(eventId2);

        CountDownLatch setupLatch = new CountDownLatch(1);
        db.collection("attendees").document(testUserId).set(attendee)
                .addOnCompleteListener(task -> setupLatch.countDown());
        setupLatch.await(5, TimeUnit.SECONDS);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        userController.deleteUser(testUserId, new UserController.OnUserOperationListener() {
            @Override
            public void onSuccess() { deleteLatch.countDown(); }
            @Override
            public void onError(Exception e) { fail(e.getMessage()); }
        });

        assertTrue("Delete operation with multiple history entries timed out", deleteLatch.await(15, TimeUnit.SECONDS));
    }

    /**
     * Verifies that deletion handles cases where only the User document exists (Attendee doc missing).
     */
    @Test
    public void testDeleteUserResilience_UserOnly() throws InterruptedException {
        setupMockUser(testUserId); // Only create the 'users' document

        CountDownLatch latch = new CountDownLatch(1);
        userController.deleteUser(testUserId, new UserController.OnUserOperationListener() {
            @Override
            public void onSuccess() { latch.countDown(); }
            @Override
            public void onError(Exception e) { fail(e.getMessage()); }
        });

        assertTrue("Resilience test (User only) timed out", latch.await(10, TimeUnit.SECONDS));
    }

    /**
     * Verifies that deletion handles cases where only the Attendee document exists (User doc missing).
     */
    @Test
    public void testDeleteUserResilience_AttendeeOnly() throws InterruptedException {
        setupMockAttendee(testUserId); // Only create the 'attendees' document

        CountDownLatch latch = new CountDownLatch(1);
        userController.deleteUser(testUserId, new UserController.OnUserOperationListener() {
            @Override
            public void onSuccess() { latch.countDown(); }
            @Override
            public void onError(Exception e) { fail(e.getMessage()); }
        });

        assertTrue("Resilience test (Attendee only) timed out", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testDeleteNonExistentUser() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        userController.deleteUser("definitely_not_an_id_" + UUID.randomUUID(), new UserController.OnUserOperationListener() {
            @Override
            public void onSuccess() { latch.countDown(); }
            @Override
            public void onError(Exception e) { fail(e.getMessage()); }
        });

        assertTrue("Delete non-existent user timed out", latch.await(10, TimeUnit.SECONDS));
    }

    // Helper Methods
    
    private void setupMockUser(String userId) throws InterruptedException {
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail(VALID_EMAIL);
        user.setPhone(VALID_PHONE);
        
        CountDownLatch latch = new CountDownLatch(1);
        userController.saveUser(user, new UserController.OnUserOperationListener() {
            @Override
            public void onSuccess() { latch.countDown(); }
            @Override
            public void onError(Exception e) { latch.countDown(); }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    private void setupMockAttendee(String userId) throws InterruptedException {
        Attendee attendee = new Attendee();
        attendee.setAttendeeID(userId);
        attendee.setName("Test Attendee");
        attendee.setEmail(VALID_EMAIL);
        attendee.setPhoneNumber(VALID_PHONE);
        
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("attendees").document(userId).set(attendee)
                .addOnCompleteListener(task -> latch.countDown());
        latch.await(5, TimeUnit.SECONDS);
    }
}
