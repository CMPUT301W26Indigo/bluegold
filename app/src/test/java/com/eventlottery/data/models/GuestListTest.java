package com.eventlottery.data.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import com.eventlottery.data.models.GuestList;


public class GuestListTest {

    private GuestList guestList;
    private final String TEST_EVENT_ID = "event_123";

    @Before
    public void setUp() {
        guestList = new GuestList(TEST_EVENT_ID);
    }

    @Test
    public void testConstructorWithLimit() {
        GuestList limitedList = new GuestList(TEST_EVENT_ID, 50);
        assertEquals(TEST_EVENT_ID, limitedList.getEventId());
        assertEquals(Integer.valueOf(50), limitedList.getListLimit());
        assertEquals(0, (int) limitedList.getListCount());
        assertTrue(limitedList.getAttendeeIds().isEmpty());
    }

    @Test
    public void testConstructorWithoutLimit() {
        assertNull(guestList.getListLimit());
        assertEquals(TEST_EVENT_ID, guestList.getEventId());
    }

    @Test
    public void testAddGuestAttendee() {
        String attendeeId = "user_456";
        guestList.addGuestAttendee(attendeeId);

        assertEquals(1, (int) guestList.getListCount());
        ArrayList<HashMap<String, String>> attendees = guestList.getAttendeeIds();
        assertEquals(1, attendees.size());
        
        HashMap<String, String> firstAttendee = attendees.get(0);
        assertTrue(firstAttendee.containsKey(attendeeId));
        assertEquals("maybe", firstAttendee.get(attendeeId));
    }

    @Test
    public void testChangeAttendeeStatus() {
        String attendeeId = "user_789";
        guestList.addGuestAttendee(attendeeId);
        
        // Initial check
        assertEquals("maybe", guestList.getAttendeeIds().get(0).get(attendeeId));
        
        // Update status
        guestList.changeAttendeeStatus(attendeeId, "confirmed");
        
        // Final check
        assertEquals("confirmed", guestList.getAttendeeIds().get(0).get(attendeeId));
    }

    @Test
    public void testChangeAttendeeStatusNonExistent() {
        guestList.addGuestAttendee("user_1");
        
        // Attempting to change status of a user not in the list should do nothing
        guestList.changeAttendeeStatus("user_2", "confirmed");
        
        assertEquals("maybe", guestList.getAttendeeIds().get(0).get("user_1"));
    }
}
