package com.example.nwto.model;

public class Post {
    private String ownerUID;
    private String profilePic;
    private String fullName;
    private String postPic;
    private String timeStamp;
    private String topic;
    private String content;
    private String neighbourhood;
    private String crimeType;

    public Post() {
    }

    public Post(String ownerUID, String profilePic, String fullName, String postPic,
                String timeStamp, String topic, String content, String neighbourhood, String crimeType) {
        this.ownerUID = ownerUID;
        this.profilePic = profilePic;
        this.fullName = fullName;
        this.postPic = postPic;
        this.timeStamp = timeStamp;
        this.topic = topic;
        this.content = content;
        this.neighbourhood = neighbourhood;
        this.crimeType = crimeType;
    }

    public String getOwnerUID() {
        return ownerUID;
    }

    public void setOwnerUID(String uID) {
        this.ownerUID = uID;
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

    public String getPostPic() {
        return postPic;
    }

    public void setPostPic(String storageRef) {
        this.postPic = storageRef;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public String getCrimeType() {
        return crimeType;
    }

    public void setCrimeType(String crimeType) {
        this.crimeType = crimeType;
    }
}
