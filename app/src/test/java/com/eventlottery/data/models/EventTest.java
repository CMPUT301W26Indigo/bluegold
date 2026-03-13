package com.eventlottery.data.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.eventlottery.model.Event;
import com.eventlottery.model.GuestList;
import com.eventlottery.model.Waitlist;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class EventTest {
    private Event event;

    @Before
    public void setUp() {
        event = new Event();
    }

    @Test
    public void testEventCreated() {
        assertNotNull(event);
    }

    @Test
    public void testEventCreationWithAllParameters() {
        String id = "123";
        String name = "Test Event";
        String description = "This is a test event";
        String organizerId = "organizer123";
        String date = "2023-08-15";
        String time = "10:00 AM";
        String location = "Test Location";
        ArrayList<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");
        boolean geolocationEnabled = true;
        Integer geolocationRadius = 10;
        String qrCodeUrl = "https://example.com/qrcode.png";
        boolean recurringEvent = false;
        Integer eventCapacity = 100;
        Integer waitlistLimit = 50;
        String registrationDeadline = "2023-08-20";
        Event newEvent = new Event(id, name, description, organizerId, date, time, location, tags, geolocationEnabled, geolocationRadius, qrCodeUrl, eventCapacity, waitlistLimit, registrationDeadline, recurringEvent);
        assertNotNull(newEvent);
    }

    @Test
    public void testName() {
        event.setName("Test Name");
        assertEquals("Test Name", event.getName());
    }

    @Test
    public void testDescription() {
        event.setDescription("Test Description");
        assertEquals("Test Description", event.getDescription());
    }

    @Test
    public void testOrganizerId() {
        event.setOrganizerId("org-123");
        assertEquals("org-123", event.getOrganizerId());
    }

    @Test
    public void testDate() {
        event.setDate("2024-12-01");
        assertEquals("2024-12-01", event.getDate());
    }

    @Test
    public void testTime() {
        event.setTime("18:00");
        assertEquals("18:00", event.getTime());
    }

    @Test
    public void testLocation() {
        event.setLocation("Test Location");
        assertEquals("Test Location", event.getLocation());
    }

    @Test
    public void testTags() {
        ArrayList<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");
        event.setTags(tags);
        assertEquals(tags, event.getTags());
        assertEquals(2, event.getTags().size());
    }

    @Test
    public void testGeolocationEnabled() {
        event.setGeolocationEnabled(true);
        assertTrue(event.isGeolocationEnabled());
        event.setGeolocationEnabled(false);
        assertFalse(event.isGeolocationEnabled());
    }

    @Test
    public void testGeolocationRadius() {
        event.setGeolocationRadius(100);
        assertEquals(Integer.valueOf(100), event.getGeolocationRadius());
    }

    @Test
    public void testStatus() {
        event.setStatus("closed");
        assertEquals("closed", event.getStatus());
    }

    @Test
    public void testQrCodeUrl() {
        event.setQrCodeUrl("http://example.com/qr");
        assertEquals("http://example.com/qr", event.getQrCodeUrl());
    }

    @Test
    public void testIsFlagged() {
        event.setFlagged(true);
        assertTrue(event.isFlagged());
        event.setFlagged(false);
        assertFalse(event.isFlagged());
    }

    @Test
    public void testWaitlistLimit() {
        Waitlist waitlist = new Waitlist("event-1", 20, "2023-08-20");
        event.setWaitlist(waitlist);
        assertEquals(waitlist, event.getWaitlist());
    }

    @Test
    public void testWaitlistNoLimit() {
        Waitlist waitlist = new Waitlist("event-1", "deadline");
        event.setWaitlist(waitlist);
        assertEquals(waitlist, event.getWaitlist());
    }

    @Test
    public void testGuestList() {
        GuestList guestList = new GuestList("event-1");
        event.setGuestList(guestList);
        assertEquals(guestList, event.getGuestList());
    }

    @Test
    public void testRecurringEvent() {
        event.setRecurringEvent(true);
        assertTrue(event.isRecurringEvent());
        event.setRecurringEvent(false);
        assertFalse(event.isRecurringEvent());
    }

    @Test
    public void testLotterySystemSmallWaitlist() {
        Waitlist waitlist = new Waitlist("event-1", null);
        GuestList guestList = new GuestList("event-1", 10); // Give it a limit so LotterySystem knows how many to pick
        waitlist.addAttendee("attendee-1");
        waitlist.addAttendee("attendee-2");
        event.setWaitlist(waitlist);
        event.setGuestList(guestList);

        event.LotterySystem();

        // Explicitly cast or call intValue() to solve ambiguity between assertEquals(long, long) and assertEquals(Object, Object)
        assertEquals(0, waitlist.getWaitlistCount().intValue());
        assertEquals(2, guestList.getListCount().intValue());
    }

    @Test
    public void testLotterySystemLargeWaitlist() {
        Waitlist waitlist = new Waitlist("event-1", null);
        GuestList guestList = new GuestList("event-1", 10); // Give it a limit so LotterySystem knows how many to pick
        waitlist.addAttendee("attendee-1");
        waitlist.addAttendee("attendee-2");
        waitlist.addAttendee("attendee-3");
        waitlist.addAttendee("attendee-4");
        waitlist.addAttendee("attendee-5");
        waitlist.addAttendee("attendee-6");
        waitlist.addAttendee("attendee-7");
        waitlist.addAttendee("attendee-8");
        waitlist.addAttendee("attendee-9");
        waitlist.addAttendee("attendee-10");
        waitlist.addAttendee("attendee-11");
        waitlist.addAttendee("attendee-12");
        waitlist.addAttendee("attendee-13");
        waitlist.addAttendee("attendee-14");
        waitlist.addAttendee("attendee-15");
        waitlist.addAttendee("attendee-16");
        waitlist.addAttendee("attendee-17");
        waitlist.addAttendee("attendee-18");

        event.setWaitlist(waitlist);
        event.setGuestList(guestList);

        event.LotterySystem();

        assertEquals(8, waitlist.getWaitlistCount().intValue());
        assertEquals(10, guestList.getListCount().intValue());
    }
}
