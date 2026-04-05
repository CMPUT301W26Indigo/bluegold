package com.eventlottery.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;

public class AttendeeTest {

    private Attendee attendee;

    @Before
    public void setUp() {
        attendee = new Attendee();
    }

    @Test
    public void testSetName() {
        attendee.setName("Apex Legends");
        assertEquals("Apex Legends", attendee.getName());
    }

    @Test
    public void testSetValidEmail() {
        attendee.setEmail("test@example.com");
        assertEquals("test@example.com", attendee.getEmail());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidEmail() {
        attendee.setEmail("invalid-email");
    }

    @Test
    public void testSetValidPhoneNumber() {
        attendee.setPhoneNumber("1234567890");
        assertEquals("1234567890", attendee.getPhoneNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidPhoneNumber() {
        attendee.setPhoneNumber("123");
    }

    @Test
    public void testJoinWaitList() {
        attendee.joinWaitList("event123");
        assertTrue(attendee.getWaitListed().contains("event123"));
    }

    @Test
    public void testLeaveWaitList() {
        attendee.joinWaitList("event123");
        attendee.leaveWaitList("event123");
        assertFalse(attendee.getWaitListed().contains("event123"));
    }

    @Test
    public void testAddEventToHistory() {
        attendee.addEventToHistory("event123");
        ArrayList<AttendeeEventHistory> history = attendee.getEventHistory();
        assertEquals(1, history.size());
        assertEquals("event123", history.get(0).getEventID());
    }

    @Test
    public void testInitialNotificationValue() {
        assertTrue(attendee.getNotification());
    }

    @Test
    public void testSetNotification() {
        attendee.setNotification(false);
        assertFalse(attendee.getNotification());
    }

    @Test
    public void testSetAddress() {
        attendee.setAddress("Diagon Alley");
        assertEquals("Diagon Alley", attendee.getAddress());
    }

    @Test
    public void testSetID() {
        attendee.setID("harry123");
        assertEquals("harry123", attendee.getID());
    }
}
