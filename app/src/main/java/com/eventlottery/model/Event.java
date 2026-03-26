package com.eventlottery.model;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;
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
    private String endTime;
    private String location;
    private String locationAddress;
    private int capacity;
    private Integer waitlistLimit; // Nullable - null means unlimited
    private int waitlistCount;
    private int confirmedCount;
    private String posterImageUrl;
    private double price;
    private long registrationOpens;
    private long registrationCloses;
    private Long lotteryDrawDate; // Nullable
    private long createdAt;
    private long updatedAt;
    private List<String> tags; //Does it work with either arraylist or list?
    private boolean geolocationEnabled;
    private Integer geolocationRadius; // Nullable - in kilometers (1-500)
    private String status; // "open", "closed", "lottery_drawn", "completed"
    private String qrCodeUrl;
    private Bitmap qrCode;
    private boolean isFlagged;
    private int flagCount;
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
        this.endTime = "";
        this.location = "";
        this.locationAddress = "";
        this.capacity = 0;
        this.waitlistLimit = null;
        this.waitlistCount = 0;
        this.confirmedCount = 0;
        this.posterImageUrl = null;
        this.price = 0.0;
        this.registrationOpens = 0L;
        this.registrationCloses = 0L;
        this.lotteryDrawDate = null;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.tags = new ArrayList<>();
        this.geolocationEnabled = false;
        this.geolocationRadius = null;
        this.status = "open";
        this.qrCodeUrl = null;
        this.qrCode = null;
        this.isFlagged = false;
        this.flagCount = 0;
        this.recurringEvent = false;
        this.isPrivate = false;
        this.waitlist = new Waitlist(""); // Use empty strings for defaults
        this.guestList = new GuestList("");
    }

    /**
     * Constructor with all parameters.
     * Use null for eventCapacity or waitlistLimit if they are not restricted.
     */
    public Event(
            String id,
            String name,
            String description,
            String organizerId,
            String date,
            String time,
            String endTime,
            String location,
            String locationAddress,
            int capacity,
            Integer waitlistLimit,
            int waitlistCount,
            int confirmedCount,
            String posterImageUrl,
            double price,
            long registrationOpens,
            long registrationCloses,
            Long lotteryDrawDate,
            long createdAt,
            long updatedAt,
            List<String> tags,
            boolean geolocationEnabled,
            Integer geolocationRadius,
            String status,
            String qrCodeUrl,
            Bitmap qrCode,
            boolean isFlagged,
            int flagCount,
            Waitlist waitlist,
            GuestList guestList,
            boolean recurringEvent,
            boolean isPrivate
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.organizerId = organizerId;
        this.date = date;
        this.time = time;
        this.endTime = endTime;
        this.location = location;
        this.locationAddress = locationAddress;
        this.capacity = capacity;
        this.waitlistLimit = waitlistLimit;
        this.waitlistCount = waitlistCount;
        this.confirmedCount = confirmedCount;
        this.posterImageUrl = posterImageUrl;
        this.price = price;
        this.registrationOpens = registrationOpens;
        this.registrationCloses = registrationCloses;
        this.lotteryDrawDate = lotteryDrawDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = (tags != null) ? new ArrayList<>(tags) : new ArrayList<>();
        this.geolocationEnabled = geolocationEnabled;
        this.geolocationRadius = geolocationRadius;
        this.status = status;
        this.qrCodeUrl = qrCodeUrl;
        this.qrCode = qrCode;
        this.isFlagged = isFlagged;
        this.flagCount = flagCount;
        this.waitlist = waitlist;
        this.guestList = guestList;
        this.recurringEvent = recurringEvent;
        this.isPrivate = isPrivate;

        // Initialize with optional limits
        this.waitlist = (waitlistLimit != null) ? new Waitlist(id, waitlistLimit, Long.toString(registrationCloses)) : new Waitlist(id);
        this.guestList = (capacity != 0) ? new GuestList(id, capacity) : new GuestList(id);
    }

    // Simplified constructor for basic events
    // I do not believe we need this
//    public Event(String id, String name, String description, String organizerId, String date, String time, String location, ArrayList<String> tags, boolean geolocationEnabled, Integer geolocationRadius, String qrCodeUrl) {
//        this(id, name, description, organizerId, date, time, location, tags, geolocationEnabled, geolocationRadius, qrCodeUrl, null, null, null, false, false);
//    }

    // Setters and Getters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Integer getWaitlistLimit() {
        return waitlistLimit;
    }

    public void setWaitlistLimit(Integer waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
    }

    public int getWaitlistCount() {
        return waitlistCount;
    }

    public void setWaitlistCount(int waitlistCount) {
        this.waitlistCount = waitlistCount;
    }

    public int getConfirmedCount() {
        return confirmedCount;
    }

    public void setConfirmedCount(int confirmedCount) {
        this.confirmedCount = confirmedCount;
    }

    public String getPosterImageUrl() {
        return posterImageUrl;
    }

    public void setPosterImageUrl(String posterImageUrl) {
        this.posterImageUrl = posterImageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getRegistrationOpens() {
        return registrationOpens;
    }

    public void setRegistrationOpens(long registrationOpens) {
        this.registrationOpens = registrationOpens;
    }

    public long getRegistrationCloses() {
        return registrationCloses;
    }

    public void setRegistrationCloses(long registrationCloses) {
        this.registrationCloses = registrationCloses;
    }

    public Long getLotteryDrawDate() {
        return lotteryDrawDate;
    }

    public void setLotteryDrawDate(Long lotteryDrawDate) {
        this.lotteryDrawDate = lotteryDrawDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
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

    public Bitmap getQrCode() {
        return qrCode;
    }

    public void setQrCode(Bitmap qrCode) {
        this.qrCode = qrCode;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public int getFlagCount() {
        return flagCount;
    }

    public void setFlagCount(int flagCount) {
        this.flagCount = flagCount;
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

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    /**
     *
     * @param content The text/URL to encode.
     * @return Bitmap of the QR code, or null if generation fails.
     */
    public Bitmap generateQRBitmap(String content) {
        if (content == null || content.isEmpty() || this.isPrivate) return null;

        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 400, 400);
            return new BarcodeEncoder().createBitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
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

    public boolean isWaitlistFull() {
        return waitlist.isWaitlistFull();
    }

    public boolean isRegistrationOpen() {
        long currentTime = System.currentTimeMillis();
        return "open".equals(status) &&
                currentTime >= registrationOpens &&
                currentTime <= registrationCloses &&
                !isWaitlistFull();
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
