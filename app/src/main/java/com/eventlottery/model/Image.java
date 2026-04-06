package com.eventlottery.model;

public class Image {
    private String url;
    private String eventId;

    public Image(String url, String eventId) {
        this.url = url;
        this.eventId = eventId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
