package com.eventlottery.data.models;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.eventlottery.model.Waitlist;

import java.util.ArrayList;

public class WaitlistTest {

    private Waitlist waitlist;

    @Before
    public void setUp() {
        waitlist = new Waitlist("test_id");
    }

    @Test
    public void testAddAttendee() {
        waitlist.addAttendee("user1");

        ArrayList<String> attendees = waitlist.getAttendeeIds();
        assertEquals(1, attendees.size());
        assertTrue(attendees.contains("user1"));
        assertEquals(1, waitlist.getWaitlistCount().intValue());
    }

    @Test
    public void testRemoveAttendee() {
        waitlist.addAttendee("user1");
        waitlist.addAttendee("user2");
        waitlist.removeAttendee("user1");

        ArrayList<String> attendees = waitlist.getAttendeeIds();
        assertEquals(1, attendees.size());
        assertFalse(attendees.contains("user1"));
        assertTrue(attendees.contains("user2"));
        assertEquals(1, waitlist.getWaitlistCount().intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNonExistentAttendee() {
        waitlist.removeAttendee("user1");
    }

    @Test
    public void testFindAttendee() {
        waitlist.addAttendee("user1");

        assertTrue(waitlist.findAttendee("user1"));
        assertFalse(waitlist.findAttendee("user2"));
    }

    @Test
    public void testGetWaitlistCount() {
        assertEquals(0, waitlist.getWaitlistCount().intValue());

        waitlist.addAttendee("user1");
        assertEquals(1, waitlist.getWaitlistCount().intValue());

        waitlist.addAttendee("user2");
        assertEquals(2, waitlist.getWaitlistCount().intValue());

        waitlist.removeAttendee("user1");
        assertEquals(1, waitlist.getWaitlistCount().intValue());
    }

    @Test
    public void testWaitlistWithLimit() {
        Waitlist limitedWaitlist = new Waitlist("test_id", 3);

        assertFalse(limitedWaitlist.isWaitlistFull());

        limitedWaitlist.addAttendee("user1");
        limitedWaitlist.addAttendee("user2");
        assertFalse(limitedWaitlist.isWaitlistFull());

        limitedWaitlist.addAttendee("user3");
        assertTrue(limitedWaitlist.isWaitlistFull());
    }

    @Test(expected = IllegalStateException.class)
    public void testAddToFullWaitlist() {
        Waitlist limitedWaitlist = new Waitlist("test_id", 2);

        limitedWaitlist.addAttendee("user1");
        limitedWaitlist.addAttendee("user2");
        limitedWaitlist.addAttendee("user3"); // Should throw exception
    }
}