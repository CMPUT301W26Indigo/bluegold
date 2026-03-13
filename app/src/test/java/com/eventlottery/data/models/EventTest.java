package com.eventlottery.data.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.eventlottery.model.Attendee;
import com.eventlottery.model.Event;
import com.eventlottery.model.GuestList;
import com.eventlottery.model.Waitlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public void testIsWaitlistFull_Unlimited() {
        event.setWaitlistLimit(null);
        event.setWaitlistCount(1000);
        assertFalse("Waitlist should not be full when limit is null", event.isWaitlistFull());
    }

    @Test
    public void testIsWaitlistFull_Limited() {
        event.setWaitlistLimit(10);
        event.setWaitlistCount(9);
        assertFalse("Waitlist should not be full yet", event.isWaitlistFull());
        
        event.setWaitlistCount(10);
        assertTrue("Waitlist should be full", event.isWaitlistFull());
    }

    @Test
    public void testIsRegistrationOpen_Success() {
        long now = System.currentTimeMillis();
        event.setStatus("open");
        event.setRegistrationOpens(now - 1000);
        event.setRegistrationCloses(now + 1000);
        event.setWaitlistLimit(null);
        
        assertTrue("Registration should be open", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_ClosedStatus() {
        long now = System.currentTimeMillis();
        event.setStatus("closed");
        event.setRegistrationOpens(now - 1000);
        event.setRegistrationCloses(now + 1000);
        
        assertFalse("Registration should be closed if status is not 'open'", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_TimeRestricted() {
        long now = System.currentTimeMillis();
        event.setStatus("open");
        
        // Future opening
        event.setRegistrationOpens(now + 1000);
        event.setRegistrationCloses(now + 2000);
        assertFalse("Registration should not be open yet", event.isRegistrationOpen());
        
        // Past closing
        event.setRegistrationOpens(now - 2000);
        event.setRegistrationCloses(now - 1000);
        assertFalse("Registration should be closed already", event.isRegistrationOpen());
    }

    @Test
    public void testGetAvailableSpots() {
        event.setCapacity(100);
        event.setConfirmedCount(20);
        assertEquals(80, event.getAvailableSpots());
    }

    @Test
    public void testGetFormattedPrice() {
        event.setPrice(0.0);
        assertEquals("Free", event.getFormattedPrice());
        
        event.setPrice(19.99);
        assertEquals("$19.99", event.getFormattedPrice());
    }

    @Test
    public void testIsWithinGeolocationRadius_Disabled() {
        event.setGeolocationEnabled(false);
        // Should return true if disabled, regardless of coordinates
        assertTrue(event.isWithinGeolocationRadius(0, 0));
    }

    @Test
    public void testIsWithinGeolocationRadius_Success() {
        event.setGeolocationEnabled(true);
        event.setGeolocationLat(53.5461); // Edmonton
        event.setGeolocationLng(-113.4938);
        event.setGeolocationRadius(10); // 10km radius
        
        // Point in Edmonton (very close)
        assertTrue("Should be within radius", event.isWithinGeolocationRadius(53.5444, -113.4909));
        
        // Point in Calgary (far away)
        assertFalse("Should be outside radius", event.isWithinGeolocationRadius(51.0447, -114.0719));
    }

    @Test
    public void testConstructorAndGetters() {
        List<String> tags = new ArrayList<>();
        tags.add("fun");
        
        Event fullEvent = new Event("id1", "Name", "Desc", "Org1", "Date", "Time", "EndTime", 
                                  "Loc", "Addr", 100, 50, 10, 5, tags, "url", true, 
                                  20, 1.0, 2.0, 10.5, "open", 100L, 200L, 300L, 
                                  "qr", 1000L, 2000L, false, 0);
        
        assertEquals("id1", fullEvent.getId());
        assertEquals("Name", fullEvent.getName());
        assertEquals(50, (int)fullEvent.getWaitlistLimit());
        assertTrue(fullEvent.isGeolocationEnabled());
        assertEquals(10.5, fullEvent.getPrice(), 0.001);
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
        long yesterday = now - 86400000; // Millisepconds in a day
        long tomorrow = now + 86400000;

        event.setStatus("open");
        event.setRegistrationOpens(yesterday);
        event.setRegistrationCloses(tomorrow);

        assertTrue(event.isRegistrationOpen());
    }

    // Test to see if geolocation in within bounds
    @Test
    public void testGeolocationWithinRadius() {
        event.setGeolocationEnabled(true);
        event.setGeolocationLat(20.0);
        event.setGeolocationLng(-40.0);
        event.setGeolocationRadius(10);

        assertTrue(event.isWithinGeolocationRadius(20.0, -40.0));
        assertFalse(event.isWithinGeolocationRadius(70.0, -100.0));
    }


}
