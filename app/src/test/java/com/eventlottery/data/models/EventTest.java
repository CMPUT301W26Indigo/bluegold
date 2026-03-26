package com.eventlottery.data.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;

import com.eventlottery.model.Attendee;
import com.eventlottery.model.Event;
import com.eventlottery.model.GuestList;
import com.eventlottery.model.Waitlist;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class EventTest {
    private Event event;
    private GuestList guestList;
    private Waitlist waitlist;

    @Before
    public void setUp() {
        event = new Event();
        event.setId("test_id");
        event.setName("Test Event");

        guestList = new GuestList("test_id");
        event.setGuestList(guestList);

        waitlist = new Waitlist("test_id");
        event.setWaitlist(waitlist);
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
        String endTime = "12:00 PM";
        String location = "Test Location";
        String locationAddress = "123 Test St";
        int capacity = 100;
        Integer waitlistLimit = 50;
        int waitlistCount = 0;
        int confirmedCount = 0;
        ArrayList<String> tags = new ArrayList<>();
        tags.add("tag1");
        String posterImageUrl = "https://example.com/poster.png";
        boolean geolocationEnabled = true;
        Integer geolocationRadius = 10;
        double price = 10.0;
        String status = "open";
        long registrationOpens = 1000L;
        long registrationCloses = 2000L;
        Long lotteryDrawDate = 3000L;
        String qrCodeUrl = "https://example.com/qrcode.png";
        Bitmap qrCode = null;
        long createdAt = 4000L;
        long updatedAt = 5000L;
        boolean isFlagged = false;
        int flagCount = 0;
        boolean recurringEvent = false;
        boolean isPrivate = false;

        Event newEvent = new Event(id, name, description, organizerId, date, time, endTime, location, locationAddress, capacity, waitlistLimit, waitlistCount, confirmedCount, posterImageUrl, price, registrationOpens, registrationCloses, lotteryDrawDate, createdAt, updatedAt, tags, geolocationEnabled, geolocationRadius, status, qrCodeUrl, qrCode, isFlagged, flagCount, waitlist, guestList, recurringEvent, isPrivate);

        assertNotNull(newEvent);
        assertEquals(id, newEvent.getId());
        assertEquals(name, newEvent.getName());
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
        Waitlist waitlist = new Waitlist("event-1");
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
        Waitlist waitlist = new Waitlist("event-1");
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
        Waitlist waitlist = new Waitlist("event-1");
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

    @Test
    public void testLotterySystemWithSpecifiedMax() {
        Waitlist waitlist = new Waitlist("event-1");
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

        event.LotterySystem(4);

        assertEquals(14, waitlist.getWaitlistCount().intValue());
        assertEquals(4, guestList.getListCount().intValue());
    }

    @Test
    public void testRandomLotterySelection() {
        // To test randomness, we run two identical setups and compare results.
        // With 100 people and 10 spots, the chance of getting the same selection is extremely low.
        int waitlistSize = 100;
        int capacity = 10;

        ArrayList<String> selection1 = runLotteryAndGetSelection(waitlistSize, capacity);
        ArrayList<String> selection2 = runLotteryAndGetSelection(waitlistSize, capacity);

        // Verify that the two runs didn't produce the exact same list of attendees in the same order
        assertNotEquals("Lottery should produce different results across different runs", selection1, selection2);
    }

    /**
     * Helper method to set up an event, fill its waitlist, and run the lottery.
     */
    private ArrayList<String> runLotteryAndGetSelection(int waitlistSize, int capacity) {
        Event testEvent = new Event();
        Waitlist waitlist = new Waitlist("event-test");
        GuestList guestList = new GuestList("event-test", capacity);

        for (int i = 0; i < waitlistSize; i++) {
            waitlist.addAttendee("attendee-" + i);
        }

        testEvent.setWaitlist(waitlist);
        testEvent.setGuestList(guestList);
        testEvent.LotterySystem();

        ArrayList<String> selection = new ArrayList<>();
        // Extract attendee IDs from the GuestList (which stores them in HashMaps)
        for (HashMap<String, String> guestMap : guestList.getAttendeeIds()) {
            selection.addAll(guestMap.keySet());
        }
        return selection;
    }

    // Test for viewing list of invited entrants
    @Test
    public void testGuestListHasInvitedAttendees() {
        guestList.addGuestAttendee("John");
        guestList.addGuestAttendee("Fortnite");
        guestList.addGuestAttendee("LeagueofLegends");

        guestList.changeAttendeeStatus("John", "invited");
        guestList.changeAttendeeStatus("Fortnite", "invited");
        guestList.changeAttendeeStatus("LeagueofLegends", "confirmed");

        ArrayList<HashMap<String, String>> attendeeIds = guestList.getAttendeeIds();

        // Count how many have "invited" status
        int invitedCount = 0;
        for (HashMap<String, String> attendee : attendeeIds) {
            for (String status : attendee.values()) {
                if ("invited".equals(status)) {
                    invitedCount++;
                }
            }
        }

        assertEquals(2, invitedCount);
    }

    // Test for CSV export
    @Test
    public void testExportToCSV() {
        ArrayList<Attendee> attendees = new ArrayList<Attendee>();

        Attendee a1 = new Attendee();
        a1.setName("Fort Nite");
        a1.setEmail("epic@gmail.com");
        a1.setPhoneNumber("1234567890");
        attendees.add(a1);

        Attendee a2 = new Attendee();
        a2.setName("Sauce Awesome");
        a2.setEmail("awesome@gmail.com");
        a2.setPhoneNumber("0987654321");
        attendees.add(a2);

        String csv = event.exportToCSV(attendees);

        assertTrue(csv.contains("Fort Nite,epic@gmail.com,1234567890,Confirmed"));
        assertTrue(csv.contains("Sauce Awesome,awesome@gmail.com,0987654321,Confirmed"));
    }

    // Test for Waitlist count
    @Test
    public void testWaitlistCount() {
        assertEquals(0, waitlist.getWaitlistCount().intValue());

        waitlist.addAttendee("Gurt");
        waitlist.addAttendee("Yo");

        assertEquals(2, waitlist.getWaitlistCount().intValue());
    }

    // Test for Confirmed entrants
    @Test
    public void testGuestListHasConfirmedAttendees() {
        guestList.addGuestAttendee("Yo");
        guestList.addGuestAttendee("Gurt");
        guestList.addGuestAttendee("SixSeven");

        guestList.changeAttendeeStatus("Yo", "confirmed");
        guestList.changeAttendeeStatus("Gurt", "invited");
        guestList.changeAttendeeStatus("SixSeven", "confirmed");

        ArrayList<HashMap<String, String>> attendeeIds = guestList.getAttendeeIds();

        // Count how many have "confirmed" status
        int confirmedCount = 0;
        for (HashMap<String, String> attendee : attendeeIds) {
            for (String status : attendee.values()) {
                if ("confirmed".equals(status)) {
                    confirmedCount++;
                }
            }
        }

        assertEquals(2, confirmedCount);
    }

    // Test for Joinable events (registration open check)
    @Test
    public void testIsRegistrationOpen() {
        long now = System.currentTimeMillis();
        long yesterday = now - 86400000; // Milliseconds in a day
        long tomorrow = now + 86400000;

        event.setStatus("open");
        event.setRegistrationOpens(yesterday);
        event.setRegistrationCloses(tomorrow);

        assertTrue(event.isRegistrationOpen());
    }

    //NOTE: Commented out as we are still working on getting geolocation services running.
    // Test to see if geolocation in within bounds
//    @Test
//    public void testGeolocationWithinRadius() {
//        event.setGeolocationEnabled(true);
//        event.setGeolocationLat(20.0);
//        event.setGeolocationLng(-40.0);
//        event.setGeolocationRadius(10);
//
//        assertTrue(event.isWithinGeolocationRadius(20.0, -40.0));
//        assertFalse(event.isWithinGeolocationRadius(70.0, -100.0));
//    }


}
