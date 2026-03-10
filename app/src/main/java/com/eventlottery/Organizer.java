package com.eventlottery;

public class Organizer {
    private String organizerId;
    private String name;
    private String email;
    private String phoneNumber;

    public Organizer(String organizerId, String name, String email, String phoneNumber) {
        this.organizerId = organizerId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    public String getOrganizerId() {
        return organizerId;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
