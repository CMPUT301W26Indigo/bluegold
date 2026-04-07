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
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
}
