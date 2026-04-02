package com.eventlottery.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class EventOrganizerTest {
    private EventOrganizer organizer;

    @Before
    public void setUp() {
        organizer = new EventOrganizer();
    }

    @Test
    public void testCreateEventBlank() {
        Event event = organizer.createEventBlank();
        assertNotNull(event);
        // findEvent should find it even if ID is not set yet (if Event assigns one)
        // Note: Blank Event might not have an ID immediately, checking behavior
        assertNotNull(organizer.findEvent(event.getId()));
    }

    @Test
    public void testAddAndFindEvent() {
        Event event = new Event();
        event.setId("test-id");
        organizer.addEvent(event);

        Event found = organizer.findEvent("test-id");
        assertEquals(event, found);
    }

    @Test
    public void testFindEvent_NotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            organizer.findEvent("non-existent");
        });
    }

    @Test
    public void testLotteryWithoutLimit() {
        Event event = new Event();
        event.setId("lottery-event");
        Waitlist waitlist = new Waitlist("lottery-event");
        waitlist.addAttendee("user1");
        waitlist.addAttendee("user2");
        
        GuestList guestList = new GuestList("lottery-event", 10);
        
        event.setWaitlist(waitlist);
        event.setGuestList(guestList);
        
        organizer.addEvent(event);
        
        organizer.lotteryWithoutLimit("lottery-event");
        
        // After lottery, people move from waitlist to guestlist
        assertEquals(0, waitlist.getWaitlistCount().intValue());
        assertEquals(2, guestList.getListCount().intValue());
    }

    @Test
    public void testLotteryWithLimit() {
        Event event = new Event();
        event.setId("lottery-limit-event");
        Waitlist waitlist = new Waitlist("lottery-limit-event");
        waitlist.addAttendee("user1");
        waitlist.addAttendee("user2");
        waitlist.addAttendee("user3");

        GuestList guestList = new GuestList("lottery-limit-event", 10);

        event.setWaitlist(waitlist);
        event.setGuestList(guestList);

        organizer.addEvent(event);

        // Limit to 2 people
        organizer.lotteryWithLimit("lottery-limit-event", 2);

        assertEquals(1, waitlist.getWaitlistCount().intValue());
        assertEquals(2, guestList.getListCount().intValue());
    }

    @Test
    public void testRemoveInactiveAttendees() {
        Event event = new Event();
        event.setId("inactive-event");
        GuestList guestList = new GuestList("inactive-event");
        guestList.addGuestAttendee("user1"); // status "maybe"
        guestList.addGuestAttendee("user2"); // status "maybe"
        
        event.setGuestList(guestList);
        organizer.addEvent(event);
        
        organizer.removeInactiveAttendees("inactive-event");
        
        // Inactive attendees (maybe/declined) should be cancelled
        assertEquals("cancelled", guestList.findAttendee("user1"));
        assertEquals("cancelled", guestList.findAttendee("user2"));
    }

    @Test
    public void testCreateEventWithParameters() {
        String id = "event1";
        String name = "Test Event";
        Waitlist waitlist = new Waitlist(id);
        GuestList guestList = new GuestList(id);
        
        Event event = organizer.createEvent(
                id, name, "description", "org123", "2024-12-01", "10:00", "12:00",
                "Campus", "123 Street", 100, 50, 0, 0, "poster_url", 0.0,
                0L, 0L, 0L, 0L, 0L, new ArrayList<>(), false, 0,
                "open", "qr_url", null, false, 0, waitlist, guestList, false, false
        );
        
        assertNotNull(event);
        assertEquals(id, event.getId());
        assertEquals(name, event.getName());
        assertEquals(event, organizer.findEvent(id));
    }
}
