package com.eventlottery.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class WaitlistTest {

    private Waitlist unlimitedWaitlist;
    private Waitlist limitedWaitlist;
    private final String TEST_EVENT_ID = "event_123";

    @Before
    public void setUp() {
        unlimitedWaitlist = new Waitlist(TEST_EVENT_ID);
        limitedWaitlist = new Waitlist(TEST_EVENT_ID, 1, null);
    }

    @Test
    public void testConstructorWithLimit() {
        assertEquals(TEST_EVENT_ID, limitedWaitlist.getEventId());
        assertEquals(Integer.valueOf(1), limitedWaitlist.getWaitlistLimit());
        assertEquals(0, (int) limitedWaitlist.getWaitlistCount());
        assertTrue(limitedWaitlist.getAttendeeIds().isEmpty());
    }

    @Test
    public void testConstructorWithoutLimit() {
        assertNull(unlimitedWaitlist.getWaitlistLimit());
        assertEquals(TEST_EVENT_ID, unlimitedWaitlist.getEventId());
    }

    @Test
    public void testAddAttendee() {
        String attendeeId = "user_456";
        unlimitedWaitlist.addAttendee(attendeeId);

        assertEquals(1, (int) unlimitedWaitlist.getWaitlistCount());
        assertTrue(unlimitedWaitlist.getAttendeeIds().contains(attendeeId));
        assertTrue(unlimitedWaitlist.findAttendee(attendeeId));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddAttendeeWhenFull() {
        limitedWaitlist.addAttendee("user1");
        limitedWaitlist.addAttendee("user2"); // Should throw exception
    }

    @Test
    public void testRemoveAttendee() {
        String attendeeId = "user_789";
        unlimitedWaitlist.addAttendee(attendeeId);
        assertEquals(1, (int) unlimitedWaitlist.getWaitlistCount());

        unlimitedWaitlist.removeAttendee(attendeeId);
        assertEquals(0, (int) unlimitedWaitlist.getWaitlistCount());
        assertFalse(unlimitedWaitlist.findAttendee(attendeeId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNonExistentAttendee() {
        unlimitedWaitlist.removeAttendee("non_existent_user");
    }

    @Test
    public void testIsWaitlistFull_Unlimited() {
        assertNull(unlimitedWaitlist.getWaitlistLimit());
        assertFalse(unlimitedWaitlist.isWaitlistFull());

        for (int i = 0; i < 100; i++) {
            unlimitedWaitlist.addAttendee("user" + i);
        }
        assertFalse(unlimitedWaitlist.isWaitlistFull());
    }

    @Test
    public void testIsWaitlistFull_Limited() {
        Waitlist limitedWaitlist2 = new Waitlist(TEST_EVENT_ID, 2, null);
        assertFalse(limitedWaitlist.isWaitlistFull());

        limitedWaitlist2.addAttendee("user1");
        assertFalse(limitedWaitlist2.isWaitlistFull());

        limitedWaitlist2.addAttendee("user2");
        assertTrue(limitedWaitlist2.isWaitlistFull());
    }

    // -------- MOVED TESTS FROM FIRST FILE --------

    @Test
    public void testAddAttendee_ListVerification() {
        unlimitedWaitlist.addAttendee("user1");

        ArrayList<String> attendees = unlimitedWaitlist.getAttendeeIds();
        assertEquals(1, attendees.size());
        assertTrue(attendees.contains("user1"));
        assertEquals(1, unlimitedWaitlist.getWaitlistCount().intValue());
    }

    @Test
    public void testRemoveAttendee_ListVerification() {
        unlimitedWaitlist.addAttendee("user1");
        unlimitedWaitlist.addAttendee("user2");
        unlimitedWaitlist.removeAttendee("user1");

        ArrayList<String> attendees = unlimitedWaitlist.getAttendeeIds();
        assertEquals(1, attendees.size());
        assertFalse(attendees.contains("user1"));
        assertTrue(attendees.contains("user2"));
        assertEquals(1, unlimitedWaitlist.getWaitlistCount().intValue());
    }

    @Test
    public void testFindAttendee() {
        unlimitedWaitlist.addAttendee("user1");

        assertTrue(unlimitedWaitlist.findAttendee("user1"));
        assertFalse(unlimitedWaitlist.findAttendee("user2"));
    }

    @Test
    public void testGetWaitlistCount() {
        assertEquals(0, unlimitedWaitlist.getWaitlistCount().intValue());

        unlimitedWaitlist.addAttendee("user1");
        assertEquals(1, unlimitedWaitlist.getWaitlistCount().intValue());

        unlimitedWaitlist.addAttendee("user2");
        assertEquals(2, unlimitedWaitlist.getWaitlistCount().intValue());

        unlimitedWaitlist.removeAttendee("user1");
        assertEquals(1, unlimitedWaitlist.getWaitlistCount().intValue());
    }

    @Test
    public void testWaitlistWithLimit() {
        Waitlist limitedWaitlist3 = new Waitlist("test_id", 3, null);

        assertFalse(limitedWaitlist3.isWaitlistFull());

        limitedWaitlist3.addAttendee("user1");
        limitedWaitlist3.addAttendee("user2");
        assertFalse(limitedWaitlist3.isWaitlistFull());

        limitedWaitlist3.addAttendee("user3");
        assertTrue(limitedWaitlist3.isWaitlistFull());
    }

    @Test(expected = IllegalStateException.class)
    public void testAddToFullWaitlist() {
        Waitlist limitedWaitlist4 = new Waitlist("test_id", 2, null);

        limitedWaitlist4.addAttendee("user1");
        limitedWaitlist4.addAttendee("user2");
        limitedWaitlist4.addAttendee("user3"); // Should throw exception
    }
}
