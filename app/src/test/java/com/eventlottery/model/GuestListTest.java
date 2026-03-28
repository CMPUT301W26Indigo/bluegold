package com.eventlottery.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

public class GuestListTest {

    private GuestList guestList;
    private final String TEST_EVENT_ID = "event_123";
    private final Integer TEST_LIMIT = 50;

    @Before
    public void setUp() {
        guestList = new GuestList(TEST_EVENT_ID, TEST_LIMIT);
    }

    @Test
    public void testConstructorWithLimit() {
        assertEquals(TEST_EVENT_ID, guestList.getEventId());
        assertEquals(TEST_LIMIT, guestList.getListLimit());
        assertEquals(Integer.valueOf(0), guestList.getListCount());
        assertTrue(guestList.getAttendees().isEmpty());
    }

    @Test
    public void testConstructorWithoutLimit() {
        GuestList unlimitedList = new GuestList(TEST_EVENT_ID);
        assertEquals(TEST_EVENT_ID, unlimitedList.getEventId());
        assertNull(unlimitedList.getListLimit());
        assertEquals(Integer.valueOf(0), unlimitedList.getListCount());
    }

    @Test
    public void testAddGuestAttendee() {
        String attendeeId = "user_456";
        guestList.addGuestAttendee(attendeeId);

        assertEquals(Integer.valueOf(1), guestList.getListCount());
        HashMap<String, String> attendees = guestList.getAttendees();
        assertEquals(1, attendees.size());
        
        assertTrue(attendees.containsKey(attendeeId));
        assertEquals("maybe", attendees.get(attendeeId));
    }

    @Test
    public void testChangeAttendeeStatus() {
        String attendeeId = "user_789";
        guestList.addGuestAttendee(attendeeId);
        
        String newStatus = "accepted";
        guestList.changeAttendeeStatus(attendeeId, newStatus);
        
        HashMap<String, String> attendees = guestList.getAttendees();
        assertEquals(newStatus, attendees.get(attendeeId));
    }

    @Test
    public void testChangeAttendeeStatus_NonExistent() {
        String attendeeId = "user_1";
        guestList.addGuestAttendee(attendeeId);
        
        // Attempting to change status for a non-existent attendee should throw IllegalArgumentException
        try {
            guestList.changeAttendeeStatus("non_existent_user", "accepted");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        HashMap<String, String> attendees = guestList.getAttendees();
        assertEquals("maybe", attendees.get(attendeeId));
    }
    @Test
    public void testCancelEntrants_ChangesMaybeAndDeclined() {
        String user1 = "user1";
        String user2 = "user2";

        guestList.addGuestAttendee(user1); // default "maybe"
        guestList.addGuestAttendee(user2);

        guestList.changeAttendeeStatus(user2, "declined");

        guestList.cancelEntrants();

        HashMap<String, String> attendees = guestList.getAttendees();

        assertEquals("cancelled", attendees.get(user1));
        assertEquals("cancelled", attendees.get(user2));
    }

    @Test
    public void testCancelEntrants_DoesNotChangeAccepted() {
        String user1 = "user1";

        guestList.addGuestAttendee(user1);
        guestList.changeAttendeeStatus(user1, "accepted");

        guestList.cancelEntrants();

        HashMap<String, String> attendees = guestList.getAttendees();

        assertEquals("accepted", attendees.get(user1));
    }

    @Test
    public void testCancelEntrants_MixedStatuses() {
        String user1 = "user1";
        String user2 = "user2";
        String user3 = "user3";

        guestList.addGuestAttendee(user1); // maybe
        guestList.addGuestAttendee(user2);
        guestList.addGuestAttendee(user3);

        guestList.changeAttendeeStatus(user2, "declined");
        guestList.changeAttendeeStatus(user3, "accepted");

        guestList.cancelEntrants();

        HashMap<String, String> attendees = guestList.getAttendees();

        assertEquals("cancelled", attendees.get(user1));
        assertEquals("cancelled", attendees.get(user2));
        assertEquals("accepted", attendees.get(user3));
    }

}
