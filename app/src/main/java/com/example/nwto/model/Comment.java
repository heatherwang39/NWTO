package com.example.nwto.model;

public class Comment {
    private String postOwnerUID;
    private String profilePic;
    private String fullName;
    private String timeStamp;
    private String content;

    public Comment() {
    }

    public Comment(String postOwnerUID, String profilePic, String fullName, String timeStamp, String content) {
        this.postOwnerUID = postOwnerUID;
        this.profilePic = profilePic;
        this.fullName = fullName;
        this.timeStamp = timeStamp;
        this.content = content;
    }

    public String getPostOwnerUID() {
        return postOwnerUID;
    }

    public void setPostOwnerUID(String uID) {
        this.postOwnerUID = uID;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
