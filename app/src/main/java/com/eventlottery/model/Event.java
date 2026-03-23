package com.eventlottery.model;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Random;

/**
 * Event data model representing an event
 * <p>
 * This class represents all properties and methods for an Event in the system.
 * It includes geolocation validation, waitlist management, and lottery functionality.
 */
public class Event {
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
    private Bitmap qrCode;
    private boolean isFlagged;
    private Waitlist waitlist;
    private GuestList guestList;
    private boolean recurringEvent;
    private boolean isPrivate;

    public Event() {
        this.id = "";
        this.name = "";
        this.description = "";
        this.organizerId = "";
        this.date = "";
        this.time = "";
        this.location = "";
        this.tags = new ArrayList<>();
        this.geolocationEnabled = false;
        this.geolocationRadius = null;
        this.status = "open";
        this.qrCodeUrl = "eventlottery://event/" + this.getId();
        this.isFlagged = false;
        this.recurringEvent = false;
        this.isPrivate = false;
        this.waitlist = new Waitlist("", null); // Use empty strings for defaults
        this.guestList = new GuestList("");
    }

    /**
     * Constructor with all parameters.
     * Use null for eventCapacity or waitlistLimit if they are not restricted.
     */
    public Event(String id, String name, String description, String organizerId, String date, String time, String location, ArrayList<String> tags, boolean geolocationEnabled, Integer geolocationRadius, String qrCodeUrl, Integer eventCapacity, Integer waitlistLimit, String registrationDeadline, boolean recurringEvent, boolean isPrivate) {
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
        this.recurringEvent = recurringEvent;
        this.isPrivate = isPrivate;


        // Initialize with optional limits
        this.waitlist = (waitlistLimit != null) ? new Waitlist(id, waitlistLimit, registrationDeadline) : new Waitlist(id, registrationDeadline);
        this.guestList = (eventCapacity != null) ? new GuestList(id, eventCapacity) : new GuestList(id);
    }

    // Simplified constructor for basic events
    public Event(String id, String name, String description, String organizerId, String date, String time, String location, ArrayList<String> tags, boolean geolocationEnabled, Integer geolocationRadius, String qrCodeUrl) {
        this(id, name, description, organizerId, date, time, location, tags, geolocationEnabled, geolocationRadius, qrCodeUrl, null, null, null, false, false);
    }

