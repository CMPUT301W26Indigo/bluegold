package com.eventlottery.data.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/**
 * Event data model representing an event in the lottery system
 * 
 * This class represents all properties and methods for an Event in the system.
 * It includes geolocation validation, waitlist management, and lottery functionality.
 */
public class Event implements Parcelable {
    
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
    private List<String> tags;
    private String posterImageUrl;
    private boolean geolocationEnabled;
    private Integer geolocationRadius; // Nullable - in kilometers (1-500)
    private Double geolocationLat; // Nullable
    private Double geolocationLng; // Nullable
    private double price;
    private String status; // "open", "closed", "lottery_drawn", "completed"
    private long registrationOpens;
    private long registrationCloses;
    private Long lotteryDrawDate; // Nullable
    private String qrCodeUrl;
    private long createdAt;
    private long updatedAt;
    private boolean isFlagged;
    private int flagCount;
    
    // Default constructor
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
        this.tags = new ArrayList<>();
        this.posterImageUrl = null;
        this.geolocationEnabled = false;
        this.geolocationRadius = null;
        this.geolocationLat = null;
        this.geolocationLng = null;
        this.price = 0.0;
        this.status = "open";
        this.registrationOpens = 0L;
        this.registrationCloses = 0L;
        this.lotteryDrawDate = null;
        this.qrCodeUrl = null;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isFlagged = false;
        this.flagCount = 0;
    }
    
    // Full constructor
    public Event(String id, String name, String description, String organizerId,
                 String date, String time, String endTime, String location,
                 String locationAddress, int capacity, Integer waitlistLimit,
                 int waitlistCount, int confirmedCount, List<String> tags,
                 String posterImageUrl, boolean geolocationEnabled,
                 Integer geolocationRadius, Double geolocationLat,
                 Double geolocationLng, double price, String status,
                 long registrationOpens, long registrationCloses,
                 Long lotteryDrawDate, String qrCodeUrl, long createdAt,
                 long updatedAt, boolean isFlagged, int flagCount) {
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
        this.tags = tags != null ? tags : new ArrayList<>();
        this.posterImageUrl = posterImageUrl;
        this.geolocationEnabled = geolocationEnabled;
        this.geolocationRadius = geolocationRadius;
        this.geolocationLat = geolocationLat;
        this.geolocationLng = geolocationLng;
        this.price = price;
        this.status = status;
        this.registrationOpens = registrationOpens;
        this.registrationCloses = registrationCloses;
        this.lotteryDrawDate = lotteryDrawDate;
        this.qrCodeUrl = qrCodeUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isFlagged = isFlagged;
        this.flagCount = flagCount;
    }
    
    // Parcelable constructor
    protected Event(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        organizerId = in.readString();
        date = in.readString();
        time = in.readString();
        endTime = in.readString();
        location = in.readString();
        locationAddress = in.readString();
        capacity = in.readInt();
        if (in.readByte() == 0) {
            waitlistLimit = null;
        } else {
            waitlistLimit = in.readInt();
        }
        waitlistCount = in.readInt();
        confirmedCount = in.readInt();
        tags = in.createStringArrayList();
        posterImageUrl = in.readString();
        geolocationEnabled = in.readByte() != 0;
        if (in.readByte() == 0) {
            geolocationRadius = null;
        } else {
            geolocationRadius = in.readInt();
        }
        if (in.readByte() == 0) {
            geolocationLat = null;
        } else {
            geolocationLat = in.readDouble();
        }
        if (in.readByte() == 0) {
            geolocationLng = null;
        } else {
            geolocationLng = in.readDouble();
        }
        price = in.readDouble();
        status = in.readString();
        registrationOpens = in.readLong();
        registrationCloses = in.readLong();
        if (in.readByte() == 0) {
            lotteryDrawDate = null;
        } else {
            lotteryDrawDate = in.readLong();
        }
        qrCodeUrl = in.readString();
        createdAt = in.readLong();
        updatedAt = in.readLong();
        isFlagged = in.readByte() != 0;
        flagCount = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(organizerId);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(endTime);
        dest.writeString(location);
        dest.writeString(locationAddress);
        dest.writeInt(capacity);
        if (waitlistLimit == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(waitlistLimit);
        }
        dest.writeInt(waitlistCount);
        dest.writeInt(confirmedCount);
        dest.writeStringList(tags);
        dest.writeString(posterImageUrl);
        dest.writeByte((byte) (geolocationEnabled ? 1 : 0));
        if (geolocationRadius == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(geolocationRadius);
        }
        if (geolocationLat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(geolocationLat);
        }
        if (geolocationLng == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(geolocationLng);
        }
        dest.writeDouble(price);
        dest.writeString(status);
        dest.writeLong(registrationOpens);
        dest.writeLong(registrationCloses);
        if (lotteryDrawDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(lotteryDrawDate);
        }
        dest.writeString(qrCodeUrl);
        dest.writeLong(createdAt);
        dest.writeLong(updatedAt);
        dest.writeByte((byte) (isFlagged ? 1 : 0));
        dest.writeInt(flagCount);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }
        
        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    
    // Business Logic Methods
    
    /**
     * Check if the event is currently accepting registrations
     */
    public boolean isRegistrationOpen() {
        long currentTime = System.currentTimeMillis();
        return "open".equals(status) && 
               currentTime >= registrationOpens && 
               currentTime <= registrationCloses &&
               !isWaitlistFull();
    }
    
    /**
     * Check if waitlist is full
     */
    public boolean isWaitlistFull() {
        if (waitlistLimit == null) {
            return false; // Unlimited waitlist
        }
        return waitlistCount >= waitlistLimit;
    }
    
    /**
     * Get available spots on waitlist
     */
    public int getAvailableSpots() {
        return Math.max(0, capacity - confirmedCount);
    }
    
    /**
     * Get formatted price string
     */
    public String getFormattedPrice() {
        if (price == 0.0) {
            return "Free";
        } else {
            return String.format("$%.2f", price);
        }
    }
    
    /**
     * Check if user location is within geolocation radius
     */
    public boolean isWithinGeolocationRadius(double userLat, double userLng) {
        if (!geolocationEnabled || geolocationLat == null || 
            geolocationLng == null || geolocationRadius == null) {
            return true; // No geolocation restriction
        }
        
        double distance = calculateDistance(userLat, userLng, 
                                           geolocationLat, geolocationLng);
        return distance <= geolocationRadius;
    }
    
    /**
     * Calculate distance between two coordinates in kilometers using Haversine formula
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS_KM = 6371.0;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    // Getters and Setters
    
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
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getPosterImageUrl() {
        return posterImageUrl;
    }
    
    public void setPosterImageUrl(String posterImageUrl) {
        this.posterImageUrl = posterImageUrl;
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
    
    public Double getGeolocationLat() {
        return geolocationLat;
    }
    
    public void setGeolocationLat(Double geolocationLat) {
        this.geolocationLat = geolocationLat;
    }
    
    public Double getGeolocationLng() {
        return geolocationLng;
    }
    
    public void setGeolocationLng(Double geolocationLng) {
        this.geolocationLng = geolocationLng;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public String getQrCodeUrl() {
        return qrCodeUrl;
    }
    
    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
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
}
