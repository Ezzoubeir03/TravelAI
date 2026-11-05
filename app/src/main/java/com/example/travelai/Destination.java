package com.example.travelai;

public class Destination {
    private final String name;
    private final String country;
    private final int imageRes;

    public Destination(String name, String country, int imageRes) {
        this.name = name;
        this.country = country;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getCountry() { return country; }
    public int getImageRes() { return imageRes; }
}