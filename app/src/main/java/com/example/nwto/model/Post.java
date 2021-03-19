package com.example.nwto.model;

public class Post {
    private String uID;
    private String profilePic;
    private String fullName;
    private String postPic;
    private String timeStamp;
    private String topic;
    private String content;
    private String neighbourhood;

    public Post() {
    }

    public Post(String uID, String profilePic, String fullName, String postPic,
                String timeStamp, String topic, String content, String neighbourhood) {
        this.uID = uID;
        this.profilePic = profilePic;
        this.fullName = fullName;
        this.postPic = postPic;
        this.timeStamp = timeStamp;
        this.topic = topic;
        this.content = content;
        this.neighbourhood = neighbourhood;
    }

    public String getUID() {
        return uID;
    }

    public void setUID(String uID) {
        this.uID = uID;
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

}