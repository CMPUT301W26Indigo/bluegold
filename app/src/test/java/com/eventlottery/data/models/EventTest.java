package com.eventlottery.data.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import com.eventlottery.model.Event;

public class EventTest {

    private Event event;

    @Before
    public void setUp() {
        event = new Event();
        event.setId("test_id");
        event.setName("Test Event");
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
}
