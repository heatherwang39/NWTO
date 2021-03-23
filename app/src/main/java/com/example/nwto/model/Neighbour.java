package com.example.nwto.model;

public class Neighbour {
    private String mNeighbourID;
    private String mOwnerUID;
    private String mFullName;
    private String mEmail;
    private String mPhoneNumber;

    public Neighbour() {
    }

    public Neighbour(String neighbourID, String ownerUID, String fullName, String email, String phoneNumber) {
        this.mNeighbourID = neighbourID;
        this.mOwnerUID = ownerUID;
        this.mFullName = fullName;
        this.mEmail = email;
        this.mPhoneNumber = phoneNumber;
    }

    public String getNeighbourID() {
        return mNeighbourID;
    }

    public void setNeighbourID(String mNeighbourID) {
        this.mNeighbourID = mNeighbourID;
    }

    public String getOwnerUID() {
        return mOwnerUID;
    }

    public void setOwnerUID(String ownerUID) {
        this.mOwnerUID = ownerUID;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        this.mFullName = fullName;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
    }

}
