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
        ArrayList<HashMap<String, String>> attendees = guestList.getAttendees();
        assertEquals(1, attendees.size());
        
        HashMap<String, String> attendeeMap = attendees.get(0);
        assertTrue(attendeeMap.containsKey(attendeeId));
        assertEquals("maybe", attendeeMap.get(attendeeId));
    }

    @Test
    public void testChangeAttendeeStatus() {
        String attendeeId = "user_789";
        guestList.addGuestAttendee(attendeeId);
        
        String newStatus = "accepted";
        guestList.changeAttendeeStatus(attendeeId, newStatus);
        
        ArrayList<HashMap<String, String>> attendees = guestList.getAttendees();
        HashMap<String, String> attendeeMap = attendees.get(0);
        assertEquals(newStatus, attendeeMap.get(attendeeId));
    }

    @Test
    public void testChangeAttendeeStatus_NonExistent() {
        String attendeeId = "user_1";
        guestList.addGuestAttendee(attendeeId);
        
        // Attempting to change status for a non-existent attendee should do nothing
        guestList.changeAttendeeStatus("non_existent_user", "accepted");
        
        ArrayList<HashMap<String, String>> attendees = guestList.getAttendees();
        HashMap<String, String> attendeeMap = attendees.get(0);
        assertEquals("maybe", attendeeMap.get(attendeeId));
    }
    @Test
    public void testCancelEntrants_ChangesMaybeAndDeclined() {
        String user1 = "user1";
        String user2 = "user2";

        guestList.addGuestAttendee(user1); // default "maybe"
        guestList.addGuestAttendee(user2);

        guestList.changeAttendeeStatus(user2, "declined");

        guestList.cancelEntrants();

        ArrayList<HashMap<String, String>> attendees = guestList.getAttendees();

        assertEquals("cancelled", attendees.get(0).get(user1));
        assertEquals("cancelled", attendees.get(1).get(user2));
    }

    @Test
    public void testCancelEntrants_DoesNotChangeAccepted() {
        String user1 = "user1";

        guestList.addGuestAttendee(user1);
        guestList.changeAttendeeStatus(user1, "accepted");

        guestList.cancelEntrants();

        ArrayList<HashMap<String, String>> attendees = guestList.getAttendees();

        assertEquals("accepted", attendees.get(0).get(user1));
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

        ArrayList<HashMap<String, String>> attendees = guestList.getAttendees();

        assertEquals("cancelled", attendees.get(0).get(user1));
        assertEquals("cancelled", attendees.get(1).get(user2));
        assertEquals("accepted", attendees.get(2).get(user3));
    }

}
