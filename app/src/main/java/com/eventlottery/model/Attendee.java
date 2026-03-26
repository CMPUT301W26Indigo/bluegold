package com.eventlottery.model;

import android.content.Context;
import android.provider.Settings;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;

/**
 * Represents an Attendee in the Event Lottery System.
 * Stores personal information, event history, and waitlist status.
 */
public class Attendee extends AbstractUser {
    private ArrayList<AttendeeEventHistory> eventHistory;
    private ArrayList<String> waitListed;
    private boolean notification;

    /**
     * Constructs a new Attendee with default values.
     * Initializes empty lists for event history and waitlists.
     */
    public Attendee() {
        super();
        this.notification = true;
        this.eventHistory = new ArrayList<AttendeeEventHistory>();
        this.waitListed = new ArrayList<String>();
    }

    /**
     * Adds an event to the attendee's personal waitlist.
     * @param eventID The unique identifier of the event.
     */
    public void joinWaitList(String eventID) {
        waitListed.add(eventID);
    }

    /**
     * Adds an event to the attendee's history of participated events.
     * @param eventID The unique identifier of the event.
     */
    public void addEventToHistory(String eventID) {
        AttendeeEventHistory event = new AttendeeEventHistory(eventID);
        eventHistory.add(event);
    }

    /**
     * Removes an event from the attendee's waitlist.
     * @param eventID The unique identifier of the event.
     */
    public void leaveWaitList(String eventID) {
        waitListed.remove(eventID);
    }

    /**
     * Gets the list of events the attendee has a history with.
     * @return An ArrayList of AttendeeEventHistory objects.
     */
    public ArrayList<AttendeeEventHistory> getEventHistory() {
        return eventHistory;
    }

    /**
     * Gets the list of event IDs the attendee is currently waitlisted for.
     * @return An ArrayList of event ID strings.
     */
    public ArrayList<String> getWaitListed() {
        return waitListed;
    }

    /**
     * Sets the notification preference for the attendee.
     * @param notification True to enable notifications, false to disable.
     */
    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    /**
     * Gets the notification preference for the attendee.
     * @return True if notifications are enabled, false otherwise.
     */
    public boolean getNotification() {
        return notification;
    }

}
