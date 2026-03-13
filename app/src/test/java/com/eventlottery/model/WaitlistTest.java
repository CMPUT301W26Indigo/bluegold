package com.eventlottery.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

public class WaitlistTest {

    private Waitlist waitlist;
    private final String TEST_EVENT_ID = "event_123";

    @Before
    public void setUp() {
        waitlist = new Waitlist(TEST_EVENT_ID);
    }

    @Test
    public void testConstructorWithLimit() {
        Waitlist limitedWaitlist = new Waitlist(TEST_EVENT_ID, 50);
        assertEquals(TEST_EVENT_ID, limitedWaitlist.getEventId());
        assertEquals(Integer.valueOf(50), limitedWaitlist.getWaitlistLimit());
        assertEquals(0, (int) limitedWaitlist.getWaitlistCount());
        assertTrue(limitedWaitlist.getAttendeeIds().isEmpty());
    }

    @Test
    public void testConstructorWithoutLimit() {
        assertNull(waitlist.getWaitlistLimit());
        assertEquals(TEST_EVENT_ID, waitlist.getEventId());
    }

    @Test
    public void testAddAttendee() {
        String attendeeId = "user_456";
        waitlist.addAttendee(attendeeId);

        assertEquals(1, (int) waitlist.getWaitlistCount());
        assertTrue(waitlist.getAttendeeIds().contains(attendeeId));
        assertTrue(waitlist.findAttendee(attendeeId));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddAttendeeWhenFull() {
        Waitlist limitedWaitlist = new Waitlist(TEST_EVENT_ID, 1);
        limitedWaitlist.addAttendee("user1");
        limitedWaitlist.addAttendee("user2"); // Should throw exception
    }

    @Test
    public void testRemoveAttendee() {
        String attendeeId = "user_789";
        waitlist.addAttendee(attendeeId);
        assertEquals(1, (int) waitlist.getWaitlistCount());

        waitlist.removeAttendee(attendeeId);
        assertEquals(0, (int) waitlist.getWaitlistCount());
        assertFalse(waitlist.findAttendee(attendeeId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNonExistentAttendee() {
        waitlist.removeAttendee("non_existent_user");
    }

    @Test
    public void testIsWaitlistFull_Unlimited() {
        assertNull(waitlist.getWaitlistLimit());
        assertFalse(waitlist.isWaitlistFull());
        
        // Even with many attendees, an unlimited waitlist shouldn't be "full"
        for (int i = 0; i < 100; i++) {
            waitlist.addAttendee("user" + i);
        }
        assertFalse(waitlist.isWaitlistFull());
    }

    @Test
    public void testIsWaitlistFull_Limited() {
        Waitlist limitedWaitlist = new Waitlist(TEST_EVENT_ID, 2);
        assertFalse(limitedWaitlist.isWaitlistFull());
        
        limitedWaitlist.addAttendee("user1");
        assertFalse(limitedWaitlist.isWaitlistFull());
        
        limitedWaitlist.addAttendee("user2");
        assertTrue(limitedWaitlist.isWaitlistFull());
    }
}
