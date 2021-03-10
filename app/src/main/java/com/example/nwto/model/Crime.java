package com.example.nwto.model;

public class Crime implements Comparable<Crime>{
    private int order;
    private String uniqueID, division, premise, category, date;
    private double latitude, longitude;

    public Crime(int order, String uniqueID, String division, String premise, String category, String occurenceDate, double latitude, double longitude) {
        this.order = order;
        this.uniqueID = uniqueID;
        this.division = division;
        this.premise = premise;
        this.category = category;
        this.date = occurenceDate;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getPremise() {
        return premise;
    }

    public void setPremise(String premise) {
        this.premise = premise;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int compareTo(Crime o) {
        if (this.order > o.order) return 1;
        else if (this.order < o.order) return -1;
        else return 0;
    }
}
