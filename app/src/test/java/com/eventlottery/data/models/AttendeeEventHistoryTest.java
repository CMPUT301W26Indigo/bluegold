package com.eventlottery.data.models;

import org.junit.Test;
import static org.junit.Assert.*;

import com.eventlottery.model.AttendeeEventHistory;


public class AttendeeEventHistoryTest {

    @Test
    public void testConstructor() {
        String eventId = "test_event_123";
        AttendeeEventHistory history = new AttendeeEventHistory(eventId);
        
        assertEquals("Event ID should match constructor argument", eventId, history.getEventID());
        assertFalse("Attendance should be false by default", history.isAttended());
    }

    @Test
    public void testUpdateAttendance() {
        AttendeeEventHistory history = new AttendeeEventHistory("test_event");
        
        assertFalse("Initial attendance should be false", history.isAttended());
        
        history.updateAttendance();
        
        assertTrue("Attendance should be true after update", history.isAttended());
    }
}
