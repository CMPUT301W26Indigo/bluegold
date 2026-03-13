package com.eventlottery.model;

import java.util.ArrayList;

/**
 * Event data model representing an event in the lottery system
 *
 * This class represents all properties and methods for an Event in the system.
 * It includes geolocation validation, waitlist management, and lottery functionality.
 */
public class blueGoldEvent {
    private String id;
    private String name;
    private String description;
    private String organizerId;
    private String date;
    private String time;
    private String location;
    private ArrayList<String> tags;
    private boolean geolocationEnabled;
    private Integer geolocationRadius; // Nullable - in kilometers (1-500)
    private String status; // "open", "closed", "lottery_drawn", "completed"
    private String qrCodeUrl;
    private boolean isFlagged;
    private Waitlist waitlist;
    private GuestList guestList;

    /**
     * Constructor with all parameters.
     * Use null for eventCapacity or waitlistLimit if they are not restricted.
     */
    public blueGoldEvent(String id, String name, String description, String organizerId, String date, String time, String location, ArrayList<String> tags, boolean geolocationEnabled, Integer geolocationRadius, String qrCodeUrl, Integer eventCapacity, Integer waitlistLimit) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.organizerId = organizerId;
        this.date = date;
        this.time = time;
        this.location = location;
        this.tags = tags;
        this.geolocationEnabled = geolocationEnabled;
        this.geolocationRadius = geolocationRadius;
        this.status = "open";
        this.qrCodeUrl = qrCodeUrl;
        this.isFlagged = false;

        // Initialize with optional limits
        this.waitlist = (waitlistLimit != null) ? new Waitlist(id, waitlistLimit) : new Waitlist(id);
        this.guestList = (eventCapacity != null) ? new GuestList(id, eventCapacity) : new GuestList(id);
    }

    // Simplified constructor for basic events
    public blueGoldEvent(String id, String name, String description, String organizerId, String date, String time, String location, ArrayList<String> tags, boolean geolocationEnabled, Integer geolocationRadius, String qrCodeUrl) {
        this(id, name, description, organizerId, date, time, location, tags, geolocationEnabled, geolocationRadius, qrCodeUrl, null, null);
    }

    // Getters and Setters

//    public String getId() {
//        return id;
//    }

//    public void setId(String id) {
//        this.id = id;
//    }

    public String getName() {
        return name;
    }

//    public void setName(String name) {
//        this.name = name;
//    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

//    public boolean isGeolocationEnabled() {
//        return geolocationEnabled;
//    }

//    public void setGeolocationEnabled(boolean geolocationEnabled) {
//        this.geolocationEnabled = geolocationEnabled;
//    }

    public Integer getGeolocationRadius() {
        return geolocationRadius;
    }

//    public void setGeolocationRadius(Integer geolocationRadius) {
//        this.geolocationRadius = geolocationRadius;
//    }

    public String getStatus() {
        return status;
    }

//    public void setStatus(String status) {
//        this.status = status;
//    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public Waitlist getWaitlist() {
        return waitlist;
    }

    public void setWaitlist(Waitlist waitlist) {
        this.waitlist = waitlist;
    }

    public GuestList getGuestList() {
        return guestList;
    }

    public void setGuestList(GuestList guestList) {
        this.guestList = guestList;
    }
}
