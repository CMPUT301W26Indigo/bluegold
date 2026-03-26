package com.eventlottery.data.models;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.eventlottery.model.GuestList;

import java.util.ArrayList;
import java.util.HashMap;

public class GuestListTest {

    private GuestList guestList;

    @Before
    public void setUp() {
        guestList = new GuestList("test1");
    }

    @Test
    public void testAddGuestAttendee() {
        guestList.addGuestAttendee("user1");

        ArrayList<HashMap<String, String>> attendees = guestList.getAttendees();
        assertEquals(1, attendees.size());
        assertEquals(1, guestList.getListCount().intValue());

        // Check default status is "maybe"
        for (HashMap<String, String> attendee : attendees) {
            if (attendee.containsKey("user1")) {
                assertEquals("maybe", attendee.get("user1"));
            }
        }
    }

    @Test
    public void testChangeAttendeeStatus() {
        guestList.addGuestAttendee("user1");
        guestList.changeAttendeeStatus("user1", "confirmed");

        ArrayList<HashMap<String, String>> attendees = guestList.getAttendees();

        for (HashMap<String, String> attendee : attendees) {
            if (attendee.containsKey("user1")) {
                assertEquals("confirmed", attendee.get("user1"));
            }
        }
    }

    @Test
    public void testChangeStatusForNonExistentAttendee() {
        guestList.addGuestAttendee("user1");
        guestList.changeAttendeeStatus("user2", "confirmed"); // Should do nothing

        ArrayList<HashMap<String, String>> attendees = guestList.getAttendees();

        for (HashMap<String, String> attendee : attendees) {
            if (attendee.containsKey("user1")) {
                assertEquals("maybe", attendee.get("user1"));
            }
        }
    }

    @Test
    public void testGetListCount() {
        assertEquals(0, guestList.getListCount().intValue());

        guestList.addGuestAttendee("user1");
        assertEquals(1, guestList.getListCount().intValue());

        guestList.addGuestAttendee("user2");
        assertEquals(2, guestList.getListCount().intValue());
    }

    @Test
    public void testGuestListWithLimit() {
        GuestList limitedList = new GuestList("test1", 5);
        assertEquals(5, limitedList.getListLimit().intValue());

        limitedList.addGuestAttendee("user1");
        assertEquals(1, limitedList.getListCount().intValue());
    }
}