    // Getters and Setters
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }

    public Integer getGeolocationRadius() {
        return geolocationRadius;
    }

    public void setGeolocationRadius(Integer geolocationRadius) {
        this.geolocationRadius = geolocationRadius;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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

    public GuestList getGuestList() {
        return guestList;
    }

    public boolean isRecurringEvent() {
        return recurringEvent;
    }

    public void setRecurringEvent(boolean recurringEvent) {
        this.recurringEvent = recurringEvent;
    }

    public boolean getIsPrivate() { return isPrivate; }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    // Lottery System if not specified a max num of guests to select

    /**
     * Lottery System if not specified a max num of guests to select
     * Randomly selects guests from the waitlist and adds them to the guest list.
     * Sends notifications to the selected guests.
     *
     * @return void
     * <p>
     * public static final Creator CREATOR = new Creator() {
     * Override public Event createFromParcel(Parcel in) {
     * return new Event(in);
     * }
     * Override public Event[] newArray(int size) {
     * return new Event[size];
     * }
     * };
     * <p>
     * /*
     * Generates QR Code once the create button is pressed
     *
     */
    public Bitmap generateQR() {
        // Credit for basis of code: https://youtu.be/n8HdrLYL9DA?si=42nC-Wwzbn5_1wUU
        // TODO: Add this code to the activity/fragment that houses the button that generates events
        // btn_generate = findViewById(R.id.[BUTTON THAT GENERATES EVENT]);
        // btn_generate.SetOnClickListener(v -> {
        //   generateQR();
        // });
        // TODO: Add a spot somewhere that displays QR Codes for events
        // qr_display = findViewById(R.id.[IMAGE THAT WILL HOLD THE QR CODE]);
        // TODO: Test generation using following dummy text
        //String text = "Welcome to the Event Details Page!";

        // TODO: Verify that this URL is correct. If not, change it. May need to change this to work with Firestore
        if (!getIsPrivate()) {
            String deepLink = getQrCodeUrl();

            MultiFormatWriter writer = new MultiFormatWriter();
            try {
                BitMatrix matrix = writer.encode(deepLink,
                        BarcodeFormat.QR_CODE,
                        400,
                        400);

                BarcodeEncoder encoder = new BarcodeEncoder();
                //qr_display.setImageBitmap(bitmap);

                return encoder.createBitmap(matrix);
            } catch (WriterException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    // Business Logic Methods

    /**
     * Check if the event is currently accepting registrations
     */
    public void LotterySystem() {
        Integer limit = guestList.getListLimit();
        int count = (limit != null) ? Math.min(waitlist.getWaitlistCount(), limit) : waitlist.getWaitlistCount();

        while (waitlist.getWaitlistCount() > 0 && count > 0) {
            Random random = new Random();
            int randomIndex = random.nextInt(waitlist.getAttendeeIds().size());
            String attendeeId = waitlist.getAttendeeIds().get(randomIndex);

            guestList.addGuestAttendee(attendeeId);
            waitlist.removeAttendee(attendeeId);
            count--;
            // TODO: Implement notification sending logic
            // Notification notification = new Notification("You have been selected!", attendeeId, id);
            // notification.sendNotification();
        }
    }

    /**
     * Lottery System if a max num of guests to select is specified
     * Randomly selects guests from the waitlist and adds them to the guest list.
     * Sends notifications to the selected guests.
     * @param lotteryLimit
     */
    public void LotterySystem(int lotteryLimit) {
        Integer limit = guestList.getListLimit();
        int tempCount = (limit != null) ? Math.min(waitlist.getWaitlistCount(), limit) : waitlist.getWaitlistCount();
        int count = Math.min(tempCount, lotteryLimit);

        while (waitlist.getWaitlistCount() > 0 && count > 0) {
            Random random = new Random();
            int randomIndex = random.nextInt(waitlist.getAttendeeIds().size());
            String attendeeId = waitlist.getAttendeeIds().get(randomIndex);

            guestList.addGuestAttendee(attendeeId);
            waitlist.removeAttendee(attendeeId);
            count--;
            // TODO: Implement notification sending logic
            // Notification notification = new Notification("You have been selected!", attendeeId, id);
            // notification.sendNotification();
        }
    }

    public void addAttendeeToWaitlist(String attendeeId) {
        waitlist.addAttendee(attendeeId);
    }

    public boolean waitlistIsFull() {
        return waitlist.isWaitlistFull();
    }

    public int getAvailableSpots() {
        if (guestList.getListLimit() == null) return Integer.MAX_VALUE;
        return Math.max(0, guestList.getListLimit() - guestList.getListCount());
    }

    /**
     * Adds a tag to an event
     *
     * @param tag
     */
    public void addTag(String tag) {
        tags.add(tag);
    }

    /**
     * Removes a tag from an event
     *
     * @param tag
     */
    public void removeTag(String tag) {
        tags.remove(tag);
    }

    /**
     * Exports a list of confirmed attendees in CSV format.
     *
     * Names containing commas are automatically wrapped in quotes to maintain formatting
     *
     * @param confirmedAttendees ArrayList of Attendee objects who confirmed attendance
     * @return String containing CSV
     *
     * @see Attendee
     */
    public String exportToCSV(ArrayList<Attendee> confirmedAttendees) {
        StringBuilder csv = new StringBuilder();

        csv.append("Name,Email,Phone,Status\n");

        for (Attendee attendee : confirmedAttendees) {

            // Handle commas in names by wrapping in quotes
            String name = attendee.getName();
            if (name != null && name.contains(",")) {
                name = "\"" + name + "\"";
            }

            csv.append(name != null ? name : "").append(",")
                    .append(attendee.getEmail() != null ? attendee.getEmail() : "").append(",")
                    .append(attendee.getPhoneNumber() != null ? attendee.getPhoneNumber() : "").append(",")
                    .append("Confirmed\n");
        }

        return csv.toString();

    }
    public void addAttendee(){

    }
    public void removeUser() {

    }

    /**
     * Sets the guest list for an event.
     *
     * @param guestList the GuestList object containing attendee status
     */
    public void setGuestList(GuestList guestList) {
        this.guestList = guestList;
    }


    /**
     * Sets the waitlist for an event.
     *
     * @param waitlist the Waitlist object containing interested attendees
     */
    public void setWaitlist(Waitlist waitlist) {
        this.waitlist = waitlist;
    }


}
