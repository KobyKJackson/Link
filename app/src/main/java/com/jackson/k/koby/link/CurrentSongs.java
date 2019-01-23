package com.jackson.k.koby.link;

public class CurrentSongs
{
    public String uid, time, date, postImage, title, profilePicture, fullName;

    public CurrentSongs()
    {

    }

    public CurrentSongs(String uid, String time, String date, String postImage, String title, String profilePicture, String fullName) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postImage = postImage;
        this.title = title;
        this.profilePicture = profilePicture;
        this.fullName = fullName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
