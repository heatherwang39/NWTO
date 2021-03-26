package com.example.nwto.model;

public class Comment {
    private String commenterUID;
    private String postOwnerUID;
    private String profilePic;
    private String fullName;
    private String timeStamp;
    private String content;
    private String postTimeStamp;

    public Comment() {
    }

    public Comment(String commenterUID, String postOwnerUID, String profilePic, String fullName, String timeStamp, String content, String postTimeStamp) {
        this.commenterUID = commenterUID;
        this.postOwnerUID = postOwnerUID;
        this.profilePic = profilePic;
        this.fullName = fullName;
        this.timeStamp = timeStamp;
        this.content = content;
        this.postTimeStamp = postTimeStamp;
    }

    public String getCommenterUID() {
        return commenterUID;
    }

    public void setCommenterUID(String commenterUID) {
        this.commenterUID = commenterUID;
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

    public String getPostTimeStamp() {
        return postTimeStamp;
    }

    public void setPostTimeStamp(String postTimeStamp) {
        this.postTimeStamp = postTimeStamp;
    }
}
