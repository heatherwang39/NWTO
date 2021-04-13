package com.example.nwto.model;

import java.util.List;

public class RegisteredUser {
    String email;
    String fullName;
    String address;
    String phoneNumber;
    String postalCode;
    List<Double> coordinates;
    String radius;
    String frequency;
    String neighbourhood;
    boolean isAdmin;
    String displayPicPath;
    boolean isMuted;

    public RegisteredUser() {
    }

    public RegisteredUser(String email, String fullName, String address, String phoneNumber, String postalCode, List<Double> coordinates, String radius, String frequency, String neighbourhood, boolean isAdmin, String displayPicPath, boolean isMuted) {
        this.email = email;
        this.fullName = fullName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.coordinates = coordinates;
        this.radius = radius;
        this.frequency = frequency;
        this.neighbourhood = neighbourhood;
        this.isAdmin = isAdmin;
        this.displayPicPath = displayPicPath;
        this.isMuted = isMuted;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        isAdmin = isAdmin;
    }

    public String getDisplayPicPath() {
        return displayPicPath;
    }

    public void setDisplayPicPath(String displayPicPath) {
        this.displayPicPath = displayPicPath;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean isMuted) {
        isMuted = isMuted;
    }
}
