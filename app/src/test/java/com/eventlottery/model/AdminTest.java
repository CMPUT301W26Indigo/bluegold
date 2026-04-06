package com.eventlottery.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the Admin class.
 * Tests property management, object creation, and synchronization between roles.
 */
public class AdminTest {
    private Admin admin;

    @Before
    public void setUp() {
        admin = new Admin();
        admin.setID("test-device-id");
        admin.setName("Test Admin");
        admin.setEmail("admin@example.com");
        admin.setPhoneNumber("7801234567");
        admin.setAddress("123 Test St");
    }

    /**
     * Tests that an Admin is correctly initialized with default values.
     */
    @Test
    public void testAdminInitialization() {
        assertTrue("User should be an admin", admin.isAdmin());
        assertFalse("New admin should not be an attendee yet", admin.isAttendee());
        assertFalse("New admin should not be an event organizer yet", admin.isEventOrganizer());
        assertEquals("Test Admin", admin.getName());
        assertEquals("admin@example.com", admin.getEmail());
    }

    /**
     * Tests the creation of an Attendee role within the Admin object.
     */
    @Test
    public void testCreateAttendee() {
        Attendee attendee = admin.createAttendee();
        
        assertNotNull("Attendee should be created", attendee);
        assertTrue("Admin should now have attendee role", admin.isAttendee());
        assertEquals("Attendee name should match admin", admin.getName(), attendee.getName());
        assertEquals("Attendee email should match admin", admin.getEmail(), attendee.getEmail());
        assertEquals("Attendee ID should match admin", admin.getID(), attendee.getID());
    }

    /**
     * Tests the creation of an EventOrganizer role within the Admin object.
     */
    @Test
    public void testCreateEventOrganizer() {
        EventOrganizer organizer = admin.createEventOrganizer();
        
        assertNotNull("Organizer should be created", organizer);
        assertTrue("Admin should now have organizer role", admin.isEventOrganizer());
        assertEquals("Organizer name should match admin", admin.getName(), organizer.getName());
        assertEquals("Organizer ID should match admin", admin.getID(), organizer.getID());
    }

    /**
     * Tests that updating Admin properties correctly synchronizes with nested roles.
     */
    @Test
    public void testPropertySynchronization() {
        // Initialize roles
        admin.createAttendee();
        admin.createEventOrganizer();
        
        // Update Admin properties
        admin.setName("Updated Name");
        admin.setEmail("updated@example.com");
        admin.setPhoneNumber("7809876543");
        admin.setAddress("456 New Ave");

        // Verify Attendee sync
        assertEquals("Attendee name should sync", "Updated Name", admin.getAttendee().getName());
        assertEquals("Attendee email should sync", "updated@example.com", admin.getAttendee().getEmail());
        assertEquals("Attendee phone should sync", "7809876543", admin.getAttendee().getPhoneNumber());
        assertEquals("Attendee address should sync", "456 New Ave", admin.getAttendee().getAddress());

        // Verify Organizer sync
        assertEquals("Organizer name should sync", "Updated Name", admin.getEventOrganizer().getName());
        assertEquals("Organizer email should sync", "updated@example.com", admin.getEventOrganizer().getEmail());
    }

    /**
     * Tests that Admin correctly handles notification preference synchronization.
     */
    @Test
    public void testNotificationSync() {
        admin.createAttendee();
        
        admin.setNotification(false);
        assertFalse("Attendee notification should sync", admin.getAttendee().getNotification());
    }
}
