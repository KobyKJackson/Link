package com.jackson.k.koby.link;

public class FindFriends {
    public String profilePicture, fullName, status;

    public FindFriends()
    {

    }

    public FindFriends(String profilePicture, String fullName, String status)
    {
        this.profilePicture = profilePicture;
        this.fullName = fullName;
        this.status = status;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
