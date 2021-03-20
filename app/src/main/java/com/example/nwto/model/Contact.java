package com.example.nwto.model;

public class Contact implements Comparable<Contact> {
    private int order;
    private String title, name, email, phoneNumb;

    public Contact(int order, String title, String name, String email, String phoneNumb) {
        this.order = order;
        this.title = title;
        this.name = name;
        this.email = email;
        this.phoneNumb = phoneNumb;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumb() {
        return phoneNumb;
    }

    public void setPhoneNumb(String phoneNumb) {
        this.phoneNumb = phoneNumb;
    }

    @Override
    public int compareTo(Contact o) {
        if (this.order > o.order) return 1;
        else if (this.order < o.order) return -1;
        else return 0;
    }
}
