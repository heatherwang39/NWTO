package com.example.nwto.model;

public class TableBox implements Comparable<TableBox>{
    private String text;
    private int order, backgroundColor;

    public TableBox(int order, String text, int backgroundColor) {
        this.order = order;
        this.text = text;
        this.backgroundColor = backgroundColor;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public int compareTo(TableBox o) {
        if (this.order > o.order) return 1;
        else if (this.order < o.order) return -1;
        else return 0;
    }
}
