package com.example.nwto.model;

public class Post {
    private String uID;
    private String storageRef;
    private String timeStamp;
    private String topic;
    private String content;
    private String fullName;
    private String Neighbourhood;

    public Post(){
    }

    public Post(String uID, String storageRef, String timeStamp, String topic,
                String content, String fullName, String Neighbourhood) {
        this.uID = uID;
        this.storageRef = storageRef;
        this.timeStamp = timeStamp;
        this.topic = topic;
        this.content = content;
        this.fullName = fullName;
        this.Neighbourhood = Neighbourhood;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getStorageRef() {
        return storageRef;
    }

    public void setStorageRef(String storageRef) {
        this.storageRef = storageRef;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNeighbourhood() {
        return Neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        Neighbourhood = neighbourhood;
    }



}
