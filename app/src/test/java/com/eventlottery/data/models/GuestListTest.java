package com.eventlottery.data.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.eventlottery.model.GuestList;

import org.junit.Before;
import org.junit.Test;

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

        HashMap<String, String> attendees = guestList.getAttendees();
        assertEquals(1, attendees.size());
        assertEquals(1, guestList.getListCount().intValue());

        // Check default status is "maybe"
        assertEquals("maybe", attendees.get("user1"));
    }

    @Test
    public void testChangeAttendeeStatus() {
        guestList.addGuestAttendee("user1");
        guestList.changeAttendeeStatus("user1", "confirmed");

        HashMap<String, String> attendees = guestList.getAttendees();

        assertEquals("confirmed", attendees.get("user1"));

    }

    @Test
    public void testChangeStatusForNonExistentAttendee() {
        guestList.addGuestAttendee("user1");
        assertThrows(IllegalArgumentException.class, () ->guestList.changeAttendeeStatus("user2", "confirmed"));

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