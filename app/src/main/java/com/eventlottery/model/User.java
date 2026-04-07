package com.eventlottery.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User data model.
 * Part of the 'Model' in MVC.
 */
public class User implements Parcelable {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    private boolean notificationsEnabled = true;
    private String deviceToken;

    /**
     * Default constructor for User.
     */
    public User() {
    }

    /**
     * Constructs a User from a Parcel.
     * @param in The parcel containing the user data.
     */
    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        email = in.readString();
        phone = in.readString();
        profileImageUrl = in.readString();
        notificationsEnabled = in.readByte() != 0;
        deviceToken = in.readString();
    }

    /**
     * Writes the user data to a Parcel.
     * @param dest The parcel to write to.
     * @param flags Additional flags about how the object should be written.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(profileImageUrl);
        dest.writeByte((byte) (notificationsEnabled ? 1 : 0));
        dest.writeString(deviceToken);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Creator for Parcelable implementation.
     */
    public static final Creator<User> CREATOR = new Creator<User>() {
        /**
         * Creates a User from a Parcel.
         * @param in The parcel to read from.
         * @return A new User instance.
         */
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        /**
         * Creates a new array of Users.
         * @param size The size of the array.
         * @return An array of Users.
         */
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // Getters and Setters
    /**
     * Gets the user's unique ID.
     * @return The user ID string.
     */
    public String getId() { return id; }
    /**
     * Sets the user's unique ID.
     * @param id The user ID to set.
     */
    public void setId(String id) { this.id = id; }
    /**
     * Gets the user's name.
     * @return The user's name.
     */
    public String getName() { return name; }
    /**
     * Sets the user's name.
     * @param name The name to set.
     */
    public void setName(String name) { this.name = name; }
    /**
     * Gets the user's email address.
     * @return The email address.
     */
    public String getEmail() { return email; }
    /**
     * Sets the user's email address.
     * @param email The email to set.
     */
    public void setEmail(String email) { this.email = email; }
    /**
     * Gets the user's phone number.
     * @return The phone number.
     */
    public String getPhone() { return phone; }
    /**
     * Sets the user's phone number.
     * @param phone The phone number to set.
     */
    public void setPhone(String phone) { this.phone = phone; }
    /**
     * Gets the URL of the user's profile image.
     * @return The profile image URL.
     */
    public String getProfileImageUrl() { return profileImageUrl; }
    /**
     * Sets the URL of the user's profile image.
     * @param profileImageUrl The URL to set.
     */
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    /**
     * Checks if notifications are enabled for the user.
     * @return true if enabled, false otherwise.
     */
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    /**
     * Sets the notification preference for the user.
     * @param notificationsEnabled true to enable, false to disable.
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
    /**
     * Gets the device token used for push notifications.
     * @return The device token string.
     */
    public String getDeviceToken() { return deviceToken; }
    /**
     * Sets the device token used for push notifications.
     * @param deviceToken The token to set.
     */
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
}
