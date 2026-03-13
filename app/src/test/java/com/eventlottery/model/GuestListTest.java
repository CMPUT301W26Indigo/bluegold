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
        assertTrue(guestList.getAttendeeIds().isEmpty());
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
        ArrayList<HashMap<String, String>> attendees = guestList.getAttendeeIds();
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
        
        ArrayList<HashMap<String, String>> attendees = guestList.getAttendeeIds();
        HashMap<String, String> attendeeMap = attendees.get(0);
        assertEquals(newStatus, attendeeMap.get(attendeeId));
    }

    @Test
    public void testChangeAttendeeStatus_NonExistent() {
        String attendeeId = "user_1";
        guestList.addGuestAttendee(attendeeId);
        
        // Attempting to change status for a non-existent attendee should do nothing
        guestList.changeAttendeeStatus("non_existent_user", "accepted");
        
        ArrayList<HashMap<String, String>> attendees = guestList.getAttendeeIds();
        HashMap<String, String> attendeeMap = attendees.get(0);
        assertEquals("maybe", attendeeMap.get(attendeeId));
    }
}
