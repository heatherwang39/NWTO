package com.example.nwto.model;

public class TableBox {
    private String text;
    private int backgroundColor;

    public TableBox(String text, int backgroundColor) {
        this.text = text;
        this.backgroundColor = backgroundColor;
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
}